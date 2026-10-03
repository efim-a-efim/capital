package dev.capital

import dev.capital.brokers.*
import dev.capital.domain.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** Contract every registered plugin must keep; a new plugin that breaks one fails here before it reaches a user. */
class BrokersTest {
    @Test fun registryIsConsistent() {
        val names=Brokers.all.map { it.name }
        assertEquals(names,names.distinct()); assertTrue(names.all { it.isNotBlank() && it.trim()==it && it.length<=40 })
        val keys=Brokers.all.flatMap { p -> p.credentials.map { it.key } }
        assertEquals(keys,keys.distinct()); assertTrue(keys.isNotEmpty())
        Brokers.all.forEach { p ->
            assertTrue(p.name,p.credentials.isNotEmpty()); assertTrue(p.name,p.site.startsWith("https://"))
            p.credentials.forEach { c -> assertTrue(c.key,c.label().isNotBlank()) }
            assertTrue(p.name,p.idLabel().isNotBlank()); assertTrue(p.name,p.idError().isNotBlank())
            assertFalse(p.name,p.idForm.matches("")); assertFalse(p.name,p.idForm.matches("12 34"))
            assertNull(p.name,Brokers.credential("missing-"+p.name)); assertEquals(p.credentials.first(),Brokers.credential(p.credentials.first().key))
        }
        assertEquals(names,brokerChoices); assertEquals(names,brokerCredentials.keys.toList())
        assertNull(Brokers.byName("Robinhood")); assertNull(Brokers.byName(null))
    }
    @Test fun idsAreNormalisedPerPlugin() {
        assertEquals("001-001-1234567-001",accountId("OANDA"," 001-001-1234567-001 ")); rejects { accountId("OANDA","0010011234567001") }
        assertEquals("8b5f262d-4bb9-365d-888a-202bd3b15fa1",accountId("SnapTrade","8B5F262D-4BB9-365D-888A-202BD3B15FA1"))
        assertEquals("42",accountId("Trading 212","42")); rejects { accountId("Trading 212","4-2") }
        assertEquals("123456",accountId("Interactive Brokers","123456")); rejects { accountId(null,"1") }; rejects { accountId("Saxo","live") }
        assertTrue(SnapTrade.listsAccounts); assertNotNull(SnapTrade.connectLabel()); assertFalse(Oanda.listsAccounts); assertNull(Oanda.connectLabel())
    }
    @Test fun pluginsWithoutExtrasUseTheDefaults()=runBlocking {
        val host=object: BrokerHost {
            override fun secret(key: String)=""
            override fun required(key: String,broker: String)=throw KeyArgumentStub(broker)
            override suspend fun send(url: String,body: String?,headers: Map<String,String>,soft: Set<Int>)=Response(200,"",emptyMap())
            override suspend fun json(url: String,body: String?,headers: Map<String,String>)=kotlinx.serialization.json.JsonObject(emptyMap())
        }
        assertEquals(emptyList<Pair<String,String>>(),Oanda.accounts(host)); assertEquals("No accounts found",Oanda.noAccounts())
        try { Oanda.connect(host); fail("Expected failure") } catch(_: UnsupportedOperationException) { }
        try { Oanda.read(host,"001-001-1234567-001"); fail("Expected failure") } catch(e: KeyArgumentStub) { assertEquals("OANDA",e.message) }
        assertEquals("bar",Response(200,"",mapOf("x-foo" to "bar")).header("X-Foo"))
    }
    private class KeyArgumentStub(message: String): RuntimeException(message)
    private fun rejects(block: ()->Unit) { try { block(); fail("Expected rejection") } catch(_: IllegalArgumentException) { } }
}
