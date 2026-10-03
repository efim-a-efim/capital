package dev.capital

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.capital.data.*
import dev.capital.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class ScreenState(
    val data: Portfolio = Portfolio(), val ready: Boolean = false, val loading: Boolean = true,
    val saving: Boolean = false, val refreshing: Boolean = false, val unsaved: Boolean = false,
    val message: String? = null, val heads: List<Revision> = emptyList(), val folder: String? = null,
    val blocked: Boolean = false, val restore: Revision? = null,
    val encrypted: Boolean = false,
    /** OPEN = no encryption or unlocked; PASSWORD = password needed; PIN = reserved for the lock step. */
    val security: String = "OPEN",
    val checking: Boolean = false, val rewrite: Rewrite? = null,
    /** "on", "off" or "change" when an unfinished rewrite was detected. */
    val pendingRewrite: String? = null,
    val plaintextExports: Int = 0, val lastPlaintextExport: Long? = null,
    /** URI of an encrypted backup waiting for its password. */
    val restorePassword: String? = null,
)
class CapitalModel(app: Application): AndroidViewModel(app) {
    private val prefs=app.getSharedPreferences("bootstrap",0)
    private val resolver=app.contentResolver
    val secrets=Secrets(app)
    val lock=Lock(app)
    /** Hooks: encryption off or a new data key makes the device-bound copies useless; the UI offers a new PIN. */
    var onEncryptionOff: ()->Unit = { lock.clear() }
    var onKeyChanged: (DataKey)->Unit = { it.wipe(); lock.clear() }
    var hasPin: ()->Boolean = { lock.hasPin }
    private var backgroundAt=0L
    private var store: FolderStore?=null
    private val mutable=MutableStateFlow(ScreenState(plaintextExports=prefs.getInt("plaintextExports",0),lastPlaintextExport=prefs.getLong("lastPlaintextExport",0).takeIf { it>0 }))
    val state=mutable.asStateFlow()
    private val transaction=Mutex()
    private var refreshJob: Job?=null
    private val providers=Providers(secrets::get)
    private var foreground=true
    init {
        val uri=prefs.getString("folder",null)
        if(uri==null) update { it.copy(loading=false) } else viewModelScope.launch {
            connect(Uri.parse(uri),false)
            startupRefresh()
        }
    }
    private fun update(block: (ScreenState)->ScreenState) { mutable.update(block) }
    private val isOpen get() = mutable.value.security=="OPEN"
    /** A copy the caller owns. */
    fun currentKey(): DataKey? = store?.key?.let { DataKey(it.bytes()) }
    fun dismissMessage() { update { it.copy(message=null) } }
    fun notice(message: String) { update { it.copy(message=message) } }
    // One automatic refresh per process, as soon as a folder is open: at launch, or after the first folder choice.
    private var started=false
    private fun startupRefresh() { if(!started && foreground && isOpen && mutable.value.pendingRewrite==null && mutable.value.ready && !mutable.value.blocked) { started=true; refresh() } }
    fun chooseFolder(uri: Uri, copy: Boolean = false) = viewModelScope.launch { connect(uri,copy); startupRefresh() }
    private suspend fun connect(uri: Uri,copy: Boolean) = transaction.withLock {
        update { it.copy(loading=true) }
        try {
            resolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            val next=FolderStore(resolver,uri); val forms=next.forms()
            if(forms.encrypted>0 && !copy) {
                require(!mutable.value.unsaved) { tr("Unsaved changes retained. Retry save, save a copy, or explicitly discard before reopening.") }
                store=next
                update { it.copy(encrypted=true,security=if(hasPin()) "PIN" else "PASSWORD",ready=false,data=Portfolio(),heads=emptyList(),blocked=false,unsaved=false,pendingRewrite=null,restore=null,restorePassword=null,message=null) }
                prefs.edit().putString("folder",uri.toString()).apply()
                update { it.copy(folder=uri.toString()) }
                return@withLock
            }
            // a marker without encrypted files: the switch on never started, or the switch off had finished
            if(forms.encrypted==0) prefs.edit().remove("rewrite").remove("previousKey").apply()
            val scan=next.scan()
            if(copy) {
                require(scan.heads.isEmpty() && scan.invalid==0) { tr("Save copy needs an empty folder. Existing data was not changed.") }
                val (revision,after)=next.save(mutable.value.data,emptySet())
                store=next
                update { it.copy(data=revision.data,heads=after.heads,unsaved=false,ready=true,blocked=after.conflicted,encrypted=false,security="OPEN",pendingRewrite=null,message=tr("Copy saved")) }
            } else {
                require(!mutable.value.unsaved) { tr("Unsaved changes retained. Retry save, save a copy, or explicitly discard before reopening.") }
                store=next
                update { it.copy(encrypted=false,security="OPEN",pendingRewrite=null,restorePassword=null) }
                applyScan(scan)
            }
            prefs.edit().putString("folder",uri.toString()).apply()
            update { it.copy(folder=uri.toString()) }
        } catch(e: Exception) { update { it.copy(message=e.message ?: tr("Folder unavailable")) } }
        finally { update { it.copy(loading=false) } }
    }
    private fun applyScan(scan: Scan) {
        val blocked=scan.conflicted || scan.missingParents
        update { it.copy(
            data=scan.heads.firstOrNull()?.data ?: if(scan.invalid>0) it.data else Portfolio(settings=it.data.settings),
            ready=true,heads=scan.heads,blocked=blocked,unsaved=false,
            message=when {
                scan.conflicted -> tr("Sync conflict: choose a revision below. Both originals will be preserved.")
                scan.missingParents -> tr("Sync incomplete. Wait for the remaining files, then reload.")
                scan.invalid>0 -> tr("Ignored {0} invalid or interrupted snapshots. Last valid data retained.",scan.invalid)
                else -> null
            },
        ) }
    }
    fun resume() {
        foreground=true
        // auto-lock: only with a PIN; never in the middle of a password operation
        val s=mutable.value
        // Unsaved edits are kept rather than lost to a lock.
        if(s.encrypted && s.security=="OPEN" && !s.checking && !s.unsaved && backgroundAt>0 && lock.hasPin && SystemClock.elapsedRealtime()-backgroundAt>=lock.lockAfterSeconds*1000L) lockNow()
        if(store != null && isOpen && !mutable.value.loading && !mutable.value.unsaved) reload()
    }
    // A system picker opened by the app itself is not time away from the app.
    private var expecting=false
    fun expectReturn() { expecting=true }
    fun background() { foreground=false; backgroundAt=if(expecting) 0L else SystemClock.elapsedRealtime(); expecting=false; refreshJob?.cancel() }
    fun reload(discard: Boolean = false) = viewModelScope.launch {
        transaction.withLock {
            if(!isOpen) return@withLock
            if(mutable.value.unsaved && !discard) { notice(tr("Unsaved changes retained. Retry save or save a copy.")); return@withLock }
            try { store?.scan()?.let { applyScan(it) } }
            catch(e: Exception) { update { it.copy(blocked=true,message=e.message ?: tr("Cannot reload folder")) } }
        }
    }
    fun edit(transform: (Portfolio)->Portfolio,onSaved: ()->Unit = {}) = viewModelScope.launch {
        transaction.withLock {
            if(!isOpen) return@withLock
            if(mutable.value.blocked || mutable.value.unsaved) { notice(tr("Resolve storage issues before editing")); return@withLock }
            try { persist(transform(mutable.value.data).ranked().validate()); onSaved() }
            catch(e: Exception) { notice(e.message ?: tr("Could not save")) }
        }
    }
    private suspend fun persist(data: Portfolio,resolve: Boolean=false) {
        val target=store ?: error(tr("Choose a storage folder first"))
        check(isOpen) { tr("Locked") }
        update { it.copy(data=data,saving=true,unsaved=true) }
        try {
            val (_,scan)=target.save(data,mutable.value.heads.map { it.id }.toSet(),resolve)
            update { it.copy(heads=scan.heads,unsaved=false,blocked=scan.conflicted || scan.missingParents,message=if(scan.conflicted) tr("Concurrent edit detected. Resolve the conflict.") else tr("Saved on device")) }
        } finally { update { it.copy(saving=false) } }
    }
    fun retrySave()=viewModelScope.launch { transaction.withLock {
        try { persist(mutable.value.data) } catch(e: Exception) { notice(e.message ?: tr("Save failed")) }
    } }
    fun resolve(revision: Revision)=viewModelScope.launch { transaction.withLock {
        try { persist(revision.data.ranked(),true) } catch(e: Exception) { notice(e.message ?: tr("Conflict resolution failed")) }
    } }
    fun refresh(bucketId: String?=null) {
        if(refreshJob?.isActive==true || !isOpen || !mutable.value.ready || mutable.value.blocked || mutable.value.unsaved || !foreground) return
        val requested=mutable.value.data
        // Nothing to value: never write a snapshot for an empty portfolio (e.g. a folder listing that came back empty).
        if(requested.buckets.isEmpty() && requested.goals.isEmpty()) return
        refreshJob=viewModelScope.launch {
            update { it.copy(refreshing=true,message=tr("Refreshing selected providers…")) }
            try {
                val observations=providers.refresh(requested,bucketId)
                transaction.withLock {
                    if(mutable.value.blocked || mutable.value.unsaved) { notice(tr("Refresh finished; resolve storage issues before retrying.")); return@withLock }
                    withContext(NonCancellable) { persist(mergeObservations(mutable.value.data,requested,observations.holdings,observations.quotes,observations.unlisted)) }
                    notice(if(observations.errors.isEmpty()) tr("Refreshed. Shared rates may revalue other buckets.") else observations.errors.joinToString("\n"))
                }
            } catch(e: CancellationException) { notice(tr("Refresh stopped. Cached values retained.")); throw e }
            catch(e: Exception) { notice(e.message ?: tr("Refresh failed. Cached values retained.")) }
            finally { update { it.copy(refreshing=false) } }
        }
    }
    /** Loads an address's tokens for the holding editor; persists nothing. */
    fun fetchTokens(asset: String,address: String,done: (List<Token>?,String?)->Unit) { viewModelScope.launch {
        val data=mutable.value.data; val source=data.settings.providers["$asset tokens"]
        if(asset==Chain.BTC.name) return@launch done(emptyList(),null)
        if(source==null || source=="Off") return@launch done(null,tr("Token source for {0} is Off. Choose one in Settings.",asset))
        val stored=data.holdings.filter { it.asset==asset }.flatMap { it.tokens }
        val list=try { providers.tokens(Holding(bucketId="-",label="-",asset=asset,quantity=null,address=address),source).map { t -> stored.find { it.contract==t.contract }?.let { o -> t.copy(decimals=t.decimals ?: o.decimals,symbol=t.symbol.ifBlank { o.symbol },checkedAt=o.checkedAt) } ?: t } }
        catch(e: CancellationException) { throw e }
        catch(e: Exception) { return@launch done(null,e.safeMessage()) }
        done(list,null)
    } }
    /** Lists a broker's accounts for the holding editor; persists nothing. */
    fun fetchAccounts(broker: String,done: (List<Pair<String,String>>?,String?)->Unit) { viewModelScope.launch {
        try { done(providers.accounts(broker),null) } catch(e: CancellationException) { throw e } catch(e: Exception) { done(null,e.safeMessage()) }
    } }
    /** Opens SnapTrade's Connection Portal for the user's own SnapTrade account. */
    fun connectSnapTrade(open: (String)->Unit) { viewModelScope.launch {
        try { open(providers.snapTradeLogin()) } catch(e: CancellationException) { throw e } catch(e: Exception) { notice(e.safeMessage()) }
    } }
    fun saveKey(provider: String,value: String): Boolean = try { secrets.put(provider,value); notice(tr("{0} key saved on this device",provider)); true } catch(e: Exception) { notice(e.message ?: tr("Could not save key")); false }
    fun export(uri: Uri,plaintext: Boolean=false)=viewModelScope.launch {
        if(plaintext && mutable.value.encrypted) return@launch notice(tr("Enter the password to export without encryption"))
        writeBackup(uri)
    }
    private suspend fun writeBackup(uri: Uri,plaintext: Boolean=false) {
        try {
            check(isOpen) { tr("Unlock the folder first") }
            val data=mutable.value.data; val s=store
            // encrypted unless encryption is off or a plaintext export was asked for after the password check
            val key=if(mutable.value.encrypted && !plaintext) (s?.key ?: error(tr("Unlock the folder first"))) else null
            val text=encodeRevision(Revision(data=data)).let { if(key!=null) encryptSnapshot(it,key,s!!.header!!) else it }
            withContext(Dispatchers.IO) {
                resolver.openOutputStream(uri,"wt")?.use { it.write(text.toByteArray()); it.flush() } ?: error(tr("Cannot write backup"))
                val readback=resolver.openInputStream(uri)?.use { it.readLimited(MAX_FILE_BYTES*2).toString(Charsets.UTF_8) } ?: error(tr("Cannot verify backup"))
                require(decodeRevision(if(key!=null) decryptSnapshot(readback,key) else readback).data==data) { tr("Backup verification failed; retry export") }
            }
            if(key==null) { // every readable backup counts, also those exported while encryption is off
                val count=mutable.value.plaintextExports+1; val now=System.currentTimeMillis()
                prefs.edit().putInt("plaintextExports",count).putLong("lastPlaintextExport",now).apply()
                update { it.copy(plaintextExports=count,lastPlaintextExport=now) }
            }
            notice(if(key!=null) tr("Encrypted backup exported without provider keys") else tr("Backup exported without provider keys"))
        } catch(e: Exception) { notice(e.message ?: tr("Export failed")) }
    }
    fun exportPlaintext(uri: Uri,password: CharArray,done: (String?)->Unit) = op(arrayOf(password),done) {
        val h=store?.header ?: return@op tr("Encryption is off")
        checkPassword(password,listOf(h))?.first?.wipe() ?: return@op tr("Wrong password")
        writeBackup(uri,true); null
    }
    fun inspectRestore(uri: Uri)=viewModelScope.launch {
        if(!isOpen) return@launch
        try {
            val text=readBackup(uri)
            if(!isEncrypted(text)) return@launch update { it.copy(restore=decodeRevision(text)) }
            val revision=store?.key?.let { k -> runCatching { decodeRevision(decryptSnapshot(text,k)) }.getOrNull() }
            update { if(revision!=null) it.copy(restore=revision) else it.copy(restorePassword=uri.toString()) }
        } catch(e: Exception) { notice(e.message ?: tr("Invalid backup")) }
    }
    private suspend fun readBackup(uri: Uri) = withContext(Dispatchers.IO) { resolver.openInputStream(uri)?.use { it.readLimited(MAX_FILE_BYTES*2).toString(Charsets.UTF_8) } ?: error(tr("Cannot read backup")) }
    fun inspectRestoreWith(password: CharArray,done: (String?)->Unit) = op(arrayOf(password),done) {
        val uri=mutable.value.restorePassword ?: return@op tr("Choose a backup first")
        val text=readBackup(Uri.parse(uri))
        val (key,_)=checkPassword(password,listOf(headerOf(text))) ?: return@op tr("Wrong password")
        try { update { it.copy(restore=decodeRevision(decryptSnapshot(text,key)),restorePassword=null) } } finally { key.wipe() }
        null
    }
    fun cancelRestore() { update { it.copy(restore=null,restorePassword=null) } }
    fun restore() { val revision=mutable.value.restore ?: return; edit({ revision.data }) { cancelRestore() } }

    // ---- encryption ----
    /** Runs a password-related operation off the main thread, one at a time; secrets are zeroed at the end. */
    private fun op(secrets: Array<CharArray>,done: (String?)->Unit,body: suspend ()->String?) {
        if(mutable.value.checking) { secrets.forEach { it.fill('\u0000') }; return done(tr("Another check is running")) }
        update { it.copy(checking=true) }
        viewModelScope.launch {
            val message=try { body() } catch(e: CancellationException) { throw e } catch(e: Exception) { e.message ?: tr("Operation failed") }
            finally { secrets.forEach { it.fill('\u0000') }; update { it.copy(checking=false,rewrite=null) } }
            done(message)
        }
    }
    /** Key derivation and a random 1 to 5 s wait run together; the result is reported only after both, so accept and reject look alike. */
    private suspend fun checkPassword(password: CharArray,headers: List<KeyHeader>): Pair<DataKey,KeyHeader>? = coroutineScope {
        val wait=async { delay(passwordDelayMillis()) }
        val found=async(Dispatchers.Default) { headers.firstNotNullOfOrNull { h -> try { unlock(h,password) to h } catch(_: WrongPassword) { null } } }
        wait.await(); found.await()
    }
    /** Precondition for a rewrite: nothing changes under it. */
    private fun idle(): String? { val s=mutable.value; return when {
        store==null -> tr("Choose a storage folder first")
        !isOpen || !s.ready -> tr("Unlock the folder first")
        s.blocked -> tr("Resolve the sync conflict or wait for sync first")
        s.unsaved -> tr("Save or discard your edits first")
        s.saving || s.refreshing -> tr("Wait for saving or refreshing to finish")
        else -> null
    } }
    /** Everything after a successful unlock: key into the store, scan, detect an unfinished rewrite. */
    private suspend fun openWith(key: DataKey,header: KeyHeader): String? = transaction.withLock {
        val s=store ?: return@withLock tr("Choose a storage folder first")
        fun fail(message: String): String { s.key=null; s.header=null; s.previous=null; key.wipe(); return message }
        s.key=key; s.header=header; s.previous=null
        prefs.getString("previousKey",null)?.let { w ->
            s.previous=try { unwrapKey(w,key) } catch(_: Exception) { return@withLock fail(tr("A password change was interrupted. Enter the new password.")) }
        }
        try {
            val scan=s.scan(); val forms=s.forms()
            applyScan(scan)
            val pending=prefs.getString("rewrite",null) ?: if(forms.plain>0 && forms.encrypted>0) "on" else null
            update { it.copy(security="OPEN",encrypted=true,pendingRewrite=pending) }
        } catch(e: Exception) { return@withLock fail(e.message ?: tr("Cannot open folder")) }
        null
    }
    fun submitPassword(password: CharArray,done: (String?)->Unit) = op(arrayOf(password),done) {
        val s=store ?: return@op tr("Choose a storage folder first")
        val (key,header)=checkPassword(password,s.headers()) ?: return@op tr("Wrong password")
        openWith(key,header).also { if(it==null) lock.resetFailures(); startupRefresh() }
    }
    /** PIN or biometric unlock: the key comes from device-bound storage; the caller keeps ownership of its copy. */
    fun unlockWith(key: DataKey,done: (String?)->Unit) = op(arrayOf(),done) {
        val s=store ?: return@op tr("Choose a storage folder first")
        val own=DataKey(key.bytes())
        val header=s.headerFor(own) ?: run { own.wipe(); return@op tr("Stored key does not match this folder") }
        openWith(own,header).also { startupRefresh() }
    }
    /** The pin array is zeroed. done(null) = unlocked. */
    fun submitPin(pin: CharArray,done: (String?)->Unit) { viewModelScope.launch {
        var key: DataKey?=null
        val message=try { key=withContext(Dispatchers.Default) { lock.unlockWithPin(pin) }; null }
            catch(e: IllegalStateException) { e.message } finally { pin.fill('\u0000') }
        if(key==null) {
            if(!lock.hasPin) update { it.copy(security="PASSWORD") }
            return@launch done(message ?: if(!lock.hasPin) tr("PIN removed after 10 wrong entries. Use your password.") else lock.blockedForMillis().let { w -> if(w>0) tr("Wrong PIN. Try again in {0}",formatWait(w)) else tr("Wrong PIN") })
        }
        unlockWith(key) { key.wipe(); done(it) }
    } }
    fun lockNow() {
        refreshJob?.cancel()
        store?.let { it.key?.wipe(); it.previous?.wipe(); it.key=null; it.header=null; it.previous=null }
        update { it.copy(data=Portfolio(),ready=false,heads=emptyList(),blocked=false,unsaved=false,restore=null,restorePassword=null,security=if(hasPin()) "PIN" else "PASSWORD") }
    }
    private suspend fun runRewrite(s: FolderStore,mode: String,target: Pair<DataKey,KeyHeader>?,previous: DataKey?=null): String? {
        try {
            val r=s.rewrite(target,previous) { p -> update { it.copy(rewrite=p) } }
            if(r.skipped>0) notice(tr("{0} unreadable files were left untouched",r.skipped))
        } catch(e: CancellationException) { throw e }
        catch(e: Exception) {
            update { it.copy(encrypted=it.encrypted || mode!="off",pendingRewrite=mode) }
            return e.message ?: tr("Rewrite stopped. Resume it in Settings.")
        }
        prefs.edit().remove("rewrite").remove("previousKey").commit()
        update { it.copy(encrypted=target!=null,security="OPEN",pendingRewrite=null) }
        if(target==null) onEncryptionOff() else if(mode=="change") onKeyChanged(DataKey(target.first.bytes()))
        previous?.wipe()
        return null
    }
    fun enableEncryption(password: CharArray,acceptedBackupRisk: Boolean,done: (String?)->Unit) = op(arrayOf(password),done) {
        if(!acceptedBackupRisk) return@op tr("Accept the backup warning first")
        validPassword(password)?.let { return@op it }
        transaction.withLock {
            idle()?.let { return@withLock it }
            val s=store!!
            if(s.key!=null) return@withLock tr("Encryption is already on")
            val (key,header)=coroutineScope {
                val wait=async { delay(passwordDelayMillis()) }
                val made=async(Dispatchers.Default) { newKey(password) }
                wait.await(); made.await()
            }
            prefs.edit().putString("rewrite","on").commit()
            s.key=key; s.header=header // scans read the mixed folder while the rewrite runs
            runRewrite(s,"on",key to header)
        }
    }
    fun disableEncryption(password: CharArray,done: (String?)->Unit) = op(arrayOf(password),done) {
        transaction.withLock {
            idle()?.let { return@withLock it }
            val s=store!!; val header=s.header ?: return@withLock tr("Encryption is off")
            checkPassword(password,listOf(header))?.first?.wipe() ?: return@withLock tr("Wrong password")
            prefs.edit().putString("rewrite","off").commit()
            runRewrite(s,"off",null)
        }
    }
    fun changePassword(current: CharArray,next: CharArray,done: (String?)->Unit) = op(arrayOf(current,next),done) {
        validPassword(next)?.let { return@op it }
        transaction.withLock {
            idle()?.let { return@withLock it }
            val s=store!!; val header=s.header ?: return@withLock tr("Encryption is off")
            val (old,_)=checkPassword(current,listOf(header)) ?: return@withLock tr("Wrong password")
            val (key,newHeader)=withContext(Dispatchers.Default) { newKey(next) }
            // the old key stays recoverable with the new password until every file is rewritten
            prefs.edit().putString("previousKey",wrapKey(old,key)).putString("rewrite","change").commit()
            s.key=key; s.header=newHeader; s.previous=old
            runRewrite(s,"change",key to newHeader,old)
        }
    }
    /** Finishes an interrupted switch on, switch off or password change; needs the key in memory. */
    fun resumeRewrite(done: (String?)->Unit) = op(arrayOf(),done) {
        transaction.withLock {
            idle()?.let { return@withLock it }
            val s=store!!; val key=s.key; val header=s.header
            val forms=s.forms()
            val mode=prefs.getString("rewrite",null) ?: if(forms.plain>0 && forms.encrypted>0) "on" else return@withLock tr("Nothing to finish")
            if(key==null || header==null) return@withLock tr("Unlock the folder first")
            runRewrite(s,mode,if(mode=="off") null else key to header,s.previous)
        }
    }
}
