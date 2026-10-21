package dev.capital.brokers

import dev.capital.data.KeyArgument
import dev.capital.data.ProviderFailure
import dev.capital.domain.tr
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory

/** Interactive Brokers Flex Web Service: NAV total, base currency and report date of the one statement an Activity Flex Query returns. */
object InteractiveBrokers: BrokerPlugin {
    override val name="Interactive Brokers"
    override val site="https://www.interactivebrokers.com"
    override val credentials=listOf(Credential("Interactive Brokers") { tr("Access token: {0}","Interactive Brokers") })
    override val idForm=Regex("[0-9]{1,20}")
    override fun idLabel()=tr("Flex Query id")
    override fun idError()=tr("Enter the numeric Flex Query id")
    private fun xml(text: String): org.w3c.dom.Document {
        require(!text.contains("<!DOCTYPE",ignoreCase=true) && !text.contains("<!ENTITY",ignoreCase=true)) { tr("XML must not declare entities") }
        return try { DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(text.byteInputStream()) } catch(e: org.xml.sax.SAXException) { throw ProviderFailure(tr("Invalid provider response")) }
    }
    private fun org.w3c.dom.Element.child(name: String): String? = getElementsByTagName(name).item(0)?.textContent?.trim()
    private fun flexError(code: String?,message: String?): String = when(code) {
        "1012" -> tr("Token has expired; generate a new one in Client Portal")
        "1015" -> tr("Token is invalid")
        "1013" -> tr("Token is restricted to another IP address")
        "1014" -> tr("Flex Query id is invalid")
        "1011" -> tr("Flex Web Service is inactive; enable it in Client Portal")
        "1016" -> tr("Account is invalid")
        "1003","1021" -> tr("Statement is not available; retry later")
        else -> tr("Interactive Brokers error {0}",(code ?: "?")+(message?.let { " "+it.clean() } ?: ""))
    }
    /** The one statement the Flex Query returns, after the two-step SendRequest / GetStatement exchange. */
    private suspend fun statement(host: BrokerHost,id: String): org.w3c.dom.Document {
        val token=host.required(name,name); if(!token.matches(Regex("[A-Za-z0-9]{1,200}"))) throw KeyArgument(tr("Invalid Interactive Brokers token"))
        val base="https://ndcdyn.interactivebrokers.com/AccountManagement/FlexWebService"; val ua=mapOf("User-Agent" to "Java")
        val sent=xml(host.text("$base/SendRequest?t=$token&q=$id&v=3",headers=ua)).documentElement
        if(sent.tagName!="FlexStatementResponse") throw ProviderFailure(tr("Invalid provider response"))
        if(sent.child("Status")!="Success") throw ProviderFailure(flexError(sent.child("ErrorCode"),sent.child("ErrorMessage")))
        val reference=sent.child("ReferenceCode")?.takeIf { it.matches(Regex("[0-9]{1,30}")) } ?: throw ProviderFailure(tr("Missing {0} in provider response","ReferenceCode"))
        // ponytail: 6 polls, 5 s apart (10 s when throttled); a query with many sections can take longer and then fails as "not ready".
        repeat(6) { attempt ->
            val doc=xml(host.text("$base/GetStatement?t=$token&q=$reference&v=3",headers=ua)); val root=doc.documentElement
            if(root.tagName=="FlexQueryResponse") {
                val statements=doc.getElementsByTagName("FlexStatement")
                if(statements.length!=1) throw ProviderFailure(tr("The query returned {0} accounts; make one Flex Query per account",statements.length))
                return doc
            }
            if(root.tagName!="FlexStatementResponse") throw ProviderFailure(tr("Invalid provider response"))
            val code=root.child("ErrorCode")
            if(code !in setOf("1009","1019","1018","1004")) throw ProviderFailure(flexError(code,root.child("ErrorMessage")))
            if(attempt==5) throw ProviderFailure(tr("Statement is not ready yet; refresh again in a minute"))
            delay(if(code=="1018") 10_000 else 5_000)
        }
        throw ProviderFailure(tr("Provider unavailable"))
    }
    private fun org.w3c.dom.Document.rows(tag: String): List<org.w3c.dom.Element> = getElementsByTagName(tag).let { n -> (0 until n.length).map { n.item(it) as org.w3c.dom.Element } }
    private fun org.w3c.dom.Document.baseCurrency(): String = (getElementsByTagName("AccountInformation").item(0) as? org.w3c.dom.Element)?.getAttribute("currency").orEmpty()
    /** Flex dates: `20250115`, `2025-01-15`, or with a time after `;` or `,`. */
    private fun flexDate(raw: String): LocalDate? = raw.substringBefore(';').substringBefore(',').filter { it.isDigit() }.takeIf { it.length==8 }?.let { runCatching { LocalDate.parse(it,DateTimeFormatter.BASIC_ISO_DATE) }.getOrNull() }
    private fun navRows(doc: org.w3c.dom.Document): List<org.w3c.dom.Element> = doc.rows("EquitySummaryByReportDateInBase").filter { it.hasAttribute("total") }
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val doc=statement(host,id)
        val latest=navRows(doc).maxByOrNull { it.getAttribute("reportDate").filter { c -> c.isDigit() } }
            ?: throw ProviderFailure(tr("Add the section Net Asset Value (NAV) Summary in Base with Report Date and Total to the Flex Query"))
        val currency=latest.getAttribute("currency").ifBlank { doc.baseCurrency() }.ifBlank { throw ProviderFailure(tr("Add the Currency field of Account Information to the Flex Query")) }
        val date=flexDate(latest.getAttribute("reportDate")) ?: throw ProviderFailure(tr("Invalid quote time"))
        return Reading(currency,latest.getAttribute("total"),date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
    }
    override val listsFlows=true
    /** `CashTransactions` rows of type Deposits & Withdrawals; the Flex web service returns at most the days the query's period covers (365 at most), no paging. */
    override suspend fun flows(host: BrokerHost,id: String,since: LocalDate): List<BrokerFlow> {
        val doc=statement(host,id)
        if(doc.getElementsByTagName("CashTransactions").length==0) throw ProviderFailure(tr("The Flex Query lacks the {0} section","Cash Transactions"))
        // The official field list does not give the type values; fixtures show "Deposits & Withdrawals", the NAV reports say "Deposits/Withdrawals".
        return doc.rows("CashTransaction").filter { it.getAttribute("type").trim() in setOf("Deposits & Withdrawals","Deposits/Withdrawals") }.mapNotNull { r ->
            val date=listOf("dateTime","settleDateTime","reportDate","date").firstNotNullOfOrNull { flexDate(r.getAttribute(it)) } ?: return@mapNotNull null
            val ref=r.getAttribute("transactionID").ifBlank { return@mapNotNull null }
            if(date.isBefore(since)) null else BrokerFlow(date,r.getAttribute("amount"),r.getAttribute("currency").ifBlank { doc.baseCurrency() },ref)
        }.sortedBy { it.date }
    }
    override val listsHistory=true
    /** Daily NAV from `EquitySummaryByReportDateInBase`; the same 365-day limit applies. */
    override suspend fun history(host: BrokerHost,id: String,since: LocalDate): List<Pair<LocalDate,Reading>> {
        val doc=statement(host,id)
        val rows=navRows(doc)
        if(rows.isEmpty() && doc.getElementsByTagName("EquitySummaryInBase").length==0) throw ProviderFailure(tr("The Flex Query lacks the {0} section","Net Asset Value (NAV) Summary in Base"))
        val base=doc.baseCurrency()
        return rows.mapNotNull { r -> flexDate(r.getAttribute("reportDate"))?.takeIf { !it.isBefore(since) }?.let { d -> d to Reading(r.getAttribute("currency").ifBlank { base },r.getAttribute("total"),d.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()) } }
            .groupBy { it.first }.map { (_,l) -> l.last() }.sortedBy { it.first }
    }
}
