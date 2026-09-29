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
import androidx.compose.ui.res.painterResource
import dev.capital.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.zIndex
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
import java.time.temporal.ChronoUnit
import java.util.Currency

private val light=lightColorScheme(primary=Color(0xff176451),onPrimary=Color.White,background=Color(0xfff8faf5),surface=Color(0xfff8faf5),surfaceVariant=Color(0xffe2eee7),onSurface=Color(0xff192d2a),onSurfaceVariant=Color(0xff52645b))
private val dark=darkColorScheme(primary=Color(0xff8fd3b0),onPrimary=Color(0xff123425),background=Color(0xff101b18),surface=Color(0xff182722),surfaceVariant=Color(0xff264b39),onSurface=Color(0xffe3eae3),onSurfaceVariant=Color(0xffb3c4b9))
private data class Editor(val kind: String,val id: String="",val owner: String="")
private fun money(value: BigDecimal?,asset: String): String {
    if(value==null) return "Unavailable · ${assetLabel(asset)}"
    // Chain assets are never formatted as fiat, even where the platform knows a currency code like BTC.
    val fiat=if(asset in Chain.entries.map { it.name }) null else runCatching { Currency.getInstance(asset) }.getOrNull()
    return if(fiat != null) NumberFormat.getCurrencyInstance().apply { currency=fiat; maximumFractionDigits=fiat.defaultFractionDigits.coerceAtLeast(0) }.format(value)
    else "${NumberFormat.getNumberInstance().apply { maximumFractionDigits=18 }.format(value)} ${assetLabel(asset)}"
}
private fun tokenQty(value: BigDecimal,symbol: String)=listOf(NumberFormat.getNumberInstance().apply { maximumFractionDigits=18 }.format(value),symbol).filter { it.isNotBlank() }.joinToString(" ")
private fun shortContract(chain: String,contract: String)=providerAddress(Chain.valueOf(chain),contract).let { if(it.length>14) it.take(8)+"…"+it.takeLast(6) else it }
private fun time(value: Long?): String = value?.let { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())) } ?: "Never refreshed"
private fun date(value: String): String=runCatching { LocalDate.parse(value).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) }.getOrDefault(value)
@Composable private fun Heading(value: String) { Text(value,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(top=20.dp,bottom=8.dp)) }
@Composable private fun Note(value: String) { Text(value,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) }
// Overlay message: never moves content. Goes away on tap, on a swipe in any direction, or after a reading-time timer.
@Composable private fun Popup(message: String,onDismiss: ()->Unit) {
    var offset by remember(message) { mutableStateOf(Offset.Zero) }
    LaunchedEffect(message) { delay((4000L+message.length*40L).coerceAtMost(15000L)); onDismiss() }
    Surface(color=MaterialTheme.colorScheme.inverseSurface,contentColor=MaterialTheme.colorScheme.inverseOnSurface,shape=MaterialTheme.shapes.small,shadowElevation=6.dp,
        modifier=Modifier.padding(horizontal=16.dp,vertical=8.dp).fillMaxWidth().heightIn(min=48.dp)
            .graphicsLayer { translationX=offset.x; translationY=offset.y; alpha=(1f-offset.getDistance()/(240.dp.toPx())).coerceIn(0.2f,1f) }
            .pointerInput(message) { detectDragGestures(onDragEnd={ if(offset.getDistance()>80.dp.toPx()) onDismiss() else offset=Offset.Zero },onDragCancel={ offset=Offset.Zero }) { change,drag -> change.consume(); offset+=drag } }
            .clickable(onClickLabel="Dismiss message") { onDismiss() }
            .semantics { liveRegion=LiveRegionMode.Polite }.testTag("message")) {
        Text(message,Modifier.padding(horizontal=16.dp,vertical=10.dp),style=MaterialTheme.typography.bodySmall,maxLines=6,overflow=TextOverflow.Ellipsis)
    }
}
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
private fun projectionLine(g: Goal,p: Projection,data: Portfolio): String {
    if(g.archived) return ""
    fun m(usd: BigDecimal)=money(data.convert(usd,"USD",g.currency),g.currency)
    val target=p.targets[g.id] ?: return "Projection unavailable: missing rate"
    val closes=p.closes[g.id]; val reached=p.reached[g.id]; val final=p.final[g.id]
    val line=when {
        (p.now[g.id] ?: ZERO)>=target -> "Funded now"
        closes!=null -> "Planned savings close this goal on ${date(closes)}"+LocalDate.parse(closes).let { c -> if(c<=LocalDate.parse(g.due)) " · on time" else " · ${ChronoUnit.DAYS.between(LocalDate.parse(g.due),c)} days after the due date" }
        reached!=null && final!=null -> "Planned savings cover up to ${m(final)} of ${m(target)} by ${date(reached)} · short by ${m(target-final)}"
        else -> "No planned savings reach this goal"
    }
    return if(p.incomplete) "$line · projection incomplete" else line
}
@Composable private fun GoalRow(goal: Goal,data: Portfolio,allocation: Allocation,projection: Projection,onClick: ()->Unit) {
    val funded=data.convert(allocation.goal(goal.id),"USD",goal.currency)
    val progress=funded?.divideMoney(goal.target.decimal())?.toFloat()?.coerceIn(0f,1f) ?: 0f
    val status=when { goal.archived -> "Archived"; funded != null && funded >= goal.target.decimal() -> "Funded"; LocalDate.parse(goal.due)<LocalDate.now() -> "Overdue"; else -> "Due ${date(goal.due)}" }
    Item(goal.name,status,"${money(funded,goal.currency)} / ${money(goal.target.decimal(),goal.currency)}",onClick)
    LinearProgressIndicator(progress={ progress },modifier=Modifier.fillMaxWidth().padding(top=6.dp,bottom=if(goal.archived) 14.dp else 4.dp))
    projectionLine(goal,projection,data).takeIf { it.isNotEmpty() }?.let { Note(it); Spacer(Modifier.height(10.dp)) }
}
// Dragging a handle reorders a local copy; release commits the net move as one edit. Rows are keyed so the gesture survives reordering.
@Composable private fun GoalGroup(goals: List<Goal>,data: Portfolio,allocation: Allocation,projection: Projection,editable: Boolean,onOpen: (String)->Unit,onMove: (String,Boolean,Int)->Unit) {
    val ids=goals.map { it.id }
    var order by remember(ids) { mutableStateOf(ids) }
    var dragging by remember { mutableStateOf<String?>(null) }
    var offset by remember { mutableFloatStateOf(0f) }
    val heights=remember { mutableStateMapOf<String,Int>() }
    order.forEach { id -> key(id) {
        val goal=goals.first { it.id==id }
        val at=order.indexOf(id)
        val lift=if(dragging==id) Modifier.zIndex(1f).graphicsLayer { translationY=offset } else Modifier
        Row(Modifier.fillMaxWidth().onGloballyPositioned { heights[id]=it.size.height }.then(lift),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { GoalRow(goal,data,allocation,projection) { onOpen(id) } }
            if(ids.size>1) Box(Modifier.size(48.dp).testTag("drag-${goal.name}").semantics {
                contentDescription="Reorder ${goal.name}"
                if(editable) customActions=listOfNotNull(
                    if(at>0) CustomAccessibilityAction("Move up") { onMove(id,true,1); true } else null,
                    if(at<ids.size-1) CustomAccessibilityAction("Move down") { onMove(id,false,1); true } else null,
                )
            }.then(if(editable) Modifier.pointerInput(id,ids) {
                detectDragGestures(
                    onDragStart={ dragging=id; offset=0f },
                    onDrag={ change,amount ->
                        change.consume(); offset+=amount.y
                        while(true) {
                            val from=order.indexOf(id); val dir=if(offset>0) 1 else -1
                            val next=order.getOrNull(from+dir) ?: break
                            val h=(heights[next] ?: break).toFloat()
                            if(kotlin.math.abs(offset)<=h/2) break
                            order=order.toMutableList().apply { removeAt(from); add(from+dir,id) }; offset-=dir*h
                        }
                    },
                    onDragEnd={ val moved=order.indexOf(id)-ids.indexOf(id); dragging=null; offset=0f; if(moved!=0) onMove(id,moved<0,kotlin.math.abs(moved)) },
                    onDragCancel={ dragging=null; offset=0f; order=ids },
                )
            } else Modifier),contentAlignment=Alignment.Center) { Text("≡",style=MaterialTheme.typography.titleLarge) }
        }
    } }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun CapitalApp(model: CapitalModel) {
    val state by model.state.collectAsStateWithLifecycle()
    val data=state.data
    val allocation=remember(data) { data.allocate() }
    val projection=remember(data) { data.project() }
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
    fun move(id: String,up: Boolean,times: Int) { model.edit({ p -> (1..times).fold(p) { acc,_ -> acc.moveGoal(id,up) } }) }
    fun select(value: String) { section=value; bucketId=null; goalId=null }
    val editable=state.ready && !state.blocked && !state.unsaved && !state.saving
    BackHandler(editor==null && (bucketId!=null || goalId!=null || section!="Overview")) { if(bucketId!=null || goalId!=null) { bucketId=null; goalId=null } else section="Overview" }
    MaterialTheme(colorScheme=scheme) {
        BoxWithConstraints {
            val wide=maxWidth>=840.dp
            Scaffold(
                topBar={ TopAppBar(title={ Text(if(!state.ready) "Capital" else section) },actions={
                    if(state.ready) {
                        if(state.refreshing) Box(Modifier.size(48.dp).semantics { contentDescription="Refreshing" },contentAlignment=Alignment.Center) { CircularProgressIndicator(Modifier.size(24.dp),strokeWidth=2.dp) }
                        else IconButton(onClick={ model.refresh(bucketId) },enabled=editable,modifier=Modifier.testTag("refresh")) { Icon(painterResource(R.drawable.ic_refresh),contentDescription="Refresh") }
                        IconButton(onClick={ select("Settings") },modifier=Modifier.testTag("settings")) { Icon(painterResource(R.drawable.ic_settings),contentDescription="Settings") }
                    }
                }) },
                bottomBar={ if(state.ready && !wide) NavigationBar { listOf("Overview","Buckets","Goals").forEach { target -> NavigationBarItem(selected=section==target,onClick={ select(target) },icon={ Text(when(target) { "Overview" -> "◫"; "Buckets" -> "▤"; else -> "◎" },Modifier.clearAndSetSemantics {}) },label={ Text(target) }) } } },
                snackbarHost={ state.message?.let { Popup(it,model::dismissMessage) } },
                floatingActionButton={
                    val add=when { !state.ready || !editable -> null; section=="Buckets" && bucketId==null -> "Bucket"; section=="Goals" && goalId==null -> "Goal"; else -> null }
                    add?.let { kind -> ExtendedFloatingActionButton(onClick={ editor=Editor(kind) }) { Text("Add ${kind.lowercase()}") } }
                },
                floatingActionButtonPosition=FabPosition.Center,
            ) { padding ->
                Row(Modifier.fillMaxSize().padding(padding)) {
                    if(wide && state.ready) NavigationRail { listOf("Overview","Buckets","Goals","Settings").forEach { target -> NavigationRailItem(selected=section==target,onClick={ select(target) },icon={ Text(target.take(1),Modifier.clearAndSetSemantics {}) },label={ Text(target) }) } }
                    if(wide && ((section=="Buckets" && bucketId!=null) || (section=="Goals" && goalId!=null))) {
                        Column(Modifier.width(250.dp).fillMaxHeight().verticalScroll(rememberScrollState()).padding(16.dp)) {
                            if(section=="Buckets") data.buckets.forEach { b -> Item(b.name,b.currency) { bucketId=b.id } }
                            else data.goals.sortedWith(compareBy<Goal> { it.archived }.thenBy { it.due }.thenByDescending { it.priority }).forEach { g -> Item(g.name,date(g.due)) { goalId=g.id } }
                        }
                    }
                    Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(screenScroll).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        if(state.loading) { LinearProgressIndicator(Modifier.fillMaxWidth()); Text("Opening your savings…") }
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
                            Notice("Public wallet providers receive your addresses, token contract addresses and IP. Never enter private keys or seed phrases. Files in the chosen folder contain your financial records.")
                            Button(onClick={ copyFolder=false; folderPicker.launch(null) }) { Text("Choose or reopen folder") }
                            Note("Default currency starts as EUR. Change it and configure optional free provider keys in Settings.")
                        } else if(state.ready) {
                            if(data.stale() || allocation.incomplete || data.incomplete(data.settings.currency)) Notice(if(allocation.incomplete || data.incomplete(data.settings.currency)) "Incomplete valuation — some balances or rates are unavailable. Native quantities remain visible." else "Cached / stale values — refresh when online. Allocations are estimates.")
                            when(section) {
                                "Overview" -> {
                                    val currency=data.settings.currency
                                    val values=data.buckets.filter { b -> data.holdings.any { it.bucketId==b.id } }.map { data.bucketValueOrNull(it.id,currency) }
                                    val total=if(values.isNotEmpty() && values.all { it==null }) null else values.filterNotNull().fold(ZERO,BigDecimal::add)
                                    Note("TOTAL VALUED SAVINGS · $currency")
                                    Text(money(total,currency),style=MaterialTheme.typography.displaySmall,fontWeight=FontWeight.SemiBold)
                                    Stat("Allocated to goals",money(data.convert(allocation.total,"USD",currency),currency))
                                    Stat("Unallocated",money(data.convert(allocation.capacities.values.fold(ZERO,BigDecimal::add)-allocation.total,"USD",currency),currency))
                                    if(data.buckets.isEmpty()) { Text("Add your first bucket to start tracking savings."); Button(onClick={ editor=Editor("Bucket") },enabled=editable) { Text("Add bucket") } }
                                    Heading("Your goals")
                                    if(data.goals.none { !it.archived }) { Text("Give your savings a purpose."); OutlinedButton(onClick={ editor=Editor("Goal") },enabled=editable) { Text("Add goal") } }
                                    data.goals.filterNot { it.archived }.sortedBy { it.due }.forEach { GoalRow(it,data,allocation,projection) { section="Goals"; goalId=it.id } }
                                    Heading("Buckets")
                                    data.buckets.forEach { b -> Item(b.name,"${data.holdings.count { it.bucketId==b.id }} holdings · ${b.currency}",money(data.bucketValueOrNull(b.id,b.currency),b.currency)) { section="Buckets"; bucketId=b.id } }
                                    Note("Goal allocations are part of savings, not additional money.")
                                }
                                "Buckets" -> {
                                    val bucket=data.buckets.find { it.id==bucketId }
                                    if(bucket==null) {
                                        if(data.buckets.isEmpty()) Text("Buckets group places where your money lives.")
                                        data.buckets.forEach { b -> Item(b.name,"${data.holdings.count { it.bucketId==b.id }} holdings",money(data.bucketValueOrNull(b.id,b.currency),b.currency)) { bucketId=b.id } }
                                        Heading("Planned savings")
                                        Note("Planned amounts are not part of your savings. They project when goals close.")
                                        Button(onClick={ editor=Editor("Planned") },enabled=editable) { Text("Add planned saving") }
                                        val today=LocalDate.now()
                                        data.planned.sortedWith(compareBy<Planned> { it.archived(today) }.thenBy { it.date }).forEach { pl ->
                                            Item(pl.name,"${date(pl.date)} · ${if(pl.archived(today)) "Archived · date passed" else "Planned"}",money(pl.amount.decimal(),pl.currency),null)
                                            Actions {
                                                TextButton(onClick={ editor=Editor("Planned",pl.id) },enabled=editable) { Text("Edit") }
                                                TextButton(onClick={ confirmation="Delete planned saving ${pl.name}?" to { model.edit({ p -> p.copy(planned=p.planned.filterNot { it.id==pl.id }) }) } },enabled=editable) { Text("Delete") }
                                            }
                                        }
                                    } else {
                                        TextButton(onClick={ bucketId=null }) { Text("← All buckets") }
                                        Heading(bucket.name)
                                        Text(money(data.bucketValueOrNull(bucket.id,bucket.currency),bucket.currency),style=MaterialTheme.typography.headlineLarge)
                                        Stat("Allocated",money(data.convert(allocation.bucket(bucket.id),"USD",bucket.currency),bucket.currency))
                                        Stat("Available",money(data.convert((allocation.capacities[bucket.id] ?: ZERO)-allocation.bucket(bucket.id),"USD",bucket.currency),bucket.currency))
                                        Actions {
                                            OutlinedButton(onClick={ editor=Editor("Bucket",bucket.id) },enabled=editable) { Text("Edit bucket") }
                                            TextButton(onClick={ confirmation="Delete ${bucket.name}, its ${data.holdings.count { it.bucketId==bucket.id }} holdings and ${data.connections.count { it.bucketId==bucket.id }} goal connections? Previous snapshots remain recoverable." to { model.edit({ it.deleteBucket(bucket.id) }) { bucketId=null } } },enabled=editable) { Text("Delete bucket") }
                                        }
                                        Heading("Holdings")
                                        Button(onClick={ editor=Editor("Holding",owner=bucket.id) },enabled=editable) { Text("Add holding") }
                                        data.holdings.filter { it.bucketId==bucket.id }.forEach { h ->
                                            val wallet=h.address!=null; val cur=bucket.currency
                                            val (known,unknown)=h.tokens.filter { it.contract !in h.excluded }.partition { data.known(h,it) }
                                            val native=h.quantity?.let { data.convert(it.decimal(),h.asset,cur) }
                                            val total=(listOfNotNull(native)+known.mapNotNull { data.convert(it.quantity()!!,tokenAsset(h.asset,it.contract),cur) }).takeIf { it.isNotEmpty() }?.fold(ZERO,BigDecimal::add)
                                            if(wallet) Item(h.label,"Read-only · ${h.address.orEmpty().take(12)}…",if(h.quantity==null) "Balance unknown" else money(total,cur),null)
                                            else Item(h.label,"Manual\n${h.quantity?.let { money(it.decimal(),h.asset) } ?: "Balance unknown"}",money(native,cur),null)
                                            Note("${h.source} · observed ${time(h.observedAt)} · fetched ${time(h.fetchedAt)}")
                                            h.error?.let { Notice(it) }
                                            h.tokensError?.let { Notice("Tokens: $it") }
                                            Actions { TextButton(onClick={ editor=Editor("Holding",h.id,bucket.id) },enabled=editable) { Text("Edit / move") }; TextButton(onClick={ confirmation="Delete ${h.label}?" to { model.edit({ p -> p.copy(holdings=p.holdings.filterNot { it.id==h.id }) }) } },enabled=editable) { Text("Delete") } }
                                            h.tokens.count { it.contract in h.excluded }.takeIf { it>0 }?.let { Note("$it ${if(it==1) "token" else "tokens"} excluded in the holding editor") }
                                            if(wallet) Item(assetLabel(h.asset),"Native · ${h.quantity?.let { money(it.decimal(),h.asset) } ?: "balance unknown"}",h.quantity?.let { money(native,cur) },null)
                                            var all by remember(h.id) { mutableStateOf(false) }
                                            known.sortedByDescending { data.convert(it.quantity()!!,tokenAsset(h.asset,it.contract),cur) ?: ZERO }.forEach { t ->
                                                Item(t.symbol.ifBlank { shortContract(h.asset,t.contract) },"Token · ${tokenQty(t.quantity()!!,t.symbol)} · ${shortContract(h.asset,t.contract)}",money(data.convert(t.quantity()!!,tokenAsset(h.asset,t.contract),cur),cur),null)
                                            }
                                            val sortedUnknown=unknown.sortedWith(compareBy<Token> { it.symbol }.thenBy { it.contract })
                                            (if(all) sortedUnknown else sortedUnknown.take(5)).forEach { t ->
                                                Item("Unknown token","Not counted · reported as “${t.symbol.ifBlank { t.name.ifBlank { "unnamed" } }}” · ${shortContract(h.asset,t.contract)} · ${t.quantity()?.let { tokenQty(it,"") } ?: "raw units ${t.units}"}",null,null)
                                            }
                                            if(sortedUnknown.size>5) TextButton(onClick={ all=!all }) { Text(if(all) "Show fewer" else "Show ${sortedUnknown.size-5} more unknown tokens") }
                                        }
                                        Heading("Connected goals")
                                        data.connections.filter { it.bucketId==bucket.id }.forEach { c -> val g=data.goals.first { it.id==c.goalId }; Item(g.name,limitLabel(c.mode),money(data.convert(allocation.byConnection[c.key] ?: ZERO,"USD",g.currency),g.currency)) { section="Goals"; goalId=g.id } }
                                        Note("Automatic tracking covers native balances and tokens on ETH, TON and TRX addresses. Tokens count only when the price provider lists their contract. Add staking or other investments as manual holdings.")
                                    }
                                }
                                "Goals" -> {
                                    val goal=data.goals.find { it.id==goalId }
                                    if(goal==null) {
                                        if(data.goals.isEmpty()) Text("Create a goal with a target, currency and due date.")
                                        val today=LocalDate.now()
                                        data.goals.filterNot { it.archived }.sortedWith(compareBy<Goal> { it.due }.thenByDescending { it.priority }).groupBy { it.due }.forEach { (due,group) ->
                                            Heading(date(due)+if(LocalDate.parse(due)<today) " · overdue" else "")
                                            GoalGroup(group,data,allocation,projection,editable,{ goalId=it },::move)
                                        }
                                        if(data.goals.any { it.archived }) Heading("Archived")
                                        data.goals.filter { it.archived }.forEach { GoalRow(it,data,allocation,projection) { goalId=it.id } }
                                    } else {
                                        TextButton(onClick={ goalId=null }) { Text("← All goals") }
                                        GoalRow(goal,data,allocation,projection) { if(editable) editor=Editor("Goal",goal.id) }
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
                                            Note(when { goal.archived -> "Archived goals reserve no funds."; allocation.incomplete -> "Missing rates or balances can limit funding."; free>ZERO && funded!=null && funded<goal.target.decimal() -> "Available savings remain. Increase this connection's limit to allocate more."; amount==ZERO && funded!=null && funded>=goal.target.decimal() -> "Goal target is covered by other buckets."; amount==ZERO -> "No capacity left after higher-ranked goals or connection limits."; else -> "Contribution respects goal order and connection limits." })
                                            TextButton(onClick={ confirmation="Disconnect ${b.name} from ${goal.name}?" to { model.edit({ p -> p.copy(connections=p.connections.filterNot { it.key==c.key }) }) } },enabled=editable) { Text("Disconnect") }
                                        }
                                        Note("Earlier dates are funded first; within a date, the order shown. Allocation never moves money.")
                                        Heading("Planned savings")
                                        val contributions=projection.contributions[goal.id].orEmpty()
                                        if(contributions.isEmpty()) Note("No planned savings reach this goal.")
                                        contributions.forEach { c -> data.planned.find { it.id==c.plannedId }?.let { pl -> Item(pl.name,date(pl.date),"+${money(data.convert(c.usd,"USD",goal.currency),goal.currency)}",null) } }
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
                                        if(!category.endsWith(" tokens") && provider in listOf("Alchemy","TronGrid","CoinGecko","TON Center")) TextButton(onClick={ keyProvider=provider }) { Text("${if(provider=="TON Center") "Optional" else "Required free"} key: $provider") }
                                    }
                                    OutlinedButton(onClick={ model.refresh() },enabled=editable && !state.refreshing) { Text("Test sources / refresh portfolio") }
                                    Note("Tests query only assets and addresses in your portfolio. No silent provider fallback. Token sources and price providers also receive wallet or token contract addresses; choose Off to stop token lookups for a chain.")
                                    Heading("Quotes and freshness")
                                    if(data.quotes.isEmpty()) Note("No cached quotes. Add holdings and refresh.")
                                    data.quotes.count { ':' in it.asset }.takeIf { it>0 }?.let { Note("$it token prices by contract address are shown with their wallets.") }
                                    data.quotes.filter { ':' !in it.asset }.forEach { q -> Text("${q.asset}: ${money(q.usd.decimal(),"USD")}"); Note("${q.source} · observed ${time(q.observedAt)} · fetched ${time(q.fetchedAt)}${q.error?.let { "\n$it" } ?: ""}") }
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
                                    listOf("Blockstream" to "https://blockstream.info", "mempool.space" to "https://mempool.space", "PublicNode" to "https://publicnode.com", "Alchemy" to "https://alchemy.com", "TON Center" to "https://toncenter.com", "TonAPI" to "https://tonapi.io", "TronGrid" to "https://trongrid.io", "Blockscout" to "https://eth.blockscout.com", "Ethplorer" to "https://ethplorer.io", "DefiLlama" to "https://defillama.com", "Powered by CoinGecko" to "https://coingecko.com", "Powered by CoinPaprika" to "https://coinpaprika.com", "Frankfurter" to "https://frankfurter.dev", "European Central Bank" to "https://ecb.europa.eu").forEach { (name,url) -> TextButton(onClick={ runCatching { context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url))) }.onFailure { model.notice("No browser available") } }) { Text(name) } }
                                }
                            }
                        }
                        // Room for the pinned add button and the message pop-up.
                        Spacer(Modifier.height(96.dp))
                    }
                }
            }
        }
        editor?.let { e -> EditSheet(e,data,state.saving,if(state.unsaved && !state.saving) state.message ?: "Save failed" else null,onDismiss={ editor=null },onSave={ transform -> model.edit(transform) { editor=null } },onFetchTokens=model::fetchTokens) }
        confirmation?.let { (text,action) -> AlertDialog(onDismissRequest={ confirmation=null },title={ Text("Confirm change") },text={ Text(text) },confirmButton={ TextButton(onClick={ confirmation=null; action() }) { Text("Confirm") } },dismissButton={ TextButton(onClick={ confirmation=null }) { Text("Cancel") } }) }
        keyProvider?.let { provider -> KeyDialog(provider,onDismiss={ keyProvider=null },onSave={ if(model.saveKey(provider,it)) keyProvider=null }) }
        state.restore?.let { revision -> AlertDialog(onDismissRequest=model::cancelRestore,title={ Text("Restore backup?") },text={ Column { RevisionSummary(revision); Text("Replaces current records with this backup. Existing snapshots remain available.") } },confirmButton={ TextButton(onClick=model::restore) { Text("Restore") } },dismissButton={ TextButton(onClick=model::cancelRestore) { Text("Cancel") } }) }
    }
}
@Composable private fun RevisionSummary(revision: Revision) {
    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Text("Revision ${revision.id.take(8)} · ${time(revision.createdAt)}",fontWeight=FontWeight.SemiBold)
        Text("${revision.data.buckets.size} buckets · ${revision.data.holdings.size} holdings · ${revision.data.goals.size} goals")
        revision.data.buckets.forEach { b -> Text("${b.name}: ${money(revision.data.bucketValueOrNull(b.id,b.currency),b.currency)}") }
        revision.data.holdings.forEach { h -> Note("${h.label}: ${h.quantity ?: "unknown"} ${h.asset} · ${h.address ?: "manual"}") }
        revision.data.goals.forEach { g -> Note("${g.name}: ${money(g.target.decimal(),g.currency)} · ${date(g.due)}") }
    }
}
@Composable private fun Choice(label: String,value: String,options: List<String>,enabled: Boolean=true,onChange: (String)->Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column { Note(label); Box { OutlinedButton(onClick={ expanded=true },enabled=enabled,modifier=Modifier.fillMaxWidth()) { Text(value) }; DropdownMenu(expanded=expanded,onDismissRequest={ expanded=false }) { options.forEach { option -> DropdownMenuItem(text={ Text(option) },onClick={ expanded=false; onChange(option) }) } } } }
}
@Composable private fun Pick(label: String,selected: String?,options: List<Pair<String,String>>,enabled: Boolean=true,onChange: (String)->Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column { Note(label); Box { OutlinedButton(onClick={ expanded=true },enabled=enabled,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)) { Text(options.find { it.first==selected }?.second ?: "Choose bucket") }; DropdownMenu(expanded=expanded,onDismissRequest={ expanded=false }) { options.forEach { (key,text) -> DropdownMenuItem(text={ Text(text) },onClick={ expanded=false; onChange(key) }) } } } }
}
private fun limitLabel(mode: Limit)=when(mode) { Limit.AUTO -> "Auto — up to remaining need"; Limit.FIXED -> "Fixed amount in goal currency"; Limit.BUCKET_PERCENT -> "% of bucket"; Limit.GOAL_PERCENT -> "% of goal" }
@Composable private fun KeyDialog(provider: String,onDismiss: ()->Unit,onSave: (String)->Unit) {
    var value by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest=onDismiss,title={ Text("$provider key") },text={ Column { Text("Stored encrypted on this device. Leave blank to remove an existing key."); OutlinedTextField(value,onValueChange={ value=it },label={ Text("API key") },visualTransformation=androidx.compose.ui.text.input.PasswordVisualTransformation(),singleLine=true) } },confirmButton={ TextButton(onClick={ onSave(value) }) { Text("Save") } },dismissButton={ TextButton(onClick=onDismiss) { Text("Cancel") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun EditSheet(editor: Editor,data: Portfolio,saving: Boolean,saveError: String?,onDismiss: ()->Unit,onSave: ((Portfolio)->Portfolio)->Unit,onFetchTokens: (String,String,(List<Token>?,String?)->Unit)->Unit) {
    val b=data.buckets.find { it.id==editor.id }
    val h=data.holdings.find { it.id==editor.id }
    val g=data.goals.find { it.id==editor.id }
    val c=data.connections.find { it.key==editor.id }
    val pl=data.planned.find { it.id==editor.id }
    val newId=remember { id() }
    val initial=remember(editor) { mapOf(
        "name" to (b?.name ?: h?.label ?: g?.name ?: pl?.name ?: ""),
        "currency" to (b?.currency ?: h?.asset ?: g?.currency ?: pl?.currency ?: data.settings.currency),
        "amount" to (pl?.amount ?: ""), "date" to (pl?.date ?: LocalDate.now().plusMonths(1).toString()),
        "quantity" to (h?.quantity ?: "0"), "target" to (g?.target ?: ""),
        "due" to (g?.due ?: LocalDate.now().plusYears(1).toString()),
        "bucket" to (h?.bucketId ?: c?.bucketId ?: editor.owner.takeIf { editor.kind=="Holding" } ?: data.buckets.firstOrNull()?.id.orEmpty()),
        "address" to (h?.address ?: ""), "type" to (if(h?.address!=null) "Wallet" else "Manual"),
        "mode" to (c?.mode?.name ?: Limit.AUTO.name), "value" to (c?.value ?: "0"), "cap" to (c?.goalCap ?: ""),
        "theme" to data.settings.theme, "currencyAction" to "Choose treatment",
    ) }
    val fields=remember { mutableStateMapOf<String,String>().apply { putAll(initial) } }
    var error by remember { mutableStateOf<String?>(null) }
    var discard by remember { mutableStateOf(false) }
    var tokens by remember { mutableStateOf(h?.tokens ?: emptyList()) }
    val disabled=remember { mutableStateListOf<String>().apply { addAll(h?.excluded ?: emptyList()) } }
    var fetching by remember { mutableStateOf(false) }
    var tokenMessage by remember { mutableStateOf<String?>(null) }
    var fetched by remember { mutableStateOf(false) }
    // Tokens belong to one chain + address: a change drops them (not on first composition).
    // ponytail: changing the address and changing it back also drops stored exclusions; re-fetch restores the list.
    val tokenKey=fields["currency"].orEmpty().trim().uppercase() to fields["address"].orEmpty().trim()
    var tokenFor by remember { mutableStateOf(tokenKey) }
    LaunchedEffect(tokenKey) { if(tokenKey!=tokenFor) { tokenFor=tokenKey; tokens=emptyList(); disabled.clear(); fetched=false; tokenMessage=null } }
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
                    tokens=if(!wallet || currency==Chain.BTC.name) emptyList() else if(fetched) tokens else if(unchanged) current?.tokens.orEmpty() else emptyList(),
                    tokensError=if(wallet && !fetched && unchanged) current?.tokensError else null,
                    excluded=if(wallet && currency!=Chain.BTC.name) disabled.distinct() else emptyList(),
                )
                p.copy(holdings=if(h==null) p.holdings+item else p.holdings.map { if(it.id==h.id) item else it })
            }
            "Goal" -> {
                val item=Goal(g?.id ?: newId,name,if(convert) converted(requireNotNull(g?.target)) else number("target"),currency,fields.getValue("due"),g?.takeIf { it.due==fields.getValue("due") }?.priority ?: 0,g?.archived ?: false)
                p.copy(goals=if(g==null) p.goals+item else p.goals.map { if(it.id==g.id) item else it },connections=p.connections.map { if(convert && it.goalId==g?.id && it.mode==Limit.FIXED) it.copy(value=converted(it.value)) else it })
            }
            "Planned" -> {
                val item=Planned(pl?.id ?: newId,name,number("amount"),currency,fields.getValue("date"))
                p.copy(planned=if(pl==null) p.planned+item else p.planned.map { if(it.id==pl.id) item else it })
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
    @Composable fun dateField(key: String,label: String) {
        field(key,label)
        OutlinedButton(onClick={ val d=runCatching { LocalDate.parse(fields.getValue(key)) }.getOrDefault(LocalDate.now()); DatePickerDialog(context,{ _,y,m,day -> fields[key]=LocalDate.of(y,m+1,day).toString() },d.year,d.monthValue-1,d.dayOfMonth).show() }) { Text("Choose date") }
    }
    @Composable fun TokenEditor() {
        val chain=Chain.entries.find { it.name==fields.getValue("currency").trim().uppercase() }?.takeIf { it!=Chain.BTC } ?: return
        Heading("Tokens")
        Note("Fetch the tokens held by this address and switch off the ones you do not want listed or counted.")
        OutlinedButton(onClick={
            val asked=fields["currency"] to fields["address"]
            try {
                val address=canonicalAddress(chain,fields.getValue("address"))
                fetching=true; tokenMessage=null
                onFetchTokens(chain.name,address) { list,message ->
                    fetching=false
                    if(asked==(fields["currency"] to fields["address"])) { if(list!=null) { tokens=list; fetched=true } else tokenMessage=message }
                }
            } catch(e: Exception) { tokenMessage=e.message }
        },enabled=!saving && !fetching && fields.getValue("address").isNotBlank(),modifier=Modifier.testTag("fetch-tokens").heightIn(min=48.dp)) { Text("Fetch tokens") }
        if(fetching) Note("Fetching tokens…")
        tokenMessage?.let { Notice(it) }
        if(fetched && tokens.isEmpty()) Note("No tokens found on this address.")
        tokens.forEach { t ->
            val short=shortContract(chain.name,t.contract)
            val priced=data.price(tokenAsset(chain.name,t.contract))!=null && t.quantity()!=null
            val title=if(priced) t.symbol.ifBlank { short } else "Unknown token"
            Row(Modifier.fillMaxWidth().heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(title,style=MaterialTheme.typography.titleMedium)
                    Note("${t.quantity()?.let { tokenQty(it,t.symbol) } ?: "raw units ${t.units}"} · $short"+if(priced) "" else " · reported as “${t.symbol.ifBlank { t.name.ifBlank { "unnamed" } }}”")
                }
                Switch(checked=t.contract !in disabled,onCheckedChange={ on -> if(on) disabled.remove(t.contract) else if(t.contract !in disabled) disabled.add(t.contract) },enabled=!saving,modifier=Modifier.testTag("token-${t.contract.take(10)}").semantics { contentDescription="Include $title $short" })
            }
        }
    }
    Dialog(onDismissRequest={ close() },properties=DialogProperties(usePlatformDefaultWidth=false,dismissOnClickOutside=false)) {
        Surface(Modifier.fillMaxSize().semantics { testTagsAsResourceId=true },color=MaterialTheme.colorScheme.background) {
            Scaffold(topBar={ TopAppBar(title={ Text(if(editor.kind=="Settings") "Currency and appearance" else "${if(editor.id.isBlank()) "Add" else "Edit"} ${if(editor.kind=="Planned") "planned saving" else editor.kind.lowercase()}") },navigationIcon={ TextButton(onClick={ close() },enabled=!saving) { Text("Cancel") } },actions={ TextButton(onClick={
                val checked=runCatching { transform(data) }
                if(checked.isFailure) error=checked.exceptionOrNull()?.message else onSave(::transform)
            },enabled=!saving) { Text(if(saving) "Saving…" else "Save") } }) }) { padding ->
                Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                    error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
                    if(saveError!=null) {
                        Notice("$saveError\nYour changes remain in memory.")
                        Button(onClick=onDismiss) { Text("Open save recovery") }
                    }
                    if(editor.kind in listOf("Bucket","Holding","Goal","Planned")) field("name",if(editor.kind=="Goal") "Purpose" else "Name")
                    if(editor.kind=="Holding") Choice("Tracking",fields.getValue("type"),listOf("Manual","Wallet"),!saving) { fields["type"]=it; if(it=="Wallet" && fields.getValue("currency") !in Chain.entries.map { chain -> chain.name }) fields["currency"]="BTC" }
                    if(editor.kind!="Connection") {
                        if(editor.kind=="Holding" && fields.getValue("type")=="Wallet") Choice("Mainnet chain",fields.getValue("currency"),Chain.entries.map { it.name },!saving) { fields["currency"]=it }
                        else { field("currency","Currency code (EUR, USD, BTC…)"); Note("Use an ISO currency code or a supported native asset. Conversion needs a quote from your selected provider.") }
                    }
                    if(editor.kind in listOf("Holding","Connection")) {
                        val options=data.buckets.map { x ->
                            val text=if(data.buckets.count { it.name==x.name }>1) "${x.name} · ${x.currency}" else x.name
                            val twins=data.buckets.filter { it.name==x.name && it.currency==x.currency }
                            x.id to if(twins.size>1) "$text (${twins.indexOf(x)+1})" else text
                        }
                        Pick("Bucket",fields["bucket"],options,!saving) { fields["bucket"]=it }
                    }
                    when(editor.kind) {
                        "Holding" -> if(fields.getValue("type")=="Wallet") { field("address","Public wallet address"); Note("Paste one address. No seed phrase, private key or HD wallet discovery."); TokenEditor() } else field("quantity","Current quantity · no grouping separators",true)
                        "Goal" -> {
                            field("target","Target amount · no grouping separators",true)
                            dateField("due","Due date · YYYY-MM-DD")
                        }
                        "Planned" -> { field("amount","Amount · no grouping separators",true); dateField("date","Planned date · YYYY-MM-DD") }
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
                            Note("Limits are ceilings. Goal order, available savings, and other connections can reduce the contribution.")
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
