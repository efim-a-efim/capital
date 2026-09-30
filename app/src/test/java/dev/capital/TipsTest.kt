package dev.capital

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class TipsTest {
    // The site lists the same wallets; one edited without the other would show different addresses.
    @Test fun siteListsTheSameWallets() {
        val site = Regex("""\{ network: (.+?), address: "(.*?)" \}""").findAll(File("../docs/_data/tips.yml").readText()).map { it.groupValues[1] to it.groupValues[2] }.toList()
        assertEquals(Tips.wallets, site)
    }
}
