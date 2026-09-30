package dev.capital

// Wallet addresses come from docs/_data/tips.yml through BuildConfig (see app/build.gradle.kts). An empty address hides its network.
object Tips {
    val wallets = listOf(
        "BTC" to BuildConfig.TIP_BTC,
        "ETH / ERC-20" to BuildConfig.TIP_ETH,
        "TRX / TRC-20" to BuildConfig.TIP_TRX,
        "TON / GRAM" to BuildConfig.TIP_TON,
    )
    val shown get() = wallets.filter { it.second.isNotBlank() }
}
