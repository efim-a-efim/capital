package dev.capital

import dev.capital.data.*
import dev.capital.domain.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerializationException
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ProviderTest {
    private fun client(retryAfter: String="0",reply: (Request)->Pair<Int,String>)=OkHttpClient.Builder().addInterceptor { chain ->
        val (status,body)=reply(chain.request())
        Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(status).message("test").header("Retry-After",retryAfter).body(body.toResponseBody()).build()
    }.build()
    @Test fun partialQuoteFailurePreservesSuccessfulObservations()=runBlocking {
        val p=Portfolio(buckets=listOf(Bucket("b","Cash","USD")),holdings=listOf(Holding("eur","b","Euro","EUR","10"),Holding("rsd","b","Dinar","RSD","10")),quotes=listOf(Quote("RSD","0.01","old",1000,1000)),settings=Settings("USD"))
        val provider=Providers({ "" },client { 200 to """[{"base":"USD","quote":"EUR","date":"2026-09-28","rate":0.8}]""" })
        val result=provider.refresh(p,null)
        assertEquals(2,result.quotes.size)
        assertEquals("1.25",result.quotes.first { it.asset=="EUR" }.usd)
        assertNotNull(result.quotes.first { it.asset=="RSD" }.error)
        assertEquals("0.01",result.quotes.first { it.asset=="RSD" }.usd)
        assertEquals(1,result.errors.size)
    }
    @Test fun transientFailuresRetryButAuthenticationDoesNot()=runBlocking {
        val h=Holding("h","b","BTC","BTC",null,"1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa")
        val attempts=AtomicInteger()
        val provider=Providers({ "" },client {
            if(attempts.incrementAndGet()<3) 429 to "{}" else 200 to """{"chain_stats":{"funded_txo_sum":100,"spent_txo_sum":0}}"""
        })
        assertEquals("0.000001",provider.balance(h,"Blockstream").quantity)
        assertEquals(3,attempts.get())
        attempts.set(0)
        val denied=Providers({ "" },client { attempts.incrementAndGet(); 401 to "secret provider detail" })
        try { denied.balance(h,"Blockstream"); fail("Expected access error") } catch(e: ProviderFailure) { assertFalse(e.message!!.contains("secret")) }
        assertEquals(1,attempts.get())
    }
    @Test fun cancellationStopsNetworkBeforeDispatch()=runBlocking {
        val attempts=AtomicInteger()
        val provider=Providers({ "" },client { attempts.incrementAndGet(); 200 to "{}" })
        val h=Holding("h","b","BTC","BTC",null,"1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa")
        try { withTimeout(30) { provider.balance(h,"Blockstream") }; fail("Expected cancellation") } catch(_: TimeoutCancellationException) { }
        assertEquals(0,attempts.get())
    }
    private val btc=Holding("h","b","BTC","BTC",null,"1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa")
    @Test fun malformedResponseIsRejectedWithoutRetry()=runBlocking {
        val attempts=AtomicInteger()
        val provider=Providers({ "" },client { attempts.incrementAndGet(); 200 to "not json" })
        try { provider.balance(btc,"Blockstream"); fail("Expected failure") } catch(_: kotlinx.serialization.SerializationException) { }
        assertEquals(1,attempts.get())
    }
    @Test fun timeoutRetriesAreBoundedToThreeAttempts()=runBlocking {
        val attempts=AtomicInteger()
        val provider=Providers({ "" },OkHttpClient.Builder().addInterceptor { attempts.incrementAndGet(); throw java.net.SocketTimeoutException("timeout") }.build())
        try { provider.balance(btc,"Blockstream"); fail("Expected failure") } catch(e: ProviderFailure) { assertEquals("Network unavailable or request timed out",e.message) }
        assertEquals(3,attempts.get())
    }
    @Test fun retryAfterBeyondBudgetStopsEarly()=runBlocking {
        val attempts=AtomicInteger()
        val provider=Providers({ "" },client("60") { attempts.incrementAndGet(); 429 to "{}" })
        try { provider.balance(btc,"Blockstream"); fail("Expected quota failure") } catch(e: ProviderFailure) { assertTrue(e.message!!.contains("quota")) }
        assertEquals(1,attempts.get())
    }
    @Test fun refreshRetainsPriorHoldingOnFailure()=runBlocking {
        val p=Portfolio(buckets=listOf(Bucket("b","Cash","USD")),holdings=listOf(btc.copy(quantity="0.5")),settings=Settings("USD"))
        val result=Providers({ "" },client { 500 to "{}" }).refresh(p,null)
        assertEquals("0.5",result.holdings.single().quantity)
        assertNotNull(result.holdings.single().error)
        assertEquals(2,result.errors.size) // holding failure + BTC quote (CoinGecko needs API key)
    }
}
