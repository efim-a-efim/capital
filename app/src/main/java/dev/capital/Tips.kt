package dev.capital

// Developer wallet addresses shown on the tip screen. An empty address hides its network. Keep docs/_data/tips.yml identical (TipsTest checks).
object Tips {
    val wallets = listOf(
        "BTC" to "",
        "ETH / ERC-20" to "",
        "TRX / TRC-20" to "",
        "TON / GRAM" to "",
    )
    val shown get() = wallets.filter { it.second.isNotBlank() }
}
