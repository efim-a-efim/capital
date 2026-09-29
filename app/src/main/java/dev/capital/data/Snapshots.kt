package dev.capital.data

import dev.capital.domain.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int
import java.security.MessageDigest

val json = Json { encodeDefaults = true; ignoreUnknownKeys = false }
const val SCHEMA = 3
const val MAX_FILE_BYTES = 8 * 1024 * 1024
class FutureSchema : IllegalArgumentException("This folder needs a newer Capital version. No data was changed.")
@Serializable data class Revision(val schema: Int = SCHEMA, val id: String = id(), val parents: List<String> = emptyList(), val createdAt: Long = System.currentTimeMillis(), val data: Portfolio)
@Serializable data class Envelope(val payload: String, val sha256: String)
fun checksum(value: String) = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it.toInt() and 255) }
fun encodeRevision(revision: Revision): String {
    revision.data.validate()
    val payload = json.encodeToString(revision)
    return json.encodeToString(Envelope(payload,checksum(payload))).also { require(it.toByteArray().size <= MAX_FILE_BYTES) { "Snapshot too large" } }
}
fun decodeRevision(text: String): Revision {
    require(text.toByteArray().size <= MAX_FILE_BYTES) { "File too large" }
    val envelope = json.decodeFromString<Envelope>(text)
    require(checksum(envelope.payload) == envelope.sha256) { "Snapshot checksum mismatch" }
    val schema = json.parseToJsonElement(envelope.payload).jsonObject.getValue("schema").jsonPrimitive.int
    if(schema > SCHEMA) throw FutureSchema()
    require(schema in 1..SCHEMA) { "Unsupported snapshot schema" }
    return json.decodeFromString<Revision>(envelope.payload).let { r ->
        // schema 1 lacks the token source keys; a no-op when all keys exist
        r.copy(data=r.data.copy(settings=r.data.settings.copy(providers=providerChoices.mapValues { it.value.first() }+r.data.settings.providers)))
    }.let { r -> if(schema < 3) r.copy(data=r.data.ranked()) else r }.also {
        require(it.id.length in 1..100 && it.id !in it.parents && it.parents.distinct().size == it.parents.size && it.parents.size <= 1000) { "Invalid revision ancestry" }
        it.data.validate()
    }
}
data class Scan(val heads: List<Revision>, val invalid: Int, val missingParents: Boolean) {
    val conflicted get() = heads.size > 1
}
fun scanRevisions(files: List<String>): Scan {
    var invalid = 0
    val revisions = files.mapNotNull {
        try { decodeRevision(it) } catch (e: FutureSchema) { throw e } catch (_: Exception) { invalid++; null }
    }
    val byId = revisions.groupBy { it.id }
    require(byId.values.all { list -> list.distinct().size == 1 }) { "Different snapshots reuse a revision ID" }
    val unique = byId.mapValues { it.value.first() }
    val parents = unique.values.flatMap { it.parents }.toSet()
    val heads = unique.values.filter { it.id !in parents }.sortedBy { it.id }
    require(unique.isEmpty() || heads.isNotEmpty()) { "Invalid revision cycle" }
    val children = unique.keys.associateWith { mutableListOf<String>() }
    val remaining = unique.mapValues { (_,r) -> r.parents.count { it in unique } }.toMutableMap()
    unique.values.forEach { r -> r.parents.forEach { children[it]?.add(r.id) } }
    val ready = ArrayDeque(remaining.filterValues { it == 0 }.keys)
    var visited = 0
    while (ready.isNotEmpty()) {
        val id = ready.removeFirst(); visited++
        children.getValue(id).forEach { child ->
            remaining[child] = remaining.getValue(child) - 1
            if (remaining.getValue(child) == 0) ready.add(child)
        }
    }
    require(visited == unique.size) { "Invalid revision cycle" }
    return Scan(heads,invalid,parents.any { it !in unique })
}

fun java.io.InputStream.readLimited(): ByteArray {
    val output = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    while (true) {
        val count = read(buffer)
        if (count < 0) break
        require(output.size() + count <= MAX_FILE_BYTES) { "File or response too large" }
        output.write(buffer,0,count)
    }
    return output.toByteArray()
}
