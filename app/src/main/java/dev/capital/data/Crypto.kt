package dev.capital.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.nio.CharBuffer
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class WrongPassword : IllegalArgumentException("Wrong password")
class Locked : IllegalStateException("Encrypted data: password required")

@Serializable data class Kdf(val alg: String = "argon2id", val m: Int = 47104, val t: Int = 1, val p: Int = 1, val salt: String)
/** Everything needed to get the data key from a password. Stored in clear in every encrypted file. */
@Serializable data class KeyHeader(val kdf: Kdf, val wrapped: String)
@Serializable data class EncryptedFile(
    val enc: Int = 1, val id: String, val parents: List<String>, val schema: Int,
    val key: KeyHeader, val nonce: String, val ciphertext: String,
)
/** Clear metadata readable without any key, for both plaintext and encrypted snapshot files. */
data class Meta(val id: String, val parents: List<String>, val schema: Int, val encrypted: Boolean)

class DataKey(bytes: ByteArray) {
    private val key = bytes.copyOf()
    fun bytes(): ByteArray = key.copyOf()
    fun wipe() = key.fill(0)
}

private val rng = SecureRandom()
private const val BAD_PARAMS = "Unsupported encryption parameters"
private fun b64(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)
private fun unb64(text: String): ByteArray = Base64.getDecoder().decode(text)
private fun randomBytes(n: Int) = ByteArray(n).also { rng.nextBytes(it) }

private fun kdfAad(k: Kdf) = "${k.alg}\n${k.m}\n${k.t}\n${k.p}\n${k.salt}"
private fun keyAad(k: Kdf) = "capital-key-1\n${kdfAad(k)}".toByteArray()
private fun fileAad(f: EncryptedFile) =
    "capital-enc-1\n${f.id}\n${f.parents.joinToString(",")}\n${f.schema}\n${kdfAad(f.key.kdf)}\n${f.key.wrapped}".toByteArray()

private fun gcm(mode: Int, key: ByteArray, nonce: ByteArray, aad: ByteArray, input: ByteArray): ByteArray =
    Cipher.getInstance("AES/GCM/NoPadding").apply {
        init(mode, SecretKeySpec(key, "AES"), GCMParameterSpec(128, nonce)); updateAAD(aad)
    }.doFinal(input)

private fun checkKdf(k: Kdf) {
    val salt = runCatching { unb64(k.salt).size }.getOrDefault(0)
    require(k.alg == "argon2id" && k.m in 8192..262144 && k.t in 1..10 && k.p in 1..4 && salt in 16..64) { BAD_PARAMS }
}

private fun utf8(password: CharArray): ByteArray {
    val buffer = Charsets.UTF_8.encode(CharBuffer.wrap(password))
    return ByteArray(buffer.remaining()).also { buffer.get(it); buffer.array().fill(0) }
}

fun derive(password: CharArray, kdf: Kdf): ByteArray {
    checkKdf(kdf)
    val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id).withVersion(Argon2Parameters.ARGON2_VERSION_13)
        .withIterations(kdf.t).withMemoryAsKB(kdf.m).withParallelism(kdf.p).withSalt(unb64(kdf.salt)).build()
    val pw = utf8(password)
    try { return ByteArray(32).also { Argon2BytesGenerator().apply { init(params) }.generateBytes(pw, it) } } finally { pw.fill(0) }
}

fun newKey(password: CharArray, kdf: Kdf = Kdf(salt = b64(randomBytes(16)))): Pair<DataKey, KeyHeader> {
    val data = randomBytes(32); val pk = derive(password, kdf); val nonce = randomBytes(12)
    try {
        val wrapped = nonce + gcm(Cipher.ENCRYPT_MODE, pk, nonce, keyAad(kdf), data)
        return DataKey(data) to KeyHeader(kdf, b64(wrapped))
    } finally { pk.fill(0); data.fill(0) }
}

fun unlock(header: KeyHeader, password: CharArray): DataKey {
    checkKdf(header.kdf)
    val wrapped = unb64(header.wrapped)
    require(wrapped.size > 12) { "Invalid key header" }
    val pk = derive(password, header.kdf)
    try {
        val data = try { gcm(Cipher.DECRYPT_MODE, pk, wrapped.copyOfRange(0, 12), keyAad(header.kdf), wrapped.copyOfRange(12, wrapped.size)) }
            catch (_: java.security.GeneralSecurityException) { throw WrongPassword() }
        try { require(data.size == 32) { "Invalid key header" }; return DataKey(data) } finally { data.fill(0) }
    } finally { pk.fill(0) }
}

// ponytail: sniffs the top-level "enc" field only; a hostile plaintext file with "enc" just fails as encrypted, fine
fun isEncrypted(text: String): Boolean = runCatching { "enc" in json.parseToJsonElement(text).jsonObject }.getOrDefault(false)

private fun parseEncrypted(text: String): EncryptedFile {
    require(text.toByteArray().size <= MAX_FILE_BYTES * 2) { "File too large" }
    val f = json.decodeFromString<EncryptedFile>(text)
    require(f.enc == 1) { "Unsupported encryption version" }
    checkKdf(f.key.kdf)
    return f
}

fun headerOf(text: String): KeyHeader = parseEncrypted(text).key

fun meta(text: String): Meta {
    if (isEncrypted(text)) return parseEncrypted(text).let { Meta(it.id, it.parents, it.schema, true) }
    require(text.toByteArray().size <= MAX_FILE_BYTES) { "File too large" }
    val payload = json.parseToJsonElement(json.decodeFromString<Envelope>(text).payload).jsonObject
    return Meta(
        payload.getValue("id").jsonPrimitive.content,
        payload.getValue("parents").jsonArray.map { it.jsonPrimitive.content },
        payload.getValue("schema").jsonPrimitive.int, false,
    )
}

fun encryptSnapshot(plain: String, key: DataKey, header: KeyHeader): String {
    val m = meta(plain); val nonce = randomBytes(12)
    val base = EncryptedFile(id = m.id, parents = m.parents, schema = m.schema, key = header, nonce = b64(nonce), ciphertext = "")
    val k = key.bytes()
    try { return json.encodeToString(base.copy(ciphertext = b64(gcm(Cipher.ENCRYPT_MODE, k, nonce, fileAad(base), plain.toByteArray())))) } finally { k.fill(0) }
}

fun decryptSnapshot(text: String, key: DataKey): String {
    val f = parseEncrypted(text)
    val nonce = unb64(f.nonce)
    require(nonce.size == 12) { "Invalid snapshot" }
    val k = key.bytes()
    val plain = try { String(gcm(Cipher.DECRYPT_MODE, k, nonce, fileAad(f), unb64(f.ciphertext))) }
        catch (_: java.security.GeneralSecurityException) { throw IllegalArgumentException("Snapshot authentication failed") }
        finally { k.fill(0) }
    require(meta(plain) == Meta(f.id, f.parents, f.schema, false)) { "Snapshot header mismatch" }
    return plain
}

/** One data key protected by another, e.g. the previous key kept until a password change has rewritten every file. */
fun wrapKey(inner: DataKey, outer: DataKey): String {
    val i = inner.bytes(); val o = outer.bytes(); val nonce = randomBytes(12)
    try { return b64(nonce + gcm(Cipher.ENCRYPT_MODE, o, nonce, "capital-wrap-1".toByteArray(), i)) } finally { i.fill(0); o.fill(0) }
}
fun unwrapKey(wrapped: String, outer: DataKey): DataKey {
    val w = unb64(wrapped); require(w.size > 12) { "Invalid wrapped key" }
    val o = outer.bytes()
    val data = try { gcm(Cipher.DECRYPT_MODE, o, w.copyOfRange(0, 12), "capital-wrap-1".toByteArray(), w.copyOfRange(12, w.size)) }
        catch (_: java.security.GeneralSecurityException) { throw IllegalArgumentException("Invalid wrapped key") }
        finally { o.fill(0) }
    try { require(data.size == 32) { "Invalid wrapped key" }; return DataKey(data) } finally { data.fill(0) }
}

fun passwordDelayMillis(random: SecureRandom = SecureRandom()): Long = 1000L + random.nextInt(4001)

fun validPassword(password: CharArray): String? = when {
    password.all { it.isWhitespace() } -> "Password must not be blank"
    password.size < 8 -> "Password must be at least 8 characters"
    password.size > 256 -> "Password must be at most 256 characters"
    else -> null
}
