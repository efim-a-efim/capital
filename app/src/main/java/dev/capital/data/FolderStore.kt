package dev.capital.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import dev.capital.domain.Portfolio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.security.SecureRandom

/** Snapshot files of one folder by name: SAF in production, in memory in tests. */
interface SnapshotFiles {
    fun list(): List<String>
    fun read(name: String): String
    fun create(name: String, text: String)
    fun delete(name: String)
}
class SafFiles(private val resolver: ContentResolver, private val tree: Uri) : SnapshotFiles {
    private val root get() = DocumentsContract.buildDocumentUriUsingTree(tree,DocumentsContract.getTreeDocumentId(tree))
    private var ids = mapOf<String,String>()
    override fun list(): List<String> {
        val children=DocumentsContract.buildChildDocumentsUriUsingTree(tree,DocumentsContract.getTreeDocumentId(tree))
        ids=resolver.query(children,arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME),null,null,null)?.use { c ->
            buildMap { while(c.moveToNext()) if(c.getString(1).startsWith("capital-") && c.getString(1).endsWith(".json")) put(c.getString(1),c.getString(0)) }
        } ?: error("Cannot read folder. Reconnect it in Settings.")
        return ids.keys.toList()
    }
    private fun doc(name: String): Uri {
        if(name !in ids) list()
        return DocumentsContract.buildDocumentUriUsingTree(tree,ids[name] ?: error("Cannot find $name"))
    }
    override fun read(name: String): String = resolver.openInputStream(doc(name))?.use {
        it.readLimited(MAX_FILE_BYTES*2).toString(Charsets.UTF_8)
    } ?: error("Cannot read file")
    override fun create(name: String, text: String) {
        val file=DocumentsContract.createDocument(resolver,root,"application/json",name) ?: error("Cannot create snapshot")
        try { resolver.openOutputStream(file,"w")?.use { it.write(text.toByteArray()); it.flush() } ?: error("Cannot write snapshot") }
        catch(e: Exception) { runCatching { DocumentsContract.deleteDocument(resolver,file) }; throw e }
    }
    override fun delete(name: String) {
        check(DocumentsContract.deleteDocument(resolver,doc(name))) { "Cannot delete $name" }
        ids=ids-name
    }
}

data class Forms(val plain: Int, val encrypted: Int, val unreadable: Int)
/** skipped = unreadable files left untouched. */
data class Rewrite(val done: Int, val total: Int, val skipped: Int)

class FolderStore(private val files: SnapshotFiles, val tree: Uri?) {
    constructor(resolver: ContentResolver, tree: Uri) : this(SafFiles(resolver,tree),tree)
    private val lock = Mutex()
    /** Set together by the model; null key = plaintext folder or locked. */
    var key: DataKey? = null
    var header: KeyHeader? = null
    /** Key of an unfinished password change; opens the files still in the old form. */
    var previous: DataKey? = null
    private fun texts(): List<String> = files.list().map { try { files.read(it) } catch(_: Exception) { "invalid snapshot" } }
    private fun plainOf(text: String): String {
        if(!isEncrypted(text)) return text
        val k=key ?: throw Locked()
        return try { decryptSnapshot(text,k) } catch(e: Exception) { decryptSnapshot(text,previous ?: throw e) }
    }
    // ponytail: scan retained snapshots; add an index when measured startup cost needs it.
    private fun scanNow(): Scan = scanRevisions(texts().map { try { plainOf(it) } catch(e: Locked) { throw e } catch(_: Exception) { "invalid snapshot" } })
    suspend fun scan() = withContext(Dispatchers.IO) { lock.withLock { scanNow() } }
    suspend fun forms(): Forms = withContext(Dispatchers.IO) { lock.withLock {
        val kinds=texts().map { runCatching { meta(it).encrypted }.getOrNull() }
        Forms(kinds.count { it==false },kinds.count { it==true },kinds.count { it==null })
    } }
    // ponytail: no ordering by age; any distinct header opens the folder. Tries each.
    suspend fun headers(): List<KeyHeader> = withContext(Dispatchers.IO) { lock.withLock {
        texts().mapNotNull { runCatching { if(isEncrypted(it)) headerOf(it) else null }.getOrNull() }.distinct()
    } }
    /** Header of a file that this key opens (a folder may briefly hold two headers during a password change). */
    suspend fun headerFor(key: DataKey): KeyHeader? = withContext(Dispatchers.IO) { lock.withLock {
        texts().firstNotNullOfOrNull { t -> runCatching { if(isEncrypted(t)) { decryptSnapshot(t,key); headerOf(t) } else null }.getOrNull() }
    } }
    suspend fun anyHeader(): KeyHeader? = headers().firstOrNull()
    suspend fun save(data: Portfolio, expected: Set<String>, resolve: Boolean = false): Pair<Revision,Scan> = withContext(Dispatchers.IO) {
        lock.withLock {
            val before=scanNow() // throws Locked before anything is written into an encrypted folder
            require(!before.missingParents) { "Sync incomplete: some parent revisions are missing. Finish syncing first." }
            require(before.heads.map { it.id }.toSet()==expected) { "Folder changed. Reload before saving; your edits are retained." }
            require(resolve || !before.conflicted) { "Resolve the folder conflict before editing" }
            val revision=Revision(parents=expected.sorted(),data=data.validate())
            val k=key; val h=header
            val encoded=encodeRevision(revision).let { if(k!=null && h!=null) encryptSnapshot(it,k,h) else it }
            val name="capital-${revision.id}.json"
            files.create(name,encoded)
            val back=files.read(name)
            require(decodeRevision(if(k!=null) decryptSnapshot(back,k) else back)==revision) { "Snapshot verification failed. Previous data is safe." }
            revision to scanNow()
        }
    }
    private fun form(plain: String, target: Pair<DataKey,KeyHeader>?) = if(target==null) plain else encryptSnapshot(plain,target.first,target.second)
    /**
     * Rewrites every file into the target form (null = plaintext). Each new file is verified before its source is deleted,
     * so every revision stays readable. Unreadable files are skipped and untouched. Safe to run again after a failure.
     */
    suspend fun rewrite(target: Pair<DataKey,KeyHeader>?, previous: DataKey? = null, progress: (Rewrite)->Unit = {}): Rewrite = withContext(Dispatchers.IO) {
        lock.withLock {
            val keys=listOfNotNull(key,previous,this@FolderStore.previous,target?.first)
            fun open(text: String): String? = if(!isEncrypted(text)) text else keys.firstNotNullOfOrNull { runCatching { decryptSnapshot(text,it) }.getOrNull() }
            class Info(val name: String, val plain: String?, val revision: Revision?, val inTarget: Boolean)
            val infos=files.list().sorted().map { name ->
                try {
                    val text=files.read(name); val plain=open(text); val revision=plain?.let { decodeRevision(it) }
                    Info(name,plain,revision,revision!=null && if(target==null) !isEncrypted(text) else isEncrypted(text) && headerOf(text)==target.second)
                } catch(_: Exception) { Info(name,null,null,false) }
            }
            var done=0; var skipped=0
            val have=infos.filter { it.inTarget }.mapNotNull { it.revision }.toMutableSet()
            for(i in infos) {
                val revision=i.revision
                if(revision==null || i.plain==null) skipped++
                else if(!i.inTarget) {
                    // a verified copy in the target form already exists (interrupted delete): only the source goes
                    if(revision !in have) {
                        val name="capital-${revision.id}-${randomHex()}.json"
                        try {
                            files.create(name,form(i.plain,target))
                            val back=files.read(name)
                            check(runCatching { decodeRevision(if(target==null) back else decryptSnapshot(back,target.first)) }.getOrNull()==revision) { "Verification of a rewritten snapshot failed. Original files were kept." }
                        } catch(e: Exception) { runCatching { files.delete(name) }; throw e }
                        have+=revision
                    }
                    files.delete(i.name); done++
                } else done++
                progress(Rewrite(done,infos.size,skipped))
            }
            key=target?.first; header=target?.second; this@FolderStore.previous=null
            Rewrite(done,infos.size,skipped)
        }
    }
    private fun randomHex() = ByteArray(4).also { SecureRandom().nextBytes(it) }.joinToString("") { "%02x".format(it) }
}
