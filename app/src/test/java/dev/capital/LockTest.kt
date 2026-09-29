package dev.capital

import dev.capital.data.*
import org.junit.Assert.*
import org.junit.Test

class LockTest {
    @Test fun pinRules() {
        assertNull(validPin("1234".toCharArray())); assertNull(validPin("123456789012".toCharArray()))
        assertNotNull(validPin("123".toCharArray())); assertNotNull(validPin("1234567890123".toCharArray()))
        assertNotNull(validPin("12a4".toCharArray())); assertNotNull(validPin("12 4".toCharArray())); assertNotNull(validPin("١٢٣٤".toCharArray()))
        assertNotNull(validPin(CharArray(0)))
    }
    @Test fun waitingTimes() {
        (0..4).forEach { assertEquals(0L,waitMillis(it)) }
        assertEquals(listOf(30_000L,60_000L,300_000L,900_000L,3_600_000L),(5..9).map(::waitMillis))
    }
    @Test fun waitText() { assertEquals("00:30",formatWait(30_000)); assertEquals("00:29",formatWait(28_500)); assertEquals("60:00",formatWait(3_600_000)) }
}
