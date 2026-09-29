package dev.capital

import dev.capital.data.*
import dev.capital.domain.*
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test

class CryptoTest {
    private val small = Kdf(m = 8192, t = 1, p = 1, salt = "AAECAwQFBgcICQoLDA0ODw==")
    private val pw = "correct horse".toCharArray()
    private val portfolio = Portfolio(
        buckets = listOf(Bucket("b", "Vacation Savings", "USD")),
        holdings = listOf(Holding("h", "b", "Ledger Nano", "BTC", "1234567.89", "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa")),
        goals = listOf(Goal("g", "Boat", "777888.5", "USD", "2030-01-01")), connections = listOf(Connection("g", "b")),
    )
    private val revision = Revision(id = "r2", parents = listOf("r0", "r1"), data = portfolio)
    private val plain = encodeRevision(revision)
    private fun setup() = newKey(pw, small).let { (k, h) -> Triple(k, h, encryptSnapshot(plain, k, h)) }
    private fun parse(t: String) = json.decodeFromString<EncryptedFile>(t)
    private fun show(f: EncryptedFile) = json.encodeToString(f)
    private fun flip(s: String): String { val i = s.length / 2; return s.substring(0, i) + (if (s[i] == 'A') 'B' else 'A') + s.substring(i + 1) }
    private fun fails(block: () -> Unit) { assertThrows(IllegalArgumentException::class.java) { block() } }

    @Test fun roundTrip() {
        val (k, _, enc) = setup()
        assertTrue(isEncrypted(enc)); assertFalse(isEncrypted(plain))
        assertEquals(Meta("r2", listOf("r0", "r1"), SCHEMA, true), meta(enc))
        val back = decryptSnapshot(enc, k)
        assertEquals(plain, back); assertEquals(portfolio, decodeRevision(back).data)
    }
    @Test fun nothingReadable() {
        val enc = setup().third
        listOf("Vacation", "Savings", "Ledger", "1A1zP1eP", "1234567", "777888", "Boat").forEach { assertFalse(it, enc.contains(it)) }
    }
    @Test fun tamperFileFields() {
        val (k, _, enc) = setup(); val f = parse(enc)
        listOf(
            f.copy(ciphertext = flip(f.ciphertext)), f.copy(nonce = flip(f.nonce)), f.copy(id = "r3"),
            f.copy(parents = listOf("r0", "r9")), f.copy(schema = f.schema - 1),
            f.copy(key = f.key.copy(kdf = f.key.kdf.copy(salt = flip(f.key.kdf.salt)))),
            f.copy(key = f.key.copy(kdf = f.key.kdf.copy(m = 8193))), f.copy(key = f.key.copy(wrapped = flip(f.key.wrapped))),
        ).forEach { t -> fails { decryptSnapshot(show(t), k) } }
    }
    @Test fun tamperKeyFields() {
        val h = setup().second
        listOf(h.copy(wrapped = flip(h.wrapped)), h.copy(kdf = h.kdf.copy(salt = flip(h.kdf.salt))), h.copy(kdf = h.kdf.copy(m = 8193)), h.copy(kdf = h.kdf.copy(t = 2)))
            .forEach { t -> fails { unlock(t, pw) } }
    }
    @Test fun clearIdSwapRejected() {
        val (k, h, _) = setup(); val other = encodeRevision(Revision(id = "zz", data = portfolio))
        fails { decryptSnapshot(show(parse(encryptSnapshot(other, k, h)).copy(id = "r2")), k) }
    }
    @Test fun wrongPassword() {
        val (_, h, enc) = setup()
        assertThrows(WrongPassword::class.java) { unlock(h, "wrong password".toCharArray()) }
        assertEquals(plain, decryptSnapshot(enc, unlock(h, pw)))
    }
    @Test fun nonceFreshness() {
        val (k, h, a) = setup(); val b = encryptSnapshot(plain, k, h)
        assertNotEquals(a, b); assertNotEquals(parse(a).nonce, parse(b).nonce)
        assertEquals(plain, decryptSnapshot(a, k)); assertEquals(plain, decryptSnapshot(b, k))
    }
    @Test fun secondDevice() {
        val enc = setup().third
        assertEquals(plain, decryptSnapshot(enc, unlock(headerOf(enc), pw)))
    }
    @Test fun passwordChange() {
        val (k, _, enc) = setup(); val text = decryptSnapshot(enc, k)
        val newPw = "another password".toCharArray()
        val (k2, h2) = newKey(newPw, small.copy(salt = "EBESExQVFhcYGRobHB0eHw=="))
        val enc2 = encryptSnapshot(text, k2, h2)
        assertThrows(WrongPassword::class.java) { unlock(headerOf(enc2), pw) }
        assertEquals(plain, decryptSnapshot(enc2, unlock(headerOf(enc2), newPw)))
        assertEquals(meta(enc), meta(enc2))
        fails { decryptSnapshot(enc2, k) }
    }
    @Test fun metaPlainEqualsEncrypted() {
        assertEquals(meta(plain), meta(setup().third).copy(encrypted = false))
        assertEquals(Meta("r2", listOf("r0", "r1"), SCHEMA, false), meta(plain))
    }
    @Test fun kdfLimitsRejectedFast() {
        val (k, _, enc) = setup(); val f = parse(enc)
        listOf(small.copy(m = 1024), small.copy(t = 100), small.copy(alg = "pbkdf2"), small.copy(p = 9), small.copy(salt = "AAEC")).forEach { kdf ->
            val start = System.nanoTime()
            val e = assertThrows(IllegalArgumentException::class.java) { decryptSnapshot(show(f.copy(key = f.key.copy(kdf = kdf))), k) }
            assertEquals("Unsupported encryption parameters", e.message)
            assertThrows(IllegalArgumentException::class.java) { unlock(f.key.copy(kdf = kdf), pw) }
            assertTrue((System.nanoTime() - start) / 1_000_000 < 200)
        }
    }
    @Test fun delayRange() {
        val rnd = java.security.SecureRandom()
        val d = List(10_000) { passwordDelayMillis(rnd) }
        assertTrue(d.all { it in 1000..5000 }); assertTrue(d.any { it < 3000 }); assertTrue(d.any { it > 3000 })
    }
    @Test fun passwordRules() {
        assertNull(validPassword("12345678".toCharArray())); assertNull(validPassword("a".repeat(256).toCharArray()))
        assertNotNull(validPassword("1234567".toCharArray())); assertNotNull(validPassword("a".repeat(257).toCharArray()))
        assertNotNull(validPassword("         ".toCharArray())); assertNotNull(validPassword(CharArray(0)))
    }
    @Test fun defaultKdfTiming() {
        val start = System.nanoTime()
        assertEquals(32, derive(pw, Kdf(salt = "AAECAwQFBgcICQoLDA0ODw==")).size)
        println("ARGON2_DEFAULT_MS=${(System.nanoTime() - start) / 1_000_000}")
    }
    @Test fun dataKeyWipeAndCopies() {
        val src = ByteArray(32) { 7 }; val k = DataKey(src)
        src.fill(0); assertEquals(7, k.bytes()[0].toInt())
        k.bytes().fill(1); assertEquals(7, k.bytes()[0].toInt())
        k.wipe(); assertTrue(k.bytes().all { it.toInt() == 0 })
    }
    @Test fun wrappedKeyRoundTripAndTamper() {
        val a=DataKey(ByteArray(32) { 1 }); val b=DataKey(ByteArray(32) { 2 })
        val w=wrapKey(a,b)
        assertArrayEquals(a.bytes(),unwrapKey(w,b).bytes())
        assertThrows(IllegalArgumentException::class.java) { unwrapKey(w,a) }
        assertNotEquals(w,wrapKey(a,b))
    }
}
