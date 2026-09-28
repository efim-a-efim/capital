package dev.capital.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import dev.capital.domain.Portfolio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class FolderStore(private val resolver: ContentResolver, val tree: Uri) {
    private val lock = Mutex()
    private val root get() = DocumentsContract.buildDocumentUriUsingTree(tree,DocumentsContract.getTreeDocumentId(tree))
    private fun documents(): List<Uri> {
        val children=DocumentsContract.buildChildDocumentsUriUsingTree(tree,DocumentsContract.getTreeDocumentId(tree))
        return resolver.query(children,arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME),null,null,null)?.use { c ->
            buildList { while(c.moveToNext()) if(c.getString(1).startsWith("capital-") && c.getString(1).endsWith(".json")) add(DocumentsContract.buildDocumentUriUsingTree(tree,c.getString(0))) }
        } ?: error("Cannot read folder. Reconnect it in Settings.")
    }
    fun read(uri: Uri): String = resolver.openInputStream(uri)?.use {
        it.readLimited().toString(Charsets.UTF_8)
    } ?: error("Cannot read file")
    // ponytail: scan retained snapshots; add an index when measured startup cost needs it.
    private fun scanNow(): Scan = scanRevisions(documents().map { try { read(it) } catch(_: Exception) { "invalid snapshot" } })
    suspend fun scan() = withContext(Dispatchers.IO) { lock.withLock { scanNow() } }
    suspend fun save(data: Portfolio, expected: Set<String>, resolve: Boolean = false): Pair<Revision,Scan> = withContext(Dispatchers.IO) {
        lock.withLock {
            val before=scanNow()
            require(!before.missingParents) { "Sync incomplete: some parent revisions are missing. Finish syncing first." }
            require(before.heads.map { it.id }.toSet()==expected) { "Folder changed. Reload before saving; your edits are retained." }
            require(resolve || !before.conflicted) { "Resolve the folder conflict before editing" }
            val revision=Revision(parents=expected.sorted(),data=data.validate())
            val encoded=encodeRevision(revision)
            val file=DocumentsContract.createDocument(resolver,root,"application/json","capital-${revision.id}.json") ?: error("Cannot create snapshot")
            resolver.openOutputStream(file,"w")?.use { it.write(encoded.toByteArray()); it.flush() } ?: error("Cannot write snapshot")
            require(decodeRevision(read(file))==revision) { "Snapshot verification failed. Previous data is safe." }
            revision to scanNow()
        }
    }
}
