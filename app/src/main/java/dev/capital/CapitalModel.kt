package dev.capital

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.capital.data.*
import dev.capital.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class ScreenState(
    val data: Portfolio = Portfolio(), val ready: Boolean = false, val loading: Boolean = true,
    val saving: Boolean = false, val refreshing: Boolean = false, val unsaved: Boolean = false,
    val message: String? = null, val heads: List<Revision> = emptyList(), val folder: String? = null,
    val blocked: Boolean = false, val restore: Revision? = null,
)
class CapitalModel(app: Application): AndroidViewModel(app) {
    private val prefs=app.getSharedPreferences("bootstrap",0)
    private val resolver=app.contentResolver
    val secrets=Secrets(app)
    private var store: FolderStore?=null
    private val mutable=MutableStateFlow(ScreenState())
    val state=mutable.asStateFlow()
    private val transaction=Mutex()
    private var refreshJob: Job?=null
    private val providers=Providers(secrets::get)
    private var foreground=true
    init {
        val uri=prefs.getString("folder",null)
        if(uri==null) mutable.value=ScreenState(loading=false) else viewModelScope.launch {
            connect(Uri.parse(uri),false)
            if(foreground && mutable.value.ready && !mutable.value.blocked) refresh()
        }
    }
    private fun update(block: (ScreenState)->ScreenState) { mutable.value=block(mutable.value) }
    fun dismissMessage() { update { it.copy(message=null) } }
    fun notice(message: String) { update { it.copy(message=message) } }
    fun chooseFolder(uri: Uri, copy: Boolean = false) = viewModelScope.launch { connect(uri,copy) }
    private suspend fun connect(uri: Uri,copy: Boolean) = transaction.withLock {
        update { it.copy(loading=true) }
        try {
            resolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            val next=FolderStore(resolver,uri); val scan=next.scan()
            if(copy) {
                require(scan.heads.isEmpty() && scan.invalid==0) { "Save copy needs an empty folder. Existing data was not changed." }
                val (revision,after)=next.save(mutable.value.data,emptySet())
                store=next
                update { it.copy(data=revision.data,heads=after.heads,unsaved=false,ready=true,blocked=after.conflicted,message="Copy saved") }
            } else {
                require(!mutable.value.unsaved) { "Unsaved changes retained. Retry save, save a copy, or explicitly discard before reopening." }
                store=next
                applyScan(scan)
            }
            prefs.edit().putString("folder",uri.toString()).apply()
            update { it.copy(folder=uri.toString()) }
        } catch(e: Exception) { update { it.copy(message=e.message ?: "Folder unavailable") } }
        finally { update { it.copy(loading=false) } }
    }
    private fun applyScan(scan: Scan) {
        val blocked=scan.conflicted || scan.missingParents
        update { it.copy(
            data=scan.heads.firstOrNull()?.data ?: if(scan.invalid>0) it.data else Portfolio(settings=it.data.settings),
            ready=true,heads=scan.heads,blocked=blocked,unsaved=false,
            message=when {
                scan.conflicted -> "Sync conflict: choose a revision below. Both originals will be preserved."
                scan.missingParents -> "Sync incomplete. Wait for the remaining files, then reload."
                scan.invalid>0 -> "Ignored ${scan.invalid} invalid or interrupted snapshots. Last valid data retained."
                else -> null
            },
        ) }
    }
    fun resume() {
        foreground=true
        if(store != null && !mutable.value.loading && !mutable.value.unsaved) reload()
    }
    fun background() { foreground=false; refreshJob?.cancel() }
    fun reload(discard: Boolean = false) = viewModelScope.launch {
        transaction.withLock {
            if(mutable.value.unsaved && !discard) { notice("Unsaved changes retained. Retry save or save a copy."); return@withLock }
            try { store?.scan()?.let { applyScan(it) } }
            catch(e: Exception) { update { it.copy(blocked=true,message=e.message ?: "Cannot reload folder") } }
        }
    }
    fun edit(transform: (Portfolio)->Portfolio,onSaved: ()->Unit = {}) = viewModelScope.launch {
        transaction.withLock {
            if(mutable.value.blocked || mutable.value.unsaved) { notice("Resolve storage issues before editing"); return@withLock }
            try { persist(transform(mutable.value.data).ranked().validate()); onSaved() }
            catch(e: Exception) { notice(e.message ?: "Could not save") }
        }
    }
    private suspend fun persist(data: Portfolio,resolve: Boolean=false) {
        val target=store ?: error("Choose a storage folder first")
        update { it.copy(data=data,saving=true,unsaved=true) }
        try {
            val (_,scan)=target.save(data,mutable.value.heads.map { it.id }.toSet(),resolve)
            update { it.copy(heads=scan.heads,unsaved=false,blocked=scan.conflicted || scan.missingParents,message=if(scan.conflicted) "Concurrent edit detected. Resolve the conflict." else "Saved on device") }
        } finally { update { it.copy(saving=false) } }
    }
    fun retrySave()=viewModelScope.launch { transaction.withLock {
        try { persist(mutable.value.data) } catch(e: Exception) { notice(e.message ?: "Save failed") }
    } }
    fun resolve(revision: Revision)=viewModelScope.launch { transaction.withLock {
        try { persist(revision.data.ranked(),true) } catch(e: Exception) { notice(e.message ?: "Conflict resolution failed") }
    } }
    fun refresh(bucketId: String?=null) {
        if(refreshJob?.isActive==true || !mutable.value.ready || mutable.value.blocked || mutable.value.unsaved || !foreground) return
        val requested=mutable.value.data
        // Nothing to value: never write a snapshot for an empty portfolio (e.g. a folder listing that came back empty).
        if(requested.buckets.isEmpty() && requested.goals.isEmpty()) return
        refreshJob=viewModelScope.launch {
            update { it.copy(refreshing=true,message="Refreshing selected providers…") }
            try {
                val observations=providers.refresh(requested,bucketId)
                transaction.withLock {
                    if(mutable.value.blocked || mutable.value.unsaved) { notice("Refresh finished; resolve storage issues before retrying."); return@withLock }
                    withContext(NonCancellable) { persist(mergeObservations(mutable.value.data,requested,observations.holdings,observations.quotes,observations.unlisted)) }
                    notice(if(observations.errors.isEmpty()) "Refreshed. Shared rates may revalue other buckets." else observations.errors.joinToString("\n"))
                }
            } catch(e: CancellationException) { notice("Refresh stopped. Cached values retained."); throw e }
            catch(e: Exception) { notice(e.message ?: "Refresh failed. Cached values retained.") }
            finally { update { it.copy(refreshing=false) } }
        }
    }
    /** Loads an address's tokens for the holding editor; persists nothing. */
    fun fetchTokens(asset: String,address: String,done: (List<Token>?,String?)->Unit) { viewModelScope.launch {
        val data=mutable.value.data; val source=data.settings.providers["$asset tokens"]
        if(asset==Chain.BTC.name) return@launch done(emptyList(),null)
        if(source==null || source=="Off") return@launch done(null,"Token source for $asset is Off. Choose one in Settings.")
        val stored=data.holdings.filter { it.asset==asset }.flatMap { it.tokens }
        val list=try { providers.tokens(Holding(bucketId="-",label="-",asset=asset,quantity=null,address=address),source).map { t -> stored.find { it.contract==t.contract }?.let { o -> t.copy(decimals=t.decimals ?: o.decimals,symbol=t.symbol.ifBlank { o.symbol },checkedAt=o.checkedAt) } ?: t } }
        catch(e: CancellationException) { throw e }
        catch(e: Exception) { return@launch done(null,e.safeMessage()) }
        done(list,null)
    } }
    fun saveKey(provider: String,value: String): Boolean = try { secrets.put(provider,value); notice("$provider key saved on this device"); true } catch(e: Exception) { notice(e.message ?: "Could not save key"); false }
    fun export(uri: Uri)=viewModelScope.launch {
        try {
            val text=encodeRevision(Revision(data=mutable.value.data))
            withContext(Dispatchers.IO) {
                resolver.openOutputStream(uri,"wt")?.use { it.write(text.toByteArray()); it.flush() } ?: error("Cannot write backup")
                val readback=resolver.openInputStream(uri)?.use { it.readLimited().toString(Charsets.UTF_8) } ?: error("Cannot verify backup")
                require(decodeRevision(readback).data==mutable.value.data) { "Backup verification failed; retry export" }
            }
            notice("Backup exported without provider keys")
        } catch(e: Exception) { notice(e.message ?: "Export failed") }
    }
    fun inspectRestore(uri: Uri)=viewModelScope.launch {
        try {
            val text=withContext(Dispatchers.IO) { resolver.openInputStream(uri)?.use { it.readLimited().toString(Charsets.UTF_8) } ?: error("Cannot read backup") }
            update { it.copy(restore=decodeRevision(text)) }
        } catch(e: Exception) { notice(e.message ?: "Invalid backup") }
    }
    fun cancelRestore() { update { it.copy(restore=null) } }
    fun restore() { val revision=mutable.value.restore ?: return; edit({ revision.data }) { cancelRestore() } }
}
