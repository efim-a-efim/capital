package dev.capital.domain

import java.math.BigInteger
import java.security.MessageDigest
import java.util.Base64
import org.bouncycastle.crypto.digests.KeccakDigest

private fun sha(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes)
private fun ByteArray.hex() = joinToString("") { "%02x".format(it.toInt() and 255) }
private fun base58(value: String): ByteArray {
    require(value.length in 26..60) { "Invalid address length" }
    val alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    var n = BigInteger.ZERO
    value.forEach { c -> val digit = alphabet.indexOf(c); require(digit >= 0) { "Invalid Base58 address" }; n = n * BigInteger.valueOf(58) + BigInteger.valueOf(digit.toLong()) }
    val raw = n.toByteArray().let { if (it.size > 1 && it[0] == 0.toByte()) it.drop(1).toByteArray() else it }
    return ByteArray(value.takeWhile { it == '1' }.length) + raw
}
private fun checked58(value: String): ByteArray {
    val bytes = base58(value)
    require(bytes.size == 25) { "Invalid address length" }
    val payload = bytes.copyOfRange(0, 21)
    require(sha(sha(payload)).take(4) == bytes.takeLast(4)) { "Invalid address checksum" }
    return payload
}
private fun crc16(bytes: ByteArray): Int {
    var crc = 0
    bytes.forEach { byte ->
        crc = crc xor ((byte.toInt() and 255) shl 8)
        repeat(8) { crc = if (crc and 0x8000 != 0) (crc shl 1) xor 0x1021 else crc shl 1; crc = crc and 0xffff }
    }
    return crc
}
fun String.unhex(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()
private fun base58Encode(bytes: ByteArray): String {
    val alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    var n = BigInteger(1, bytes); val out = StringBuilder()
    while (n.signum() > 0) { val (q, r) = n.divideAndRemainder(BigInteger.valueOf(58)); out.append(alphabet[r.toInt()]); n = q }
    return "1".repeat(bytes.takeWhile { it == 0.toByte() }.size) + out.reverse()
}
/** Provider-facing form of a canonical address: TRX Base58Check, TON bounceable url-safe base64. */
fun providerAddress(chain: Chain, canonical: String): String = when (chain) {
    Chain.TRX -> canonical.unhex().let { base58Encode(it + sha(sha(it)).copyOf(4)) }
    Chain.TON -> {
        val (wc, hash) = canonical.split(":")
        val head = byteArrayOf(0x11, wc.toInt().toByte()) + hash.unhex()
        val crc = crc16(head)
        Base64.getUrlEncoder().withoutPadding().encodeToString(head + byteArrayOf((crc ushr 8).toByte(), crc.toByte()))
    }
    else -> canonical
}
fun canonicalAddress(chain: Chain, input: String): String {
    val value = input.trim()
    require(value.length <= 120) { "Address too long" }
    return when (chain) {
        Chain.ETH -> {
            require(value.matches(Regex("0x[0-9a-fA-F]{40}"))) { "Enter a mainnet Ethereum address" }
            val body = value.substring(2)
            if (body != body.lowercase() && body != body.uppercase()) {
                val digest = KeccakDigest(256)
                val bytes = body.lowercase().toByteArray(Charsets.US_ASCII)
                digest.update(bytes, 0, bytes.size)
                val output = ByteArray(32); digest.doFinal(output, 0)
                val hash = output.hex()
                require(body.indices.all { i -> !body[i].isLetter() || body[i].isUpperCase() == (hash[i].digitToInt(16) >= 8) }) { "Invalid Ethereum checksum" }
            }
            value.lowercase()
        }
        Chain.TRX -> {
            val payload = if (value.matches(Regex("41[0-9a-fA-F]{40}"))) value.unhex() else checked58(value)
            require(payload[0] == 0x41.toByte()) { "Enter a TRON address" }
            payload.hex()
        }
        Chain.TON -> {
            if (value.matches(Regex("(-1|0):[0-9a-fA-F]{64}"))) value.lowercase() else {
                val bytes = runCatching { Base64.getUrlDecoder().decode(value.replace('+','-').replace('/','_')) }.getOrElse { error("Invalid TON address") }
                require(bytes.size == 36) { "Invalid TON address length" }
                val tag = bytes[0].toInt() and 255
                require(tag in listOf(0x11, 0x51)) { "Use a mainnet TON address" }
                require(crc16(bytes.copyOfRange(0, 34)) == ((bytes[34].toInt() and 255) shl 8 or (bytes[35].toInt() and 255))) { "Invalid TON checksum" }
                require(bytes[1].toInt() in listOf(-1, 0)) { "Unsupported TON workchain" }
                "${bytes[1]}:${bytes.copyOfRange(2,34).hex()}"
            }
        }
        Chain.BTC -> {
            if (value.lowercase().startsWith("bc1")) {
                require(value == value.lowercase() || value == value.uppercase()) { "Mixed-case Bitcoin address" }
                val lower = value.lowercase()
                require(lower.length in 14..90) { "Invalid Bitcoin address length" }
                val chars = "qpzry9x8gf2tvdw0s3jn54khce6mua7l"
                val data = lower.substring(3).map { chars.indexOf(it).also { v -> require(v >= 0) { "Invalid Bitcoin address" } } }
                var chk = 1
                val generators = intArrayOf(0x3b6a57b2,0x26508e6d,0x1ea119fa,0x3d4233dd,0x2a1462b3)
                (listOf(3,3,0,2,3) + data).forEach { v ->
                    val top = chk ushr 25; chk = ((chk and 0x1ffffff) shl 5) xor v
                    repeat(5) { if ((top ushr it) and 1 != 0) chk = chk xor generators[it] }
                }
                val version = data[0]
                require(version in 0..16 && chk == if (version == 0) 1 else 0x2bc830a3) { "Invalid Bitcoin checksum" }
                var acc = 0; var bits = 0; val program = mutableListOf<Int>()
                data.drop(1).dropLast(6).forEach { v -> acc = ((acc shl 5) or v) and 0xffff; bits += 5; while(bits >= 8) { bits -= 8; program += (acc ushr bits) and 255 } }
                require(bits < 5 && ((acc shl (8-bits)) and 255) == 0 && program.size in 2..40 && (version != 0 || program.size in listOf(20,32))) { "Invalid Bitcoin witness program" }
                lower
            } else {
                val payload = checked58(value)
                require(payload[0].toInt() in listOf(0,5)) { "Use a mainnet Bitcoin address" }
                value
            }
        }
    }
}
