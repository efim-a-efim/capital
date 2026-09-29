package dev.capital.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.capital.CapitalModel
import dev.capital.data.Revision
import dev.capital.domain.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Currency

private val light=lightColorScheme(primary=Color(0xff176451),onPrimary=Color.White,background=Color(0xfff8faf5),surface=Color(0xfff8faf5),surfaceVariant=Color(0xffe2eee7),onSurface=Color(0xff192d2a),onSurfaceVariant=Color(0xff52645b))
private val dark=darkColorScheme(primary=Color(0xff8fd3b0),onPrimary=Color(0xff123425),background=Color(0xff101b18),surface=Color(0xff182722),surfaceVariant=Color(0xff264b39),onSurface=Color(0xffe3eae3),onSurfaceVariant=Color(0xffb3c4b9))
private data class Editor(val kind: String,val id: String="",val owner: String="")
private fun money(value: BigDecimal?,asset: String): String {
    if(value==null) return "Unavailable · ${assetLabel(asset)}"
    val fiat=runCatching { Currency.getInstance(asset) }.getOrNull()
    return if(fiat != null) NumberFormat.getCurrencyInstance().apply { currency=fiat; maximumFractionDigits=fiat.defaultFractionDigits.coerceAtLeast(0) }.format(value)
    else "${NumberFormat.getNumberInstance().apply { maximumFractionDigits=18 }.format(value)} ${assetLabel(asset)}"
}
private fun time(value: Long?): String = value?.let { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())) } ?: "Never refreshed"
private fun date(value: String): String=runCatching { LocalDate.parse(value).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) }.getOrDefault(value)
@Composable private fun Heading(value: String) { Text(value,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(top=20.dp,bottom=8.dp)) }
@Composable private fun Note(value: String) { Text(value,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) }
@Composable private fun Notice(value: String) { Surface(color=MaterialTheme.colorScheme.surfaceVariant,shape=MaterialTheme.shapes.medium) { Text(value,Modifier.fillMaxWidth().padding(16.dp),style=MaterialTheme.typography.bodyMedium) } }
@Composable private fun Actions(content: @Composable FlowRowScope.()->Unit) { FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth(),content=content) }
@Composable private fun Stat(label: String,value: String) { Column(Modifier.padding(vertical=12.dp)) { Note(label); Text(value,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.SemiBold) } }
@Composable private fun Item(title: String,subtitle: String,value: String?=null,onClick: (()->Unit)?) {
    val body: @Composable ()->Unit = {
        Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            Text(title,style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.onSurface)
            if(value!=null) Text(value,style=MaterialTheme.typography.titleLarge,color=MaterialTheme.colorScheme.onSurface)
            Note(subtitle)
        }
    }
    if(onClick==null) Box(Modifier.fillMaxWidth().padding(vertical=14.dp,horizontal=4.dp)) { body() }
    else TextButton(onClick=onClick,modifier=Modifier.fillMaxWidth(),shape=MaterialTheme.shapes.small,contentPadding=PaddingValues(vertical=14.dp,horizontal=4.dp)) { body() }
    HorizontalDivider()
}
@Composable private fun GoalRow(goal: Goal,data: Portfolio,allocation: Allocation,onClick: ()->Unit) {
    val funded=data.convert(allocation.goal(goal.id),"USD",goal.currency)
    val progress=funded?.divideMoney(goal.target.decimal())?.toFloat()?.coerceIn(0f,1f) ?: 0f
    val status=when { goal.archived -> "Archived"; funded != null && funded >= goal.target.decimal() -> "Funded"; LocalDate.parse(goal.due)<LocalDate.now() -> "Overdue"; else -> "Due ${date(goal.due)}" }
    Item(goal.name,"Priority ${goal.priority} · $status","${money(funded,goal.currency)} / ${money(goal.target.decimal(),goal.currency)}",onClick)
    LinearProgressIndicator(progress={ progress },modifier=Modifier.fillMaxWidth().padding(top=6.dp,bottom=14.dp))
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun CapitalApp(model: CapitalModel) {
    val state by model.state.collectAsStateWithLifecycle()
    val data=state.data
    val allocation=remember(data) { data.allocate() }
    var section by rememberSaveable { mutableStateOf("Overview") }
    var bucketId by rememberSaveable { mutableStateOf<String?>(null) }
    var goalId by rememberSaveable { mutableStateOf<String?>(null) }
    var editor by remember { mutableStateOf<Editor?>(null) }
    var confirmation by remember { mutableStateOf<Pair<String,()->Unit>?>(null) }
    var copyFolder by remember { mutableStateOf(false) }
    var keyProvider by remember { mutableStateOf<String?>(null) }
    val folderPicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri -> uri?.let { model.chooseFolder(it,copyFolder) } }
    val backup=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { it?.let(model::export) }
    val restore=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { it?.let(model::inspectRestore) }
    val context=LocalContext.current
    val screenScroll=remember(section,bucketId,goalId) { ScrollState(0) }
    val scheme=when(data.settings.theme) { "Dark" -> dark; "Light" -> light; else -> if(isSystemInDarkTheme()) dark else light }
    fun select(value: String) { section=value; bucketId=null; goalId=null }
    val editable=state.ready && !state.blocked && !state.unsaved && !state.saving
    BackHandler(editor==null && (bucketId!=null || goalId!=null || section!="Overview")) { if(bucketId!=null || goalId!=null) { bucketId=null; goalId=null } else section="Overview" }
    MaterialTheme(colorScheme=scheme) {
        BoxWithConstraints {
            val wide=maxWidth>=840.dp
            Scaffold(
                topBar={ TopAppBar(title={ Text(if(!state.ready) "Capital" else section) },actions={
                    if(state.ready) { TextButton(onClick={ model.refresh(bucketId) },enabled=!state.refreshing && editable) { Text(if(state.refreshing) "Refreshing…" else "Refresh") }; TextButton(onClick={ select("Settings") }) { Text("Settings") } }
                }) },
                bottomBar={ if(state.ready && !wide) NavigationBar { listOf("Overview","Buckets","Goals").forEach { target -> NavigationBarItem(selected=section==target,onClick={ select(target) },icon={ Text(when(target) { "Overview" -> "◫"; "Buckets" -> "▤"; else -> "◎" },Modifier.clearAndSetSemantics {}) },label={ Text(target) }) } } },
            ) { padding ->
                Row(Modifier.fillMaxSize().padding(padding)) {
                    if(wide && state.ready) NavigationRail { listOf("Overview","Buckets","Goals","Settings").forEach { target -> NavigationRailItem(selected=section==target,onClick={ select(target) },icon={ Text(target.take(1),Modifier.clearAndSetSemantics {}) },label={ Text(target) }) } }
                    if(wide && ((section=="Buckets" && bucketId!=null) || (section=="Goals" && goalId!=null))) {
                        Column(Modifier.width(250.dp).fillMaxHeight().verticalScroll(rememberScrollState()).padding(16.dp)) {
                            if(section=="Buckets") data.buckets.forEach { b -> Item(b.name,b.currency) { bucketId=b.id } }
                            else data.goals.sortedByDescending { it.priority }.forEach { g -> Item(g.name,"Priority ${g.priority}") { goalId=g.id } }
                        }
                    }
                    Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(screenScroll).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        if(state.loading) { LinearProgressIndicator(Modifier.fillMaxWidth()); Text("Opening your savings…") }
                        state.message?.let { Notice(it); TextButton(onClick=model::dismissMessage) { Text("Dismiss message") } }
                        if(state.unsaved && !state.saving) {
                            Notice("Unsaved changes — kept in memory. Do not close the app before saving or exporting a copy.")
                            Actions {
                                Button(onClick={ model.retrySave() },enabled=!state.saving) { Text("Retry save") }
                                OutlinedButton(onClick={ copyFolder=true; folderPicker.launch(null) }) { Text("Save copy to folder") }
                                TextButton(onClick={ confirmation="Discard unsaved changes and reload the folder?" to { model.reload(true) } }) { Text("Discard and reload") }
                            }
                        }
                        if(state.blocked) {
                            Notice("Editing is paused until storage is resolved. Cached data remains visible.")
                            OutlinedButton(onClick={ model.reload() }) { Text("Reload folder") }
                            if(state.heads.size>1) state.heads.forEach { revision ->
                                RevisionSummary(revision)
                                OutlinedButton(onClick={ confirmation="Use revision ${revision.id.take(8)}? Both originals remain in the folder." to { model.resolve(revision) } }) { Text("Keep this version") }
                            }
                        }
                        if(!state.ready && !state.loading) {
                            Text("Your savings.\nTheir purpose.",style=MaterialTheme.typography.displaySmall,fontWeight=FontWeight.SemiBold)
                            Text("Track balances, connect goals, and see what is already covered. No account required.")
                            Heading("Start with your folder")
                            Text("Choose a dedicated local folder such as Documents/CapitalTracker. Reopen that folder on another device using your own sync tool.")
                            Notice("Public wallet providers receive your addresses and IP. Never enter private keys or seed phrases. Files in the chosen folder contain your financial records.")
                            Button(onClick={ copyFolder=false; folderPicker.launch(null) }) { Text("Choose or reopen folder") }
                            Note("Default currency starts as EUR. Change it and configure optional free provider keys in Settings.")
                        } else if(state.ready) {
                            if(data.stale() || allocation.incomplete) Notice(if(allocation.incomplete) "Incomplete valuation — some balances or rates are unavailable. Native quantities remain visible." else "Cached / stale values — refresh when online. Allocations are estimates.")
                            when(section) {
                                "Overview" -> {
                                    val currency=data.settings.currency
                                    val total=data.buckets.fold(ZERO) { sum,b -> sum+data.bucketValue(b.id,currency) }
                                    Note("TOTAL VALUED SAVINGS · $currency")
                                    Text(money(total,currency),style=MaterialTheme.typography.displaySmall,fontWeight=FontWeight.SemiBold)
                                    Stat("Allocated to goals",money(data.convert(allocation.total,"USD",currency),currency))
                                    Stat("Unallocated",money(data.convert(allocation.capacities.values.fold(ZERO,BigDecimal::add)-allocation.total,"USD",currency),currency))
                                    if(data.buckets.isEmpty()) { Text("Add your first bucket to start tracking savings."); Button(onClick={ editor=Editor("Bucket") },enabled=editable) { Text("Add bucket") } }
                                    Heading("Your goals")
                                    if(data.goals.none { !it.archived }) { Text("Give your savings a purpose."); OutlinedButton(onClick={ editor=Editor("Goal") },enabled=editable) { Text("Add goal") } }
                                    data.goals.filterNot { it.archived }.sortedBy { it.due }.forEach { GoalRow(it,data,allocation) { section="Goals"; goalId=it.id } }
                                    Heading("Buckets")
                                    data.buckets.forEach { b -> Item(b.name,"${data.holdings.count { it.bucketId==b.id }} holdings · ${b.currency}",money(data.bucketValue(b.id,b.currency),b.currency)) { section="Buckets"; bucketId=b.id } }
                                    Note("Goal allocations are part of savings, not additional money.")
                                }
                                "Buckets" -> {
                                    val bucket=data.buckets.find { it.id==bucketId }
                                    if(bucket==null) {
                                        Button(onClick={ editor=Editor("Bucket") },enabled=editable) { Text("Add bucket") }
                                        if(data.buckets.isEmpty()) Text("Buckets group places where your money lives.")
                                        data.buckets.forEach { b -> Item(b.name,"${data.holdings.count { it.bucketId==b.id }} holdings",money(data.bucketValue(b.id,b.currency),b.currency)) { bucketId=b.id } }
                                    } else {
                                        TextButton(onClick={ bucketId=null }) { Text("← All buckets") }
                                        Heading(bucket.name)
                                        Text(money(data.bucketValue(bucket.id,bucket.currency),bucket.currency),style=MaterialTheme.typography.headlineLarge)
                                        Stat("Allocated",money(data.convert(allocation.bucket(bucket.id),"USD",bucket.currency),bucket.currency))
                                        Stat("Available",money(data.convert((allocation.capacities[bucket.id] ?: ZERO)-allocation.bucket(bucket.id),"USD",bucket.currency),bucket.currency))
                                        Actions {
                                            OutlinedButton(onClick={ editor=Editor("Bucket",bucket.id) },enabled=editable) { Text("Edit bucket") }
                                            TextButton(onClick={ confirmation="Delete ${bucket.name}, its ${data.holdings.count { it.bucketId==bucket.id }} holdings and ${data.connections.count { it.bucketId==bucket.id }} goal connections? Previous snapshots remain recoverable." to { model.edit({ it.deleteBucket(bucket.id) }) { bucketId=null } } },enabled=editable) { Text("Delete bucket") }
                                        }
                                        Heading("Holdings")
                                        Button(onClick={ editor=Editor("Holding",owner=bucket.id) },enabled=editable) { Text("Add holding") }
                                        data.holdings.filter { it.bucketId==bucket.id }.forEach { h ->
                                            val converted=h.quantity?.let { data.convert(it.decimal(),h.asset,bucket.currency) }
                                            Item(h.label,"${h.address?.let { "Read-only · ${it.take(12)}…" } ?: "Manual"}\n${h.quantity?.let { money(it.decimal(),h.asset) } ?: "Balance unknown"}",money(converted,bucket.currency),null)
                                            Note("${h.source} · observed ${time(h.observedAt)} · fetched ${time(h.fetchedAt)}")
                                            h.error?.let { Notice(it) }
                                            Actions { TextButton(onClick={ editor=Editor("Holding",h.id,bucket.id) },enabled=editable) { Text("Edit / move") }; TextButton(onClick={ confirmation="Delete ${h.label}?" to { model.edit({ p -> p.copy(holdings=p.holdings.filterNot { it.id==h.id }) }) } },enabled=editable) { Text("Delete") } }
                                        }
                                        Heading("Connected goals")
                                        data.connections.filter { it.bucketId==bucket.id }.forEach { c -> val g=data.goals.first { it.id==c.goalId }; Item(g.name,limitLabel(c.mode),money(data.convert(allocation.byConnection[c.key] ?: ZERO,"USD",g.currency),g.currency)) { section="Goals"; goalId=g.id } }
                                        Note("Automatic tracking covers native address balances only. Add tokens, staking or other investments as manual holdings.")
                                    }
                                }
                                "Goals" -> {
                                    val goal=data.goals.find { it.id==goalId }
                                    if(goal==null) {
                                        Button(onClick={ editor=Editor("Goal") },enabled=editable) { Text("Add goal") }
                                        if(data.goals.isEmpty()) Text("Create a goal with a target, currency and due date.")
                                        data.goals.sortedWith(compareBy<Goal> { it.archived }.thenByDescending { it.priority }.thenBy { it.due }).forEach { GoalRow(it,data,allocation) { goalId=it.id } }
                                    } else {
                                        TextButton(onClick={ goalId=null }) { Text("← All goals") }
                                        GoalRow(goal,data,allocation) { if(editable) editor=Editor("Goal",goal.id) }
                                        val funded=data.convert(allocation.goal(goal.id),"USD",goal.currency)
                                        Stat("Still needed",money(funded?.let { (goal.target.decimal()-it).max(ZERO) },goal.currency))
                                        Actions {
                                            OutlinedButton(onClick={ editor=Editor("Goal",goal.id) },enabled=editable) { Text("Edit goal") }
                                            TextButton(onClick={ model.edit({ p -> p.copy(goals=p.goals.map { if(it.id==goal.id) it.copy(archived=!it.archived) else it }) }) },enabled=editable) { Text(if(goal.archived) "Activate" else "Archive") }
                                            TextButton(onClick={ confirmation="Delete ${goal.name} and its connections?" to { model.edit({ it.deleteGoal(goal.id) }) { goalId=null } } },enabled=editable) { Text("Delete") }
                                        }
                                        Heading("Funding sources")
                                        Button(onClick={ editor=Editor("Connection",owner=goal.id) },enabled=editable && data.buckets.isNotEmpty()) { Text("Connect bucket") }
                                        if(data.buckets.isEmpty()) Note("Create a bucket first.")
                                        data.connections.filter { it.goalId==goal.id }.forEach { c ->
                                            val b=data.buckets.first { it.id==c.bucketId }
                                            val amount=allocation.byConnection[c.key] ?: ZERO
                                            Item(b.name,"${limitLabel(c.mode)} ${if(c.mode!=Limit.AUTO) c.value else ""}${c.goalCap?.let { " · Max $it% of goal" } ?: ""}",money(data.convert(amount,"USD",goal.currency),goal.currency)) { if(editable) editor=Editor("Connection",c.key,goal.id) }
                                            val free=(allocation.capacities[b.id] ?: ZERO)-allocation.bucket(b.id)
                                            Note(when { goal.archived -> "Archived goals reserve no funds."; allocation.incomplete -> "Missing rates or balances can limit funding."; free>ZERO && funded!=null && funded<goal.target.decimal() -> "Available savings remain. Increase this connection's limit to allocate more."; amount==ZERO && funded!=null && funded>=goal.target.decimal() -> "Goal target is covered by other buckets."; amount==ZERO -> "No capacity left after higher priorities or connection limits."; else -> "Contribution respects goal priority and connection limits." })
                                            TextButton(onClick={ confirmation="Disconnect ${b.name} from ${goal.name}?" to { model.edit({ p -> p.copy(connections=p.connections.filterNot { it.key==c.key }) }) } },enabled=editable) { Text("Disconnect") }
                                        }
                                        Note("Higher priority numbers are funded first. Equal priorities share bucket value. Allocation never moves money.")
                                    }
                                }
                                "Settings" -> {
                                    Heading("Preferences")
                                    OutlinedButton(onClick={ editor=Editor("Settings") },enabled=editable) { Text("Currency: ${data.settings.currency} · Theme: ${data.settings.theme}") }
                                    Note("Default currency values the overview and new entities. Existing currencies stay unchanged.")
                                    Heading("Free data providers")
                                    providerChoices.forEach { (category,options) ->
                                        Choice(category,data.settings.providers.getValue(category),options,editable) { provider -> model.edit({ p -> p.copy(settings=p.settings.copy(providers=p.settings.providers+(category to provider))) }) }
                                        val provider=data.settings.providers.getValue(category)
                                        if(provider in listOf("Alchemy","TronGrid","CoinGecko","TON Center")) TextButton(onClick={ keyProvider=provider }) { Text("${if(provider=="TON Center") "Optional" else "Required free"} key: $provider") }
                                    }
                                    OutlinedButton(onClick={ model.refresh() },enabled=editable && !state.refreshing) { Text("Test sources / refresh portfolio") }
                                    Note("Tests query only assets and addresses in your portfolio. No silent provider fallback.")
                                    Heading("Quotes and freshness")
                                    if(data.quotes.isEmpty()) Note("No cached quotes. Add holdings and refresh.")
                                    data.quotes.forEach { q -> Text("${q.asset}: ${money(q.usd.decimal(),"USD")}"); Note("${q.source} · observed ${time(q.observedAt)} · fetched ${time(q.fetchedAt)}${q.error?.let { "\n$it" } ?: ""}") }
                                    Heading("Storage")
                                    Text(state.folder ?: "No folder")
                                    Actions {
                                        OutlinedButton(onClick={ copyFolder=false; folderPicker.launch(null) }) { Text("Reconnect / open folder") }
                                        OutlinedButton(onClick={ model.reload() }) { Text("Reload local files") }
                                        OutlinedButton(onClick={ backup.launch("capital-backup-${LocalDate.now()}.json") }) { Text("Export backup") }
                                        OutlinedButton(onClick={ restore.launch(arrayOf("application/json","text/plain","application/octet-stream")) },enabled=editable) { Text("Restore backup") }
                                    }
                                    Note("Your sync tool manages this folder. Snapshots are retained for recovery; API keys never leave device storage. Refresh happens only on cold startup or request.")
                                    Heading("Sources / attribution")
                                    listOf("Blockstream" to "https://blockstream.info", "mempool.space" to "https://mempool.space", "PublicNode" to "https://publicnode.com", "Alchemy" to "https://alchemy.com", "TON Center" to "https://toncenter.com", "TonAPI" to "https://tonapi.io", "TronGrid" to "https://trongrid.io", "Powered by CoinGecko" to "https://coingecko.com", "Powered by CoinPaprika" to "https://coinpaprika.com", "Frankfurter" to "https://frankfurter.dev", "European Central Bank" to "https://ecb.europa.eu").forEach { (name,url) -> TextButton(onClick={ runCatching { context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url))) }.onFailure { model.notice("No browser available") } }) { Text(name) } }
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
        editor?.let { e -> EditSheet(e,data,state.saving,if(state.unsaved && !state.saving) state.message ?: "Save failed" else null,onDismiss={ editor=null },onSave={ transform -> model.edit(transform) { editor=null } }) }
        confirmation?.let { (text,action) -> AlertDialog(onDismissRequest={ confirmation=null },title={ Text("Confirm change") },text={ Text(text) },confirmButton={ TextButton(onClick={ confirmation=null; action() }) { Text("Confirm") } },dismissButton={ TextButton(onClick={ confirmation=null }) { Text("Cancel") } }) }
        keyProvider?.let { provider -> KeyDialog(provider,onDismiss={ keyProvider=null },onSave={ if(model.saveKey(provider,it)) keyProvider=null }) }
        state.restore?.let { revision -> AlertDialog(onDismissRequest=model::cancelRestore,title={ Text("Restore backup?") },text={ Column { RevisionSummary(revision); Text("Replaces current records with this backup. Existing snapshots remain available.") } },confirmButton={ TextButton(onClick=model::restore) { Text("Restore") } },dismissButton={ TextButton(onClick=model::cancelRestore) { Text("Cancel") } }) }
    }
}
@Composable private fun RevisionSummary(revision: Revision) {
    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Text("Revision ${revision.id.take(8)} · ${time(revision.createdAt)}",fontWeight=FontWeight.SemiBold)
        Text("${revision.data.buckets.size} buckets · ${revision.data.holdings.size} holdings · ${revision.data.goals.size} goals")
        revision.data.buckets.forEach { b -> Text("${b.name}: ${money(revision.data.bucketValue(b.id,b.currency),b.currency)}") }
        revision.data.holdings.forEach { h -> Note("${h.label}: ${h.quantity ?: "unknown"} ${h.asset} · ${h.address ?: "manual"}") }
        revision.data.goals.forEach { g -> Note("${g.name}: ${money(g.target.decimal(),g.currency)} · priority ${g.priority} · ${date(g.due)}") }
    }
}
@Composable private fun Choice(label: String,value: String,options: List<String>,enabled: Boolean=true,onChange: (String)->Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column { Note(label); Box { OutlinedButton(onClick={ expanded=true },enabled=enabled,modifier=Modifier.fillMaxWidth()) { Text(value) }; DropdownMenu(expanded=expanded,onDismissRequest={ expanded=false }) { options.forEach { option -> DropdownMenuItem(text={ Text(option) },onClick={ expanded=false; onChange(option) }) } } } }
}
private fun limitLabel(mode: Limit)=when(mode) { Limit.AUTO -> "Auto — up to remaining need"; Limit.FIXED -> "Fixed amount in goal currency"; Limit.BUCKET_PERCENT -> "% of bucket"; Limit.GOAL_PERCENT -> "% of goal" }
@Composable private fun KeyDialog(provider: String,onDismiss: ()->Unit,onSave: (String)->Unit) {
    var value by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest=onDismiss,title={ Text("$provider key") },text={ Column { Text("Stored encrypted on this device. Leave blank to remove an existing key."); OutlinedTextField(value,onValueChange={ value=it },label={ Text("API key") },visualTransformation=androidx.compose.ui.text.input.PasswordVisualTransformation(),singleLine=true) } },confirmButton={ TextButton(onClick={ onSave(value) }) { Text("Save") } },dismissButton={ TextButton(onClick=onDismiss) { Text("Cancel") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun EditSheet(editor: Editor,data: Portfolio,saving: Boolean,saveError: String?,onDismiss: ()->Unit,onSave: ((Portfolio)->Portfolio)->Unit) {
    val b=data.buckets.find { it.id==editor.id }
    val h=data.holdings.find { it.id==editor.id }
    val g=data.goals.find { it.id==editor.id }
    val c=data.connections.find { it.key==editor.id }
    val newId=remember { id() }
    val initial=remember(editor) { mapOf(
        "name" to (b?.name ?: h?.label ?: g?.name ?: ""),
        "currency" to (b?.currency ?: h?.asset ?: g?.currency ?: data.settings.currency),
        "quantity" to (h?.quantity ?: "0"), "target" to (g?.target ?: ""),
        "due" to (g?.due ?: LocalDate.now().plusYears(1).toString()), "priority" to (g?.priority?.toString() ?: "0"),
        "bucket" to (h?.bucketId ?: c?.bucketId ?: editor.owner.takeIf { editor.kind=="Holding" } ?: data.buckets.firstOrNull()?.id.orEmpty()),
        "address" to (h?.address ?: ""), "type" to (if(h?.address!=null) "Wallet" else "Manual"),
        "mode" to (c?.mode?.name ?: Limit.AUTO.name), "value" to (c?.value ?: "0"), "cap" to (c?.goalCap ?: ""),
        "theme" to data.settings.theme, "currencyAction" to "Choose treatment",
    ) }
    val fields=remember { mutableStateMapOf<String,String>().apply { putAll(initial) } }
    var error by remember { mutableStateOf<String?>(null) }
    var discard by remember { mutableStateOf(false) }
    val context=LocalContext.current
    fun close() { if(fields.toMap()!=initial) discard=true else onDismiss() }
    fun number(key: String)=fields.getValue(key).trim().replace(',','.').also { it.decimal() }
    fun transform(p: Portfolio): Portfolio {
        val currency=fields.getValue("currency").trim().uppercase()
        require(validAsset(currency)) { "Use an ISO currency code (EUR, USD…) or BTC, ETH, TON, TRX" }
        val name=fields.getValue("name").trim()
        val oldCurrency=when(editor.kind) { "Holding" -> h?.asset; "Goal" -> g?.currency; else -> null }
        val changed=oldCurrency!=null && oldCurrency!=currency && (editor.kind!="Holding" || fields.getValue("type")=="Manual")
        if(changed) require(fields.getValue("currencyAction")!="Choose treatment") { "Choose how to treat the changed currency" }
        val convert=changed && fields.getValue("currencyAction")=="Convert existing values"
        fun converted(amount: String)=p.convert(amount.decimal(),requireNotNull(oldCurrency),currency)?.text() ?: error("Refresh exchange rates before converting")
        return when(editor.kind) {
            "Bucket" -> {
                val item=Bucket(b?.id ?: newId,name,currency)
                p.copy(buckets=if(b==null) p.buckets+item else p.buckets.map { if(it.id==b.id) item else it })
            }
            "Holding" -> {
                val wallet=fields.getValue("type")=="Wallet"
                val address=if(wallet) canonicalAddress(Chain.entries.find { it.name==currency } ?: error("Choose BTC, ETH, TON or TRX for a wallet"),fields.getValue("address")) else null
                p.holdings.firstOrNull { it.id!=h?.id && it.address!=null && it.asset==currency && canonicalAddress(Chain.valueOf(currency),it.address)==address }?.let { duplicate -> error("Wallet already belongs to ${p.buckets.first { it.id==duplicate.bucketId }.name}. Edit or move it there.") }
                val current=p.holdings.find { it.id==h?.id }
                require(h==null || current!=null) { "This holding was removed; reopen the editor" }
                val unchanged=wallet && current?.address!=null && current.asset==currency && canonicalAddress(Chain.valueOf(currency),current.address)==address
                val item=Holding(
                    id=h?.id ?: newId,bucketId=fields.getValue("bucket"),label=name,asset=currency,address=address,
                    quantity=if(wallet) if(unchanged) current?.quantity else null else if(convert) converted(requireNotNull(h?.quantity)) else number("quantity"),
                    observedAt=if(unchanged) current?.observedAt else if(!wallet) System.currentTimeMillis() else null,
                    fetchedAt=if(unchanged) current?.fetchedAt else null,source=if(unchanged) current?.source ?: "Unknown" else if(wallet) "Not refreshed" else "Manual",
                    error=if(unchanged) current?.error else null,
                )
                p.copy(holdings=if(h==null) p.holdings+item else p.holdings.map { if(it.id==h.id) item else it })
            }
            "Goal" -> {
                val item=Goal(g?.id ?: newId,name,if(convert) converted(requireNotNull(g?.target)) else number("target"),currency,fields.getValue("due"),fields.getValue("priority").toIntOrNull() ?: error("Priority must be a whole number"),g?.archived ?: false)
                p.copy(goals=if(g==null) p.goals+item else p.goals.map { if(it.id==g.id) item else it },connections=p.connections.map { if(convert && it.goalId==g?.id && it.mode==Limit.FIXED) it.copy(value=converted(it.value)) else it })
            }
            "Connection" -> {
                val item=Connection(editor.owner,fields.getValue("bucket"),Limit.valueOf(fields.getValue("mode")),number("value"),fields.getValue("cap").takeIf { it.isNotBlank() }?.trim()?.replace(',','.'))
                p.copy(connections=p.connections.filterNot { it.key==c?.key }+item)
            }
            "Settings" -> p.copy(settings=p.settings.copy(currency=currency,theme=fields.getValue("theme")))
            else -> error("Unknown editor")
        }.validate()
    }
    @Composable fun field(key: String,label: String,numeric: Boolean=false) {
        OutlinedTextField(value=fields.getValue(key),onValueChange={ fields[key]=it; error=null },label={ Text(label) },modifier=Modifier.fillMaxWidth().testTag(key),singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=if(numeric) KeyboardType.Decimal else KeyboardType.Text),enabled=!saving)
    }
    Dialog(onDismissRequest={ close() },properties=DialogProperties(usePlatformDefaultWidth=false,dismissOnClickOutside=false)) {
        Surface(Modifier.fillMaxSize().semantics { testTagsAsResourceId=true },color=MaterialTheme.colorScheme.background) {
            Scaffold(topBar={ TopAppBar(title={ Text("${if(editor.id.isBlank()) "Add" else "Edit"} ${editor.kind.lowercase()}") },navigationIcon={ TextButton(onClick={ close() },enabled=!saving) { Text("Cancel") } },actions={ TextButton(onClick={
                val checked=runCatching { transform(data) }
                if(checked.isFailure) error=checked.exceptionOrNull()?.message else onSave(::transform)
            },enabled=!saving) { Text(if(saving) "Saving…" else "Save") } }) }) { padding ->
                Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                    error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
                    if(saveError!=null) {
                        Notice("$saveError\nYour changes remain in memory.")
                        Button(onClick=onDismiss) { Text("Open save recovery") }
                    }
                    if(editor.kind in listOf("Bucket","Holding","Goal")) field("name",if(editor.kind=="Goal") "Purpose" else "Name")
                    if(editor.kind=="Holding") Choice("Tracking",fields.getValue("type"),listOf("Manual","Wallet"),!saving) { fields["type"]=it; if(it=="Wallet" && fields.getValue("currency") !in Chain.entries.map { chain -> chain.name }) fields["currency"]="BTC" }
                    if(editor.kind!="Connection") {
                        if(editor.kind=="Holding" && fields.getValue("type")=="Wallet") Choice("Mainnet chain",fields.getValue("currency"),Chain.entries.map { it.name },!saving) { fields["currency"]=it }
                        else { field("currency","Currency code (EUR, USD, BTC…)"); Note("Use an ISO currency code or a supported native asset. Conversion needs a quote from your selected provider.") }
                    }
                    if(editor.kind in listOf("Holding","Connection")) {
                        val labels=data.buckets.associate { "${it.name} · ${it.id.take(4)}" to it.id }
                        Choice("Bucket",labels.entries.find { it.value==fields["bucket"] }?.key ?: "Choose bucket",labels.keys.toList(),!saving) { fields["bucket"]=labels.getValue(it) }
                    }
                    when(editor.kind) {
                        "Holding" -> if(fields.getValue("type")=="Wallet") { field("address","Public wallet address"); Note("Paste one address. No seed phrase, private key, token discovery or HD wallet discovery.") } else field("quantity","Current quantity · no grouping separators",true)
                        "Goal" -> {
                            field("target","Target amount · no grouping separators",true)
                            field("priority","Priority (higher number first)",true)
                            field("due","Due date · YYYY-MM-DD")
                            OutlinedButton(onClick={ val d=runCatching { LocalDate.parse(fields.getValue("due")) }.getOrDefault(LocalDate.now()); DatePickerDialog(context,{ _,y,m,day -> fields["due"]=LocalDate.of(y,m+1,day).toString() },d.year,d.monthValue-1,d.dayOfMonth).show() }) { Text("Choose date") }
                        }
                        "Connection" -> {
                            val mode=Limit.valueOf(fields.getValue("mode"))
                            Choice("Contribution limit",limitLabel(mode),Limit.entries.map(::limitLabel),!saving) { chosen -> fields["mode"]=Limit.entries.first { limitLabel(it)==chosen }.name }
                            if(mode!=Limit.AUTO) field("value",if(mode==Limit.FIXED) "Amount in ${data.goals.first { it.id==editor.owner }.currency}" else limitLabel(mode),true)
                            field("cap","Optional maximum % of goal (0–100)",true)
                            val preview=remember(fields.toMap()) { runCatching { transform(data).allocate() }.getOrNull() }
                            val goal=data.goals.first { it.id==editor.owner }
                            if(preview!=null) {
                                val key="${fields.getValue("bucket")}/${editor.owner}"
                                Notice("Preview: ${money(data.convert(preview.byConnection[key] ?: ZERO,"USD",goal.currency),goal.currency)} from this bucket.\nGoal funded: ${money(data.convert(preview.goal(goal.id),"USD",goal.currency),goal.currency)} of ${money(goal.target.decimal(),goal.currency)}${if(preview.incomplete) "\nIncomplete: missing rates or balances." else ""}")
                            }
                            Note("Limits are ceilings. Priority, available savings, and other connections can reduce the contribution.")
                        }
                        "Settings" -> Choice("Appearance",fields.getValue("theme"),listOf("System","Light","Dark"),!saving) { fields["theme"]=it }
                    }
                    val oldCurrency=if(editor.kind=="Holding") h?.asset else if(editor.kind=="Goal") g?.currency else null
                    if(oldCurrency!=null && oldCurrency!=fields.getValue("currency").trim().uppercase() && (editor.kind!="Holding" || fields.getValue("type")=="Manual")) {
                        Choice("Currency changed from $oldCurrency",fields.getValue("currencyAction"),listOf("Convert existing values","Replace with entered numbers"),!saving) { fields["currencyAction"]=it }
                        Note("Convert uses the saved amount and cached rates. Replace treats entered numbers (including fixed goal connection limits) as values in the new currency.")
                    }
                    Note("Changes stay in this editor until Save. Cancel discards them.")
                }
            }
        }
        BackHandler { close() }
        if(discard) AlertDialog(onDismissRequest={ discard=false },title={ Text("Discard edits?") },confirmButton={ TextButton(onClick=onDismiss) { Text("Discard") } },dismissButton={ TextButton(onClick={ discard=false }) { Text("Keep editing") } })
    }
}
