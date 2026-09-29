package dev.capital

import dev.capital.data.*
import dev.capital.domain.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class MemFiles : SnapshotFiles {
    val map = linkedMapOf<String, String>()
    var creates = 0
    var failCreate = -1            // the Nth create throws without writing
    var corruptNew = false         // files created from now on read back damaged
    var failDeletes = 0            // this many deletes throw
    private val bad = mutableSetOf<String>()
    override fun list() = map.keys.filter { it.startsWith("capital-") && it.endsWith(".json") }.toList()
    override fun read(name: String): String = (map[name] ?: error("missing $name")).let { if (name in bad) it.dropLast(3) + "xyz" else it }
    override fun create(name: String, text: String) {
        creates++
        if (creates == failCreate) error("disk full")
        map[name] = text; if (corruptNew) bad += name
    }
    override fun delete(name: String) { if (failDeletes > 0) { failDeletes--; error("cannot delete") }; map.remove(name); bad -= name }
}

class StoreTest {
    private fun kdf(n: Int) = Kdf(m = 8192, salt = java.util.Base64.getEncoder().encodeToString(ByteArray(16) { (it + n).toByte() }))
    private val a = newKey("password A".toCharArray(), kdf(1))
    private val b = newKey("password B".toCharArray(), kdf(2))
    private val portfolio = Portfolio(buckets = listOf(Bucket("b", "Vacation Savings", "USD")))
    private val chain = (0..4).map { Revision(id = "r$it", parents = if (it == 0) emptyList() else listOf("r${it - 1}"), data = portfolio) }
    private fun plainFolder(extra: Map<String, String> = emptyMap()) = MemFiles().also { f ->
        chain.forEach { f.map["capital-${it.id}.json"] = encodeRevision(it) }; f.map.putAll(extra)
    }
    private fun metas(f: MemFiles) = f.list().map { meta(f.read(it)).let { m -> Triple(m.id, m.parents, m.schema) } }.sortedBy { it.first }
    private fun rev(f: MemFiles, name: String, key: DataKey?) = decodeRevision(f.read(name).let { if (isEncrypted(it)) decryptSnapshot(it, key!!) else it })
    private fun assertAll(f: MemFiles, target: Pair<DataKey, KeyHeader>?) {
        assertEquals(5, f.list().size)
        assertEquals(chain.map { it.id }, f.list().map { rev(f, it, target?.first) }.map { it.id }.sorted())
        assertEquals(chain, f.list().map { rev(f, it, target?.first) }.sortedBy { it.id })
        f.list().forEach { n ->
            val t = f.read(n)
            if (target == null) assertFalse(isEncrypted(t)) else { assertTrue(isEncrypted(t)); assertEquals(target.second, headerOf(t)); assertFalse(t.contains("Vacation")) }
        }
    }
    private fun heads(f: MemFiles, key: DataKey?, previous: DataKey? = null) = FolderStore(f, null).also { it.key = key; it.previous = previous }.let { runBlocking { it.scan() } }

    @Test fun plainSaveAndScan() = runBlocking {
        val store = FolderStore(MemFiles(), null)
        val (r1, _) = store.save(portfolio, emptySet())
        val (r2, scan) = store.save(portfolio.copy(buckets = listOf(Bucket("c", "Other", "EUR"))), setOf(r1.id))
        assertEquals(listOf(r2), scan.heads); assertEquals(listOf(r1.id), r2.parents); assertFalse(scan.conflicted)
    }
    @Test fun encryptedSaveAndScan() = runBlocking {
        val f = MemFiles(); val store = FolderStore(f, null).also { it.key = a.first; it.header = a.second }
        val (r1, _) = store.save(portfolio, emptySet())
        val text = f.map.values.single()
        assertTrue(isEncrypted(text)); assertFalse(text.contains("Vacation"))
        assertEquals(portfolio, store.scan().heads.single().data)
        store.save(portfolio, setOf(r1.id))
        val locked = FolderStore(f, null)
        assertThrows(Locked::class.java) { runBlocking { locked.scan() } }
        assertThrows(Locked::class.java) { runBlocking { locked.save(portfolio, setOf(r1.id)) } }
        assertEquals(2, f.map.size)
    }
    @Test fun rewriteToEncrypted() = runBlocking {
        val f = plainFolder(); val before = metas(f); val store = FolderStore(f, null)
        val seen = mutableListOf<Rewrite>()
        assertEquals(Rewrite(5, 5, 0), store.rewrite(a) { seen += it })
        assertEquals(5, seen.size)
        assertAll(f, a); assertEquals(before, metas(f))
        assertEquals(chain.last(), heads(f, a.first).heads.single())
        assertSame(a.first, store.key); assertEquals(a.second, store.header)
    }
    @Test fun rewriteToPlain() = runBlocking {
        val f = plainFolder(); val store = FolderStore(f, null)
        store.rewrite(a); val before = metas(f)
        store.rewrite(null); assertAll(f, null); assertEquals(before, metas(f)); assertNull(store.key)
    }
    @Test fun interruptionAtEveryStep() = runBlocking {
        for (k in 1..5) {
            val f = plainFolder(); val store = FolderStore(f, null); f.failCreate = k
            assertThrows(IllegalStateException::class.java) { runBlocking { store.rewrite(a) } }
            val ids = f.list().map { rev(f, it, a.first).id }.toSet()
            assertEquals(chain.map { it.id }.toSet(), ids)
            f.failCreate = -1; assertEquals(Rewrite(5, 5, 0), store.rewrite(a)); assertAll(f, a)
        }
        for (k in 1..5) { // and back
            val f = plainFolder(); val store = FolderStore(f, null); store.rewrite(a); f.creates = 0; f.failCreate = k
            assertThrows(IllegalStateException::class.java) { runBlocking { store.rewrite(null) } }
            assertEquals(chain.map { it.id }.toSet(), f.list().map { rev(f, it, a.first).id }.toSet())
            f.failCreate = -1; store.rewrite(null); assertAll(f, null)
        }
    }
    @Test fun verificationFailureKeepsSource() = runBlocking {
        val f = plainFolder(); val store = FolderStore(f, null); val names = f.map.keys.toSet(); f.corruptNew = true
        val e = assertThrows(IllegalStateException::class.java) { runBlocking { store.rewrite(a) } }
        assertTrue(e.message!!.contains("Original files were kept")); assertEquals(names, f.map.keys); assertTrue(f.map.values.none { isEncrypted(it) })
        f.corruptNew = false; store.rewrite(a); assertAll(f, a)
    }
    @Test fun deleteFailureLeavesBothForms() = runBlocking {
        val f = plainFolder(); val store = FolderStore(f, null); f.failDeletes = 1
        assertThrows(IllegalStateException::class.java) { runBlocking { store.rewrite(a) } }
        assertEquals(6, f.list().size)
        val scan = heads(f, a.first); assertEquals("r4", scan.heads.single().id); assertEquals(0, scan.invalid)
        store.rewrite(a); assertAll(f, a)
    }
    @Test fun passwordChange() = runBlocking {
        val f = plainFolder(); val store = FolderStore(f, null); store.rewrite(a)
        store.rewrite(b, previous = a.first); assertAll(f, b)
        f.list().forEach { assertThrows(WrongPassword::class.java) { unlock(headerOf(f.read(it)), "password A".toCharArray()) } }
        assertEquals(5, f.list().count { unlock(headerOf(f.read(it)), "password B".toCharArray()).bytes().contentEquals(b.first.bytes()) })
    }
    @Test fun interruptedPasswordChangeResumes() = runBlocking {
        val f = plainFolder(); val store = FolderStore(f, null); store.rewrite(a); f.creates = 0; f.failCreate = 3
        assertThrows(IllegalStateException::class.java) { runBlocking { store.rewrite(b, previous = a.first) } }
        assertEquals(2, f.list().map { headerOf(f.read(it)) }.distinct().size)
        val mid = FolderStore(f, null).also { it.key = b.first; it.previous = a.first }
        assertEquals(0, mid.scan().invalid); assertEquals(chain.last(), mid.scan().heads.single())
        assertEquals(2, mid.headers().size)
        f.failCreate = -1; mid.rewrite(b, previous = a.first); assertAll(f, b)
        assertEquals(b.second, mid.header); assertNull(mid.previous)
    }
    @Test fun unreadableFileUntouched() = runBlocking {
        val f = plainFolder(mapOf("capital-junk.json" to "garbage")); val store = FolderStore(f, null)
        assertEquals(Rewrite(5, 6, 1), store.rewrite(a)); assertEquals("garbage", f.map["capital-junk.json"])
        assertEquals(Rewrite(5, 6, 1), store.rewrite(null)); assertEquals("garbage", f.map["capital-junk.json"])
        assertEquals(6, f.map.size)
    }
    @Test fun formsAndHeaders() = runBlocking {
        val f = plainFolder(mapOf("capital-junk.json" to "garbage")); val store = FolderStore(f, null)
        assertEquals(Forms(5, 0, 1), store.forms()); assertNull(store.anyHeader())
        f.map["capital-r0.json"] = encryptSnapshot(encodeRevision(chain[0]), a.first, a.second)
        f.map["capital-r1.json"] = encryptSnapshot(encodeRevision(chain[1]), b.first, b.second)
        assertEquals(Forms(3, 2, 1), store.forms()); assertEquals(setOf(a.second, b.second), store.headers().toSet())
        assertEquals(a.second, store.headerFor(a.first))
        assertNull(store.headerFor(DataKey(ByteArray(32))))
    }
}
