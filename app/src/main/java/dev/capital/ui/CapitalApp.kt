package dev.capital.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.draw.rotate
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.capital.CapitalModel
import dev.capital.data.Revision
import dev.capital.Tips
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
// Stored values and state keys are translated only when shown.
private fun shown(v: String)=when(v) {
    "Overview" -> tr("Overview"); "Buckets" -> tr("Buckets"); "Goals" -> tr("Goals"); "Plans" -> tr("Plans"); "Settings" -> tr("Settings"); "Tips" -> tr("Tip the developer")
    "System" -> tr("System"); "Light" -> tr("Light"); "Dark" -> tr("Dark"); "Off" -> tr("Off"); "Manual" -> tr("Manual"); "Wallet" -> tr("Wallet"); "Account" -> tr("Broker account")
    "Not refreshed" -> tr("Not refreshed"); "Unknown" -> tr("Unknown"); "Choose treatment" -> tr("Choose treatment")
    "Convert existing values" -> tr("Convert existing values"); "Replace with entered numbers" -> tr("Replace with entered numbers")
    "ETH tokens" -> tr("ETH tokens"); "TON tokens" -> tr("TON tokens"); "TRX tokens" -> tr("TRX tokens"); "Crypto" -> tr("Crypto"); "Fiat" -> tr("Fiat")
    "Powered by CoinGecko" -> tr("Powered by CoinGecko"); "Powered by CoinPaprika" -> tr("Powered by CoinPaprika")
    else -> v
}
// Secrets names shown as buttons and dialog titles.
private fun credentialLabel(name: String)=when(name) {
    "Trading 212" -> tr("API key: Trading 212"); "Trading 212 secret" -> tr("API secret: Trading 212")
    "SnapTrade" -> tr("Client id: SnapTrade"); "SnapTrade consumer key" -> tr("Consumer key: SnapTrade")
    else -> tr("Access token: {0}",name)
}
private fun editorTitle(kind: String,new: Boolean)=when(kind) {
    "Settings" -> tr("Currency and appearance")
    "Bucket" -> if(new) tr("Add bucket") else tr("Edit bucket")
    "Holding" -> if(new) tr("Add holding") else tr("Edit holding")
    "Goal" -> if(new) tr("Add goal") else tr("Edit goal")
    "Planned" -> if(new) tr("Add planned saving") else tr("Edit planned saving")
    "Connection" -> if(new) tr("Add connection") else tr("Edit connection")
    else -> kind
}
private fun money(value: BigDecimal?,asset: String): String {
    if(value==null) return tr("Unavailable · {0}",assetLabel(asset))
    // Chain assets are never formatted as fiat, even where the platform knows a currency code like BTC.
    val fiat=if(asset in Chain.entries.map { it.name }) null else runCatching { Currency.getInstance(asset) }.getOrNull()
    return if(fiat != null) NumberFormat.getCurrencyInstance(I18n.locale).apply { currency=fiat; maximumFractionDigits=fiat.defaultFractionDigits.coerceAtLeast(0) }.format(value)
    else "${NumberFormat.getNumberInstance(I18n.locale).apply { maximumFractionDigits=18 }.format(value)} ${assetLabel(asset)}"
}
// Digits worth showing for a trade quantity: currency minor units, at most 8 for coins, 6 for tokens.
private fun assetDigits(asset: String)=Chain.entries.find { it.name==asset }?.let { minOf(it.decimals,8) } ?: if(':' in asset) 6 else runCatching { Currency.getInstance(asset).defaultFractionDigits.coerceAtLeast(0) }.getOrDefault(2)
private fun tokenQty(value: BigDecimal,symbol: String)=listOf(NumberFormat.getNumberInstance(I18n.locale).apply { maximumFractionDigits=18 }.format(value),symbol).filter { it.isNotBlank() }.joinToString(" ")
private fun shortContract(chain: String,contract: String)=providerAddress(Chain.valueOf(chain),contract).let { if(it.length>14) it.take(8)+"…"+it.takeLast(6) else it }
private fun assetName(data: Portfolio,asset: String): String {
    if(':' !in asset) return assetLabel(asset)
    val chain=asset.substringBefore(':'); val contract=asset.substringAfter(':')
    val symbol=data.holdings.filter { it.asset==chain }.flatMap { h -> h.tokens.filter { it.contract==contract && data.known(h,it) } }.firstOrNull()?.symbol?.takeIf { it.isNotBlank() }
    return symbol?.let { "$it · "+shortContract(chain,contract) } ?: tr("Token {0}",shortContract(chain,contract))
}
internal fun time(value: Long?): String = value?.let { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(I18n.locale).format(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())) } ?: tr("Never refreshed")
private fun date(value: String): String=runCatching { LocalDate.parse(value).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(I18n.locale)) }.getOrDefault(value)
@Composable internal fun Heading(value: String) { Text(value,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(top=20.dp,bottom=8.dp)) }
@Composable internal fun Note(value: String,modifier: Modifier=Modifier) { Text(value,modifier,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) }
// Overlay message: never moves content. Goes away on tap, on a swipe in any direction, or after a reading-time timer.
@Composable internal fun Popup(message: String,onDismiss: ()->Unit) {
    var offset by remember(message) { mutableStateOf(Offset.Zero) }
    LaunchedEffect(message) { delay((4000L+message.length*40L).coerceAtMost(15000L)); onDismiss() }
    Surface(color=MaterialTheme.colorScheme.inverseSurface,contentColor=MaterialTheme.colorScheme.inverseOnSurface,shape=MaterialTheme.shapes.small,shadowElevation=6.dp,
        modifier=Modifier.padding(horizontal=16.dp,vertical=8.dp).fillMaxWidth().heightIn(min=48.dp)
            .graphicsLayer { translationX=offset.x; translationY=offset.y; alpha=(1f-offset.getDistance()/(240.dp.toPx())).coerceIn(0.2f,1f) }
            .pointerInput(message) { detectDragGestures(onDragEnd={ if(offset.getDistance()>80.dp.toPx()) onDismiss() else offset=Offset.Zero },onDragCancel={ offset=Offset.Zero }) { change,drag -> change.consume(); offset+=drag } }
            .clickable(onClickLabel=tr("Dismiss message")) { onDismiss() }
            .semantics { liveRegion=LiveRegionMode.Polite }.testTag("message")) {
        Text(message,Modifier.padding(horizontal=16.dp,vertical=10.dp),style=MaterialTheme.typography.bodySmall,maxLines=6,overflow=TextOverflow.Ellipsis)
    }
}
@Composable internal fun Notice(value: String) { Surface(color=MaterialTheme.colorScheme.surfaceVariant,shape=MaterialTheme.shapes.medium) { Text(value,Modifier.fillMaxWidth().padding(16.dp),style=MaterialTheme.typography.bodyMedium) } }
@Composable internal fun Actions(content: @Composable FlowRowScope.()->Unit) { FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth(),content=content) }
@Composable private fun Stat(label: String,value: String) { Column(Modifier.padding(vertical=12.dp)) { Note(label); Text(value,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.SemiBold) } }
// One dark text colour reads on both badge backgrounds, in light and dark theme.
private val badgeGreen=Color(0xff66bb6a); private val badgeYellow=Color(0xffffd54f); private val badgeText=Color(0xff1a1a1a)
@Composable private fun Badge(text: String,color: Color) { Surface(color=color,contentColor=badgeText,shape=MaterialTheme.shapes.small) { Text(text,Modifier.padding(horizontal=8.dp,vertical=2.dp),style=MaterialTheme.typography.labelMedium,fontWeight=FontWeight.SemiBold) } }
@Composable private fun Item(title: String,subtitle: String,value: String?=null,onClick: (()->Unit)?) { BadgedItem(title,subtitle,value,null,onClick) }
@Composable private fun BadgedItem(title: String,subtitle: String,value: String?,badge: Pair<String,Color>?,onClick: (()->Unit)?) {
    val body: @Composable ()->Unit = {
        Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(4.dp),itemVerticalAlignment=Alignment.CenterVertically) {
                Text(title,style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.onSurface)
                badge?.let { Badge(it.first,it.second) }
            }
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
    val target=p.targets[g.id] ?: return tr("Projection unavailable: missing rate")
    val closes=p.closes[g.id]; val reached=p.reached[g.id]; val final=p.final[g.id]
    val line=when {
        (p.now[g.id] ?: ZERO)>=target -> tr("Funded now")
        closes!=null -> tr("Planned savings close this goal on {0}",date(closes))+" · "+LocalDate.parse(closes).let { c -> if(c<=LocalDate.parse(g.due)) tr("on time") else tr("{0} days after the due date",ChronoUnit.DAYS.between(LocalDate.parse(g.due),c)) }
        reached!=null && final!=null -> tr("Planned savings cover up to {0} of {1} by {2} · short by {3}",m(final),m(target),date(reached),m(target-final))
        else -> tr("No planned savings reach this goal")
    }
    return if(p.incomplete) line+" · "+tr("projection incomplete") else line
}
@Composable private fun GoalRow(goal: Goal,data: Portfolio,allocation: Allocation,projection: Projection,onClick: ()->Unit) {
    val funded=data.convert(allocation.goal(goal.id),"USD",goal.currency)
    val progress=funded?.divideMoney(goal.target.decimal())?.toFloat()?.coerceIn(0f,1f) ?: 0f
    val status=when { goal.archived -> tr("Archived"); funded != null && funded >= goal.target.decimal() -> tr("Funded"); LocalDate.parse(goal.due)<LocalDate.now() -> tr("Overdue"); else -> tr("Due {0}",date(goal.due)) }
    val inTime=projection.closes[goal.id]?.let { LocalDate.parse(it)<=LocalDate.parse(goal.due) }==true
    val badge=when { goal.archived -> null; funded != null && funded >= goal.target.decimal() -> tr("Funded") to badgeGreen; inTime -> tr("Funded in time") to badgeGreen; else -> tr("Not funded") to badgeYellow }
    BadgedItem(goal.name,status,"${money(funded,goal.currency)} / ${money(goal.target.decimal(),goal.currency)}",badge,onClick)
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
                contentDescription=tr("Reorder {0}",goal.name)
                if(editable) customActions=listOfNotNull(
                    if(at>0) CustomAccessibilityAction(tr("Move up")) { onMove(id,true,1); true } else null,
                    if(at<ids.size-1) CustomAccessibilityAction(tr("Move down")) { onMove(id,false,1); true } else null,
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
private fun sectionIcon(section: String)=when(section) { "Overview" -> R.drawable.ic_overview; "Buckets" -> R.drawable.ic_buckets; "Goals" -> R.drawable.ic_goals; "Plans" -> R.drawable.ic_plans; else -> R.drawable.ic_settings }
@Composable private fun RefreshButton(refreshing: Boolean,enabled: Boolean,onClick: ()->Unit) {
    val angle=if(refreshing) rememberInfiniteTransition(label="refresh").animateFloat(0f,360f,infiniteRepeatable(tween(900,easing=LinearEasing)),label="angle").value else 0f
    IconButton(onClick=onClick,enabled=enabled && !refreshing,modifier=Modifier.testTag("refresh")) { Icon(painterResource(R.drawable.ic_refresh),contentDescription=if(refreshing) tr("Refreshing") else tr("Refresh"),tint=if(refreshing) MaterialTheme.colorScheme.primary else LocalContentColor.current,modifier=Modifier.rotate(angle)) }
}
// A new language rebuilds the whole tree; Arabic and Urdu read right to left whatever the device language is.
@Composable private fun Localized(language: String,scheme: ColorScheme,content: @Composable ()->Unit) {
    key(language) { CompositionLocalProvider(LocalLayoutDirection provides if(I18n.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) { MaterialTheme(colorScheme=scheme,typography=if(I18n.rtl) rtlType else Typography(),content=content) } }
}
// Compose takes paragraph direction from the first strong character, so a line starting with "EUR" or a name would flip to left-to-right.
private val rtlType=Typography().run {
    fun TextStyle.r()=copy(textDirection=TextDirection.Rtl)
    Typography(displayLarge.r(),displayMedium.r(),displaySmall.r(),headlineLarge.r(),headlineMedium.r(),headlineSmall.r(),titleLarge.r(),titleMedium.r(),titleSmall.r(),bodyLarge.r(),bodyMedium.r(),bodySmall.r(),labelLarge.r(),labelMedium.r(),labelSmall.r())
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
    var calculator by remember { mutableStateOf<String?>(null) }
    var confirmation by remember { mutableStateOf<Pair<String,()->Unit>?>(null) }
    var copyFolder by remember { mutableStateOf(false) }
    var keyProvider by remember { mutableStateOf<String?>(null) }
    var secDialog by remember { mutableStateOf<String?>(null) }
    var lockRev by remember { mutableIntStateOf(0) }
    val folderPicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri -> uri?.let { model.chooseFolder(it,copyFolder) } }
    val backup=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { it?.let(model::export) }
    val restore=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { it?.let(model::inspectRestore) }
    val context=LocalContext.current
    var language by remember { mutableStateOf(dev.capital.Language.choice(context)) }
    val screenScroll=remember(section,bucketId,goalId) { ScrollState(0) }
    val scheme=when(data.settings.theme) { "Dark" -> dark; "Light" -> light; else -> if(isSystemInDarkTheme()) dark else light }
    fun move(id: String,up: Boolean,times: Int) { model.edit({ p -> (1..times).fold(p) { acc,_ -> acc.moveGoal(id,up) } }) }
    fun select(value: String) { section=value; bucketId=null; goalId=null }
    val editable=state.ready && !state.blocked && !state.unsaved && !state.saving
    BackHandler(editor==null && (bucketId!=null || goalId!=null || section!="Overview")) { if(bucketId!=null || goalId!=null) { bucketId=null; goalId=null } else section="Overview" }
    // Locked: only the lock screen; nothing financial is composed and open dialogs are dropped.
    LaunchedEffect(state.security) { if(state.security!="OPEN") { editor=null; calculator=null; confirmation=null; keyProvider=null; secDialog=null } }
    if(state.security!="OPEN") { Localized(language,scheme) { LockedScreen(model,state) { } }; return }
    Localized(language,scheme) {
        BoxWithConstraints(Modifier.semantics { testTagsAsResourceId=true }) {
            val wide=maxWidth>=840.dp
            Scaffold(
                topBar={ TopAppBar(title={ Text(if(!state.ready) tr("Capital") else shown(section)) },actions={
                    val uriHandler=LocalUriHandler.current
                    val screen=when { !state.ready -> "start"; section=="Buckets" && bucketId!=null -> "bucket"; section=="Goals" && goalId!=null -> "goal"; else -> section.lowercase() }
                    if(dev.capital.BuildConfig.TIPS && state.ready && section=="Overview" && Tips.shown.isNotEmpty()) TipButton { select("Tips") }
                    IconButton(onClick={ uriHandler.openUri(if(screen=="tips") I18n.siteUrl("tips") else I18n.helpUrl(screen)) },modifier=Modifier.testTag("help")) { Icon(painterResource(R.drawable.ic_help),contentDescription=tr("Help")) }
                    if(state.ready) {
                        RefreshButton(state.refreshing,editable) { model.refresh(bucketId) }
                        IconButton(onClick={ select("Settings") },modifier=Modifier.testTag("settings")) { Icon(painterResource(R.drawable.ic_settings),contentDescription=tr("Settings")) }
                    }
                }) },
                bottomBar={ if(state.ready && !wide) NavigationBar { listOf("Overview","Buckets","Goals","Plans").forEach { target -> NavigationBarItem(selected=section==target,onClick={ select(target) },icon={ Icon(painterResource(sectionIcon(target)),contentDescription=null) },label={ Text(shown(target)) }) } } },
                snackbarHost={ state.message?.let { Popup(it,model::dismissMessage) } },
                floatingActionButton={
                    val add=when { !state.ready || !editable -> null; section=="Buckets" && bucketId==null -> "Bucket"; section=="Goals" && goalId==null -> "Goal"; section=="Plans" -> "Planned"; else -> null }
                    add?.let { kind -> FloatingActionButton(onClick={ editor=Editor(kind) },modifier=Modifier.testTag("add"),containerColor=MaterialTheme.colorScheme.primary,contentColor=MaterialTheme.colorScheme.onPrimary) { Icon(painterResource(R.drawable.ic_add),contentDescription=editorTitle(kind,true)) } }
                },
            ) { padding ->
                Row(Modifier.fillMaxSize().padding(padding)) {
                    if(wide && state.ready) NavigationRail { listOf("Overview","Buckets","Goals","Plans","Settings").forEach { target -> NavigationRailItem(selected=section==target,onClick={ select(target) },icon={ Icon(painterResource(sectionIcon(target)),contentDescription=null) },label={ Text(shown(target)) }) } }
                    if(wide && ((section=="Buckets" && bucketId!=null) || (section=="Goals" && goalId!=null))) {
                        Column(Modifier.width(250.dp).fillMaxHeight().verticalScroll(rememberScrollState()).padding(16.dp)) {
                            if(section=="Buckets") data.buckets.forEach { b -> Item(b.name,b.currency) { bucketId=b.id } }
                            else data.goals.sortedWith(compareBy<Goal> { it.archived }.thenBy { it.due }.thenByDescending { it.priority }).forEach { g -> Item(g.name,date(g.due)) { goalId=g.id } }
                        }
                    }
                    Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(screenScroll).padding(start=20.dp,top=20.dp,end=20.dp,bottom=88.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        if(state.loading) { LinearProgressIndicator(Modifier.fillMaxWidth()); Text(tr("Opening your savings…")) }
                        if(state.unsaved && !state.saving) {
                            Notice(tr("Unsaved changes — kept in memory. Do not close the app before saving or exporting a copy."))
                            Actions {
                                Button(onClick={ model.retrySave() },enabled=!state.saving) { Text(tr("Retry save")) }
                                OutlinedButton(onClick={ copyFolder=true; model.expectReturn(); folderPicker.launch(null) }) { Text(tr("Save copy to folder")) }
                                TextButton(onClick={ confirmation=tr("Discard unsaved changes and reload the folder?") to { model.reload(true) } }) { Text(tr("Discard and reload")) }
                            }
                        }
                        if(state.blocked) {
                            Notice(tr("Editing is paused until storage is resolved. Cached data remains visible."))
                            OutlinedButton(onClick={ model.reload() }) { Text(tr("Reload folder")) }
                            if(state.heads.size>1) state.heads.forEach { revision ->
                                RevisionSummary(revision)
                                OutlinedButton(onClick={ confirmation=tr("Use revision {0}? Both originals remain in the folder.",revision.id.take(8)) to { model.resolve(revision) } }) { Text(tr("Keep this version")) }
                            }
                        }
                        if(!state.ready && !state.loading) {
                            Text(tr("Your savings.\nTheir purpose."),style=MaterialTheme.typography.displaySmall,fontWeight=FontWeight.SemiBold)
                            Text(tr("Track balances, connect goals, and see what is already covered. No account required."))
                            Heading(tr("Start with your folder"))
                            Text(tr("Choose a dedicated local folder such as Documents/CapitalTracker. Reopen that folder on another device using your own sync tool."))
                            Notice(tr("Public wallet providers receive your addresses, token contract addresses and IP. Brokers receive your access token and account id. Never enter private keys or seed phrases. Files in the chosen folder contain your financial records."))
                            Button(onClick={ copyFolder=false; model.expectReturn(); folderPicker.launch(null) }) { Text(tr("Choose or reopen folder")) }
                            Note(tr("Default currency starts as EUR. Change it and configure optional free provider keys in Settings."))
                        } else if(state.ready) {
                            if(data.stale() || allocation.incomplete || data.incomplete(data.settings.currency)) Notice(if(allocation.incomplete || data.incomplete(data.settings.currency)) tr("Incomplete valuation — some balances or rates are unavailable. Native quantities remain visible.") else tr("Cached / stale values — refresh when online. Allocations are estimates."))
                            when(section) {
                                "Overview" -> {
                                    val currency=data.settings.currency
                                    val values=data.buckets.filter { b -> data.holdings.any { it.bucketId==b.id } }.map { data.bucketValueOrNull(it.id,currency) }
                                    val total=if(values.isNotEmpty() && values.all { it==null }) null else values.filterNotNull().fold(ZERO,BigDecimal::add)
                                    Note(tr("TOTAL VALUED SAVINGS · {0}",currency))
                                    Text(money(total,currency),style=MaterialTheme.typography.displaySmall,fontWeight=FontWeight.SemiBold)
                                    Stat(tr("Allocated to goals"),money(data.convert(allocation.total,"USD",currency),currency))
                                    Stat(tr("Unallocated"),money(data.convert(allocation.capacities.values.fold(ZERO,BigDecimal::add)-allocation.total,"USD",currency),currency))
                                    if(data.buckets.isEmpty()) { Text(tr("Add your first bucket to start tracking savings.")); Button(onClick={ editor=Editor("Bucket") },enabled=editable) { Text(tr("Add bucket")) } }
                                    Heading(tr("Your goals"))
                                    if(data.goals.none { !it.archived }) { Text(tr("Give your savings a purpose.")); OutlinedButton(onClick={ editor=Editor("Goal") },enabled=editable) { Text(tr("Add goal")) } }
                                    data.goals.filterNot { it.archived }.sortedBy { it.due }.forEach { GoalRow(it,data,allocation,projection) { section="Goals"; goalId=it.id } }
                                    Heading(tr("Buckets"))
                                    data.buckets.forEach { b -> Item(b.name,tr("{0} holdings · {1}",data.holdings.count { it.bucketId==b.id },b.currency),money(data.bucketValueOrNull(b.id,b.currency),b.currency)) { section="Buckets"; bucketId=b.id } }
                                    Note(tr("Goal allocations are part of savings, not additional money."))
                                }
                                "Plans" -> {
                                    Note(tr("Planned amounts are not part of your savings. They project when goals close."))
                                    if(data.planned.isEmpty()) Text(tr("Add the amounts you plan to save and their dates."))
                                    val today=LocalDate.now()
                                    val (past,active)=data.planned.sortedBy { it.date }.partition { it.archived(today) }
                                    // Archived: the date passed, so the money is either in a bucket already or the plan was dropped. Newest first.
                                    (active+past.reversed()).forEach { pl ->
                                        val old=pl.archived(today)
                                        if(old && pl==past.last()) Heading(tr("Archived"))
                                        val tone=if(old) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                        Column(Modifier.fillMaxWidth().padding(vertical=14.dp,horizontal=4.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                                            Text(pl.name,style=MaterialTheme.typography.titleMedium,color=tone)
                                            // Icons sit right of the amount; FlowRow drops them to the next line when they do not fit.
                                            FlowRow(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,itemVerticalAlignment=Alignment.CenterVertically) {
                                                Text(money(pl.amount.decimal(),pl.currency),style=MaterialTheme.typography.titleLarge,color=tone)
                                                Row {
                                                    IconButton(onClick={ editor=Editor("Planned",pl.id) },enabled=editable) { Icon(painterResource(R.drawable.ic_edit),contentDescription=tr("Edit {0}",pl.name)) }
                                                    IconButton(onClick={ confirmation=tr("Delete planned saving {0}?",pl.name) to { model.edit({ p -> p.copy(planned=p.planned.filterNot { it.id==pl.id }) }) } },enabled=editable) { Icon(painterResource(R.drawable.ic_delete),contentDescription=tr("Delete {0}",pl.name)) }
                                                }
                                            }
                                            Note("${date(pl.date)} · ${if(old) tr("Archived · date passed") else tr("Planned")}")
                                        }
                                        HorizontalDivider()
                                    }
                                }
                                "Buckets" -> {
                                    val bucket=data.buckets.find { it.id==bucketId }
                                    if(bucket==null) {
                                        if(data.buckets.isEmpty()) Text(tr("Buckets group places where your money lives."))
                                        data.buckets.forEach { b -> Item(b.name,tr("{0} holdings",data.holdings.count { it.bucketId==b.id }),money(data.bucketValueOrNull(b.id,b.currency),b.currency)) { bucketId=b.id } }
                                    } else {
                                        TextButton(onClick={ bucketId=null }) { Text(tr("← All buckets")) }
                                        Heading(bucket.name)
                                        Text(money(data.bucketValueOrNull(bucket.id,bucket.currency),bucket.currency),style=MaterialTheme.typography.headlineLarge)
                                        Stat(tr("Allocated"),money(data.convert(allocation.bucket(bucket.id),"USD",bucket.currency),bucket.currency))
                                        Stat(tr("Available"),money(data.convert((allocation.capacities[bucket.id] ?: ZERO)-allocation.bucket(bucket.id),"USD",bucket.currency),bucket.currency))
                                        Actions {
                                            OutlinedButton(onClick={ editor=Editor("Bucket",bucket.id) },enabled=editable) { Text(tr("Edit bucket")) }
                                            TextButton(onClick={ confirmation=tr("Delete {0}, its {1} holdings and {2} goal connections? Previous snapshots remain recoverable.",bucket.name,data.holdings.count { it.bucketId==bucket.id },data.connections.count { it.bucketId==bucket.id }) to { model.edit({ it.deleteBucket(bucket.id) }) { bucketId=null } } },enabled=editable) { Text(tr("Delete bucket")) }
                                        }
                                        if(bucket.portfolio) {
                                            val base=data.settings.currency; val w=data.weights(bucket.id)
                                            Heading(tr("Portfolio"))
                                            Note(tr("Shares are calculated in {0}.",base))
                                            if(w.missing.isNotEmpty() || w.flagged.isNotEmpty()) Notice(tr("Portfolio shares unavailable: no value for {0}. Refresh, or remove the holding or target that has no value.",(w.missing+w.flagged).joinToString { assetName(data,it) }))
                                            else w.rows.forEach { r ->
                                                val diff=r.real-r.target
                                                val drift=if(diff.signum()==0) tr("on target") else (if(diff.signum()>0) "+" else "-")+diff.abs().setScale(2,java.math.RoundingMode.HALF_UP).toPlainString()+"%"
                                                Item(assetName(data,r.asset),tr("Real {0}% · target {1}% · {2}",r.real.toPlainString(),r.target.setScale(2,java.math.RoundingMode.HALF_UP).toPlainString(),drift),money(r.value,base),null)
                                            }
                                            OutlinedButton(onClick={ calculator=bucket.id },modifier=Modifier.heightIn(min=48.dp).testTag("rebalance")) { Text(tr("Rebalance")) }
                                        }
                                        Heading(tr("Holdings"))
                                        Button(onClick={ editor=Editor("Holding",owner=bucket.id) },enabled=editable) { Text(tr("Add holding")) }
                                        data.holdings.filter { it.bucketId==bucket.id }.forEach { h ->
                                            val account=h.broker!=null; val wallet=h.address!=null && !account; val cur=bucket.currency
                                            val (known,unknown)=h.tokens.filter { it.contract !in h.excluded }.partition { data.known(h,it) }
                                            val native=h.quantity?.let { data.convert(it.decimal(),h.asset,cur) }
                                            val total=(listOfNotNull(native)+known.mapNotNull { data.convert(it.quantity()!!,tokenAsset(h.asset,it.contract),cur) }).takeIf { it.isNotEmpty() }?.fold(ZERO,BigDecimal::add)
                                            if(account) Item(h.label,tr("Read-only · {0} · {1}",h.broker.orEmpty(),h.address.orEmpty())+"\n"+(h.quantity?.let { money(it.decimal(),h.asset) } ?: tr("Balance unknown")),if(h.quantity==null) tr("Balance unknown") else money(native,cur),null)
                                            else if(wallet) Item(h.label,tr("Read-only · {0}…",h.address.orEmpty().take(12)),if(h.quantity==null) tr("Balance unknown") else money(total,cur),null)
                                            else Item(h.label,tr("Manual")+"\n"+(h.quantity?.let { money(it.decimal(),h.asset) } ?: tr("Balance unknown")),money(native,cur),null)
                                            Note(tr("{0} · observed {1} · fetched {2}",shown(h.source),time(h.observedAt),time(h.fetchedAt)))
                                            h.error?.let { Notice(it) }
                                            h.tokensError?.let { Notice(tr("Tokens: {0}",it)) }
                                            Actions { TextButton(onClick={ editor=Editor("Holding",h.id,bucket.id) },enabled=editable) { Text(tr("Edit / move")) }; TextButton(onClick={ confirmation=tr("Delete {0}?",h.label) to { model.edit({ p -> p.copy(holdings=p.holdings.filterNot { it.id==h.id }) }) } },enabled=editable) { Text(tr("Delete")) } }
                                            h.tokens.count { it.contract in h.excluded }.takeIf { it>0 }?.let { Note(if(it==1) tr("{0} token excluded in the holding editor",it) else tr("{0} tokens excluded in the holding editor",it)) }
                                            if(wallet) Item(assetLabel(h.asset),tr("Native · {0}",h.quantity?.let { money(it.decimal(),h.asset) } ?: tr("balance unknown")),h.quantity?.let { money(native,cur) },null)
                                            var all by remember(h.id) { mutableStateOf(false) }
                                            known.sortedByDescending { data.convert(it.quantity()!!,tokenAsset(h.asset,it.contract),cur) ?: ZERO }.forEach { t ->
                                                Item(t.symbol.ifBlank { shortContract(h.asset,t.contract) },tr("Token · {0} · {1}",tokenQty(t.quantity()!!,t.symbol),shortContract(h.asset,t.contract)),money(data.convert(t.quantity()!!,tokenAsset(h.asset,t.contract),cur),cur),null)
                                            }
                                            val sortedUnknown=unknown.sortedWith(compareBy<Token> { it.symbol }.thenBy { it.contract })
                                            (if(all) sortedUnknown else sortedUnknown.take(5)).forEach { t ->
                                                Item(tr("Unknown token"),tr("Not counted · reported as “{0}” · {1} · {2}",t.symbol.ifBlank { t.name.ifBlank { tr("unnamed") } },shortContract(h.asset,t.contract),t.quantity()?.let { tokenQty(it,"") } ?: tr("raw units {0}",t.units)),null,null)
                                            }
                                            if(sortedUnknown.size>5) TextButton(onClick={ all=!all }) { Text(if(all) tr("Show fewer") else tr("Show {0} more unknown tokens",sortedUnknown.size-5)) }
                                        }
                                        Heading(tr("Connected goals"))
                                        data.connections.filter { it.bucketId==bucket.id }.forEach { c -> val g=data.goals.first { it.id==c.goalId }; Item(g.name,limitLabel(c.mode),money(data.convert(allocation.byConnection[c.key] ?: ZERO,"USD",g.currency),g.currency)) { section="Goals"; goalId=g.id } }
                                        Note(tr("Automatic tracking covers native balances and tokens on ETH, TON and TRX addresses, and the total value of connected broker accounts. Tokens count only when the price provider lists their contract. Add staking or other investments as manual holdings."))
                                    }
                                }
                                "Goals" -> {
                                    val goal=data.goals.find { it.id==goalId }
                                    if(goal==null) {
                                        if(data.goals.isEmpty()) Text(tr("Create a goal with a target, currency and due date."))
                                        val today=LocalDate.now()
                                        data.goals.filterNot { it.archived }.sortedWith(compareBy<Goal> { it.due }.thenByDescending { it.priority }).groupBy { it.due }.forEach { (due,group) ->
                                            Heading(date(due)+if(LocalDate.parse(due)<today) " · "+tr("overdue") else "")
                                            GoalGroup(group,data,allocation,projection,editable,{ goalId=it },::move)
                                        }
                                        if(data.goals.any { it.archived }) Heading(tr("Archived"))
                                        data.goals.filter { it.archived }.forEach { GoalRow(it,data,allocation,projection) { goalId=it.id } }
                                    } else {
                                        TextButton(onClick={ goalId=null }) { Text(tr("← All goals")) }
                                        GoalRow(goal,data,allocation,projection) { if(editable) editor=Editor("Goal",goal.id) }
                                        val funded=data.convert(allocation.goal(goal.id),"USD",goal.currency)
                                        Stat(tr("Still needed"),money(funded?.let { (goal.target.decimal()-it).max(ZERO) },goal.currency))
                                        Actions {
                                            OutlinedButton(onClick={ editor=Editor("Goal",goal.id) },enabled=editable) { Text(tr("Edit goal")) }
                                            TextButton(onClick={ model.edit({ p -> p.copy(goals=p.goals.map { if(it.id==goal.id) it.copy(archived=!it.archived) else it }) }) },enabled=editable) { Text(if(goal.archived) tr("Activate") else tr("Archive")) }
                                            TextButton(onClick={ confirmation=tr("Delete {0} and its connections?",goal.name) to { model.edit({ it.deleteGoal(goal.id) }) { goalId=null } } },enabled=editable) { Text(tr("Delete")) }
                                        }
                                        Heading(tr("Funding sources"))
                                        Button(onClick={ editor=Editor("Connection",owner=goal.id) },enabled=editable && data.buckets.isNotEmpty()) { Text(tr("Connect bucket")) }
                                        if(data.buckets.isEmpty()) Note(tr("Create a bucket first."))
                                        data.connections.filter { it.goalId==goal.id }.forEach { c ->
                                            val b=data.buckets.first { it.id==c.bucketId }
                                            val amount=allocation.byConnection[c.key] ?: ZERO
                                            Item(b.name,"${limitLabel(c.mode)} ${if(c.mode!=Limit.AUTO) c.value else ""}${c.goalCap?.let { " · "+tr("Max {0}% of goal",it) } ?: ""}",money(data.convert(amount,"USD",goal.currency),goal.currency)) { if(editable) editor=Editor("Connection",c.key,goal.id) }
                                            val free=(allocation.capacities[b.id] ?: ZERO)-allocation.bucket(b.id)
                                            Note(when { goal.archived -> tr("Archived goals reserve no funds."); allocation.incomplete -> tr("Missing rates or balances can limit funding."); free>ZERO && funded!=null && funded<goal.target.decimal() -> tr("Available savings remain. Increase this connection's limit to allocate more."); amount==ZERO && funded!=null && funded>=goal.target.decimal() -> tr("Goal target is covered by other buckets."); amount==ZERO -> tr("No capacity left after higher-ranked goals or connection limits."); else -> tr("Contribution respects goal order and connection limits.") })
                                            TextButton(onClick={ confirmation=tr("Disconnect {0} from {1}?",b.name,goal.name) to { model.edit({ p -> p.copy(connections=p.connections.filterNot { it.key==c.key }) }) } },enabled=editable) { Text(tr("Disconnect")) }
                                        }
                                        Note(tr("Earlier dates are funded first; within a date, the order shown. Allocation never moves money."))
                                        Heading(tr("Planned savings"))
                                        val contributions=projection.contributions[goal.id].orEmpty()
                                        if(contributions.isEmpty()) Note(tr("No planned savings reach this goal."))
                                        contributions.forEach { c -> data.planned.find { it.id==c.plannedId }?.let { pl -> Item(pl.name,date(pl.date),"+${money(data.convert(c.usd,"USD",goal.currency),goal.currency)}",null) } }
                                    }
                                }
                                "Tips" -> TipsScreen(model::notice)
                                "Settings" -> {
                                    Heading(tr("Preferences"))
                                    OutlinedButton(onClick={ editor=Editor("Settings") },enabled=editable) { Text(tr("Currency: {0} · Theme: {1}",data.settings.currency,shown(data.settings.theme))) }
                                    Choice(tr("Language"),language,listOf(I18n.SYSTEM)+I18n.languages.keys,show={ if(it==I18n.SYSTEM) tr("System default") else I18n.languages.getValue(it) }) { dev.capital.Language.choose(context,it); language=it }
                                    Note(tr("Default currency values the overview and new entities. Existing currencies stay unchanged."))
                                    Heading(tr("Free data providers"))
                                    providerChoices.forEach { (category,options) ->
                                        Choice(shown(category),data.settings.providers.getValue(category),options,editable,::shown) { provider -> model.edit({ p -> p.copy(settings=p.settings.copy(providers=p.settings.providers+(category to provider))) }) }
                                        val provider=data.settings.providers.getValue(category)
                                        if(!category.endsWith(" tokens") && provider in listOf("Alchemy","TronGrid","CoinGecko","TON Center")) TextButton(onClick={ keyProvider=provider }) { Text(if(provider=="TON Center") tr("Optional key: {0}",provider) else tr("Required free key: {0}",provider)) }
                                    }
                                    OutlinedButton(onClick={ model.refresh() },enabled=editable && !state.refreshing) { Text(tr("Test sources / refresh portfolio")) }
                                    Note(tr("Tests query only assets and addresses in your portfolio. No silent provider fallback. Token sources and price providers also receive wallet or token contract addresses; choose Off to stop token lookups for a chain."))
                                    Heading(tr("Broker accounts"))
                                    brokerCredentials.values.flatten().forEach { name -> TextButton(onClick={ keyProvider=name }) { Text(credentialLabel(name)) } }
                                    TextButton(onClick={ runCatching { context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(I18n.siteUrl("accounts")))) }.onFailure { model.notice(tr("No browser available")) } }) { Text(tr("Setup guide for broker accounts")) }
                                    Note(tr("Tokens are stored encrypted on this device and sent only to the broker that issued them. Capital reads account values and never places orders."))
                                    Heading(tr("Quotes and freshness"))
                                    if(data.quotes.isEmpty()) Note(tr("No cached quotes. Add holdings and refresh."))
                                    data.quotes.count { ':' in it.asset }.takeIf { it>0 }?.let { Note(tr("{0} token prices by contract address are shown with their wallets.",it)) }
                                    data.quotes.filter { ':' !in it.asset }.forEach { q -> Text("${q.asset}: ${money(q.usd.decimal(),"USD")}"); Note(tr("{0} · observed {1} · fetched {2}{3}",shown(q.source),time(q.observedAt),time(q.fetchedAt),q.error?.let { "\n$it" } ?: "")) }
                                    SecuritySection(model,state,lockRev,{ lockRev++ }) { secDialog=it }
                                    Heading(tr("Storage"))
                                    Text(state.folder ?: tr("No folder"))
                                    Actions {
                                        OutlinedButton(onClick={ copyFolder=false; model.expectReturn(); folderPicker.launch(null) }) { Text(tr("Reconnect / open folder")) }
                                        OutlinedButton(onClick={ model.reload() }) { Text(tr("Reload local files")) }
                                        OutlinedButton(onClick={ model.expectReturn(); backup.launch("capital-backup-${LocalDate.now()}.json") }) { Text(tr("Export backup")) }
                                        if(state.encrypted) OutlinedButton(onClick={ secDialog="plain" },modifier=Modifier.testTag("export-plaintext")) { Text(tr("Export plaintext backup")) }
                                        OutlinedButton(onClick={ model.expectReturn(); restore.launch(arrayOf("application/json","text/plain","application/octet-stream")) },enabled=editable) { Text(tr("Restore backup")) }
                                    }
                                    Note(tr("Your sync tool manages this folder. Snapshots are retained for recovery; API keys never leave device storage. Refresh happens only on cold startup or request."))
                                    Heading(tr("Sources / attribution"))
                                    listOf("Blockstream" to "https://blockstream.info", "mempool.space" to "https://mempool.space", "PublicNode" to "https://publicnode.com", "Alchemy" to "https://alchemy.com", "TON Center" to "https://toncenter.com", "TonAPI" to "https://tonapi.io", "TronGrid" to "https://trongrid.io", "Blockscout" to "https://eth.blockscout.com", "Ethplorer" to "https://ethplorer.io", "DefiLlama" to "https://defillama.com", "Powered by CoinGecko" to "https://coingecko.com", "Powered by CoinPaprika" to "https://coinpaprika.com", "Frankfurter" to "https://frankfurter.dev", "European Central Bank" to "https://ecb.europa.eu", "Interactive Brokers" to "https://www.interactivebrokers.com", "OANDA" to "https://www.oanda.com", "Trading 212" to "https://www.trading212.com", "SnapTrade" to "https://snaptrade.com").forEach { (name,url) -> TextButton(onClick={ runCatching { context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url))) }.onFailure { model.notice(tr("No browser available")) } }) { Text(shown(name)) } }
                                    Heading(tr("Legal"))
                                    listOf(tr("Privacy Policy") to "privacy", tr("Data safety") to "data-safety", tr("Financial features") to "financial-features").forEach { (name,path) -> TextButton(onClick={ runCatching { context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(I18n.siteUrl(path)))) }.onFailure { model.notice(tr("No browser available")) } }) { Text(name) } }
                                    // Read from the installed package, so it always matches the build that is running.
                                    val pkg=remember { runCatching { context.packageManager.getPackageInfo(context.packageName,0) }.getOrNull() }
                                    Note(tr("Capital {0} · build {1}",pkg?.versionName ?: tr("unknown version"),pkg?.let { androidx.core.content.pm.PackageInfoCompat.getLongVersionCode(it) } ?: tr("unknown")),Modifier.testTag("version"))
                                }
                            }
                        }
                        // Room for the pinned add button and the message pop-up.
                        Spacer(Modifier.height(96.dp))
                    }
                }
            }
        }
        editor?.let { e -> EditSheet(e,data,state.saving,if(state.unsaved && !state.saving) state.message ?: tr("Save failed") else null,onDismiss={ editor=null },onSave={ transform -> model.edit(transform) { editor=null } },onFetchTokens=model::fetchTokens,onFetchAccounts=model::fetchAccounts,
            onConnectSnapTrade={ model.connectSnapTrade { url -> runCatching { context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url))) }.onFailure { model.notice(tr("No browser available")) } } }) }
        calculator?.let { id -> data.buckets.find { it.id==id }?.let { RebalanceDialog(it,data) { calculator=null } } }
        confirmation?.let { (text,action) -> AlertDialog(onDismissRequest={ confirmation=null },title={ Text(tr("Confirm change")) },text={ Text(text) },confirmButton={ TextButton(onClick={ confirmation=null; action() }) { Text(tr("Confirm")) } },dismissButton={ TextButton(onClick={ confirmation=null }) { Text(tr("Cancel")) } }) }
        keyProvider?.let { provider -> KeyDialog(provider,onDismiss={ keyProvider=null },onSave={ if(model.saveKey(provider,it)) keyProvider=null }) }
        SecurityDialogs(model,state,secDialog,{ lockRev++ },{ secDialog=null }) { secDialog=it }
        state.restore?.let { revision -> AlertDialog(onDismissRequest=model::cancelRestore,title={ Text(tr("Restore backup?")) },text={ Column { RevisionSummary(revision); Text(tr("Replaces current records with this backup. Existing snapshots remain available.")) } },confirmButton={ TextButton(onClick=model::restore) { Text(tr("Restore")) } },dismissButton={ TextButton(onClick=model::cancelRestore) { Text(tr("Cancel")) } }) }
    }
}
@Composable private fun RevisionSummary(revision: Revision) {
    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Text(tr("Revision {0} · {1}",revision.id.take(8),time(revision.createdAt)),fontWeight=FontWeight.SemiBold)
        Text(tr("{0} buckets · {1} holdings · {2} goals",revision.data.buckets.size,revision.data.holdings.size,revision.data.goals.size))
        revision.data.buckets.forEach { b -> Text("${b.name}: ${money(revision.data.bucketValueOrNull(b.id,b.currency),b.currency)}") }
        revision.data.holdings.forEach { h -> Note("${h.label}: ${h.quantity ?: tr("unknown")} ${h.asset} · ${h.address ?: tr("manual")}") }
        revision.data.goals.forEach { g -> Note("${g.name}: ${money(g.target.decimal(),g.currency)} · ${date(g.due)}") }
    }
}
@Composable internal fun Choice(label: String,value: String,options: List<String>,enabled: Boolean=true,show: (String)->String={ it },onChange: (String)->Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column { Note(label); Box { OutlinedButton(onClick={ expanded=true },enabled=enabled,modifier=Modifier.fillMaxWidth()) { Text(show(value)) }; DropdownMenu(expanded=expanded,onDismissRequest={ expanded=false }) { options.forEach { option -> DropdownMenuItem(text={ Text(show(option)) },onClick={ expanded=false; onChange(option) }) } } } }
}
@Composable private fun Pick(label: String,selected: String?,options: List<Pair<String,String>>,enabled: Boolean=true,placeholder: String=tr("Choose bucket"),onChange: (String)->Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column { Note(label); Box { OutlinedButton(onClick={ expanded=true },enabled=enabled,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)) { Text(options.find { it.first==selected }?.second ?: placeholder) }; DropdownMenu(expanded=expanded,onDismissRequest={ expanded=false }) { options.forEach { (key,text) -> DropdownMenuItem(text={ Text(text) },onClick={ expanded=false; onChange(key) }) } } } }
}
private fun limitLabel(mode: Limit)=when(mode) { Limit.AUTO -> tr("Auto — up to remaining need"); Limit.FIXED -> tr("Fixed amount in goal currency"); Limit.BUCKET_PERCENT -> tr("% of bucket"); Limit.GOAL_PERCENT -> tr("% of goal") }
@Composable private fun KeyDialog(provider: String,onDismiss: ()->Unit,onSave: (String)->Unit) {
    var value by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest=onDismiss,title={ Text(if(provider in brokerCredentials.values.flatten()) credentialLabel(provider) else tr("{0} key",provider)) },text={ Column { Text(tr("Stored encrypted on this device. Leave blank to remove an existing key.")); OutlinedTextField(value,onValueChange={ value=it },label={ Text(tr("API key")) },visualTransformation=androidx.compose.ui.text.input.PasswordVisualTransformation(),singleLine=true) } },confirmButton={ TextButton(onClick={ onSave(value) }) { Text(tr("Save")) } },dismissButton={ TextButton(onClick=onDismiss) { Text(tr("Cancel")) } })
}

// Read-only calculator: never calls model.edit.
@Composable private fun RebalanceDialog(bucket: Bucket,data: Portfolio,onDismiss: ()->Unit) {
    var amount by remember { mutableStateOf("") }
    val base=data.settings.currency
    val parsed=remember(amount) { runCatching { amount.trim().replace(',','.').ifEmpty { "0" }.decimal() } }
    val plan=remember(parsed,data) { parsed.getOrNull()?.let { data.rebalance(bucket.id,it) } }
    Dialog(onDismissRequest=onDismiss,properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Surface(Modifier.fillMaxSize().semantics { testTagsAsResourceId=true },color=MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Heading(tr("Rebalance {0}",bucket.name))
                OutlinedTextField(amount,onValueChange={ amount=it },label={ Text(tr("Amount to invest in {0} · no grouping separators",base)) },modifier=Modifier.fillMaxWidth().testTag("invest-amount"),singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal))
                Text(if(bucket.allowSells) tr("Sells allowed") else tr("Buys only. Allow sells in the bucket settings."))
                parsed.exceptionOrNull()?.let { Text(it.message.orEmpty(),color=MaterialTheme.colorScheme.error) }
                if(plan?.unavailable!=null) {
                    val w=data.weights(bucket.id)
                    Notice(if(w.missing.isNotEmpty() || w.flagged.isNotEmpty()) tr("Rebalance unavailable: no value for {0}. Refresh, or remove the holding or target that has no value.",(w.missing+w.flagged).joinToString { assetName(data,it) }) else tr("Rebalance unavailable: {0}",plan.unavailable))
                } else plan?.trades?.forEach { t ->
                    val action=when { t.amount.signum()>0 -> tr("Buy {0}",money(t.amount,base)); t.amount.signum()<0 -> tr("Sell {0}",money(t.amount.abs(),base)); else -> tr("No trade") }
                    val qty=t.quantity?.let { "${NumberFormat.getNumberInstance(I18n.locale).apply { maximumFractionDigits=assetDigits(t.asset) }.format(it.abs())} ${assetName(data,t.asset)}" } ?: tr("quantity unavailable")
                    Item(assetName(data,t.asset),tr("{0} · result {1}% · target {2}%",qty,t.resultPercent.toPlainString(),t.target.setScale(2,java.math.RoundingMode.HALF_UP).toPlainString()),action,null)
                }
                Note(tr("Estimate from cached rates. Capital does not trade; nothing is changed."))
                if(data.holdings.any { it.bucketId==bucket.id && it.address!=null }) Note(tr("Wallet balances are read-only. Trade in your wallet or exchange."))
                Button(onClick=onDismiss,modifier=Modifier.heightIn(min=48.dp)) { Text(tr("Close")) }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun EditSheet(editor: Editor,data: Portfolio,saving: Boolean,saveError: String?,onDismiss: ()->Unit,onSave: ((Portfolio)->Portfolio)->Unit,onFetchTokens: (String,String,(List<Token>?,String?)->Unit)->Unit,onFetchAccounts: (String,(List<Pair<String,String>>?,String?)->Unit)->Unit,onConnectSnapTrade: ()->Unit) {
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
        "address" to (h?.address ?: ""), "type" to (if(h?.broker!=null) "Account" else if(h?.address!=null) "Wallet" else "Manual"), "broker" to (h?.broker ?: brokerChoices.first()),
        "mode" to (c?.mode?.name ?: Limit.AUTO.name), "value" to (c?.value ?: "0"), "cap" to (c?.goalCap ?: ""),
        "theme" to data.settings.theme, "currencyAction" to "Choose treatment",
    ) }
    val fields=remember { mutableStateMapOf<String,String>().apply { putAll(initial) } }
    var error by remember { mutableStateOf<String?>(null) }
    var discard by remember { mutableStateOf(false) }
    var tokens by remember { mutableStateOf(h?.tokens ?: emptyList()) }
    val disabled=remember { mutableStateListOf<String>().apply { addAll(h?.excluded ?: emptyList()) } }
    var portfolio by remember { mutableStateOf(b?.portfolio ?: false) }
    var sells by remember { mutableStateOf(b?.allowSells ?: false) }
    val targets=remember { mutableStateListOf<Pair<String,String>>().apply { addAll(b?.targets?.toList().orEmpty()) } }
    var otherAsset by remember { mutableStateOf("") }
    var fetching by remember { mutableStateOf(false) }
    var tokenMessage by remember { mutableStateOf<String?>(null) }
    var fetched by remember { mutableStateOf(false) }
    var accounts by remember { mutableStateOf<List<Pair<String,String>>?>(null) }
    var accountMessage by remember { mutableStateOf<String?>(null) }
    // Tokens belong to one chain + address: a change drops them (not on first composition).
    // ponytail: changing the address and changing it back also drops stored exclusions; re-fetch restores the list.
    val tokenKey=fields["currency"].orEmpty().trim().uppercase() to fields["address"].orEmpty().trim()
    var tokenFor by remember { mutableStateOf(tokenKey) }
    LaunchedEffect(tokenKey) { if(tokenKey!=tokenFor) { tokenFor=tokenKey; tokens=emptyList(); disabled.clear(); fetched=false; tokenMessage=null } }
    val context=LocalContext.current
    fun close() { if(fields.toMap()!=initial || portfolio!=(b?.portfolio ?: false) || sells!=(b?.allowSells ?: false) || targets.toList()!=b?.targets?.toList().orEmpty()) discard=true else onDismiss() }
    fun number(key: String)=fields.getValue(key).trim().replace(',','.').also { it.decimal() }
    fun transform(p: Portfolio): Portfolio {
        val currency=fields.getValue("currency").trim().uppercase()
        require(validAsset(currency)) { tr("Use an ISO currency code (EUR, USD…) or BTC, ETH, TON, TRX") }
        val name=fields.getValue("name").trim()
        val oldCurrency=when(editor.kind) { "Holding" -> h?.asset; "Goal" -> g?.currency; else -> null }
        val changed=oldCurrency!=null && oldCurrency!=currency && (editor.kind!="Holding" || fields.getValue("type")=="Manual")
        if(changed) require(fields.getValue("currencyAction")!="Choose treatment") { tr("Choose how to treat the changed currency") }
        val convert=changed && fields.getValue("currencyAction")=="Convert existing values"
        fun converted(amount: String)=p.convert(amount.decimal(),requireNotNull(oldCurrency),currency)?.text() ?: error(tr("Refresh exchange rates before converting"))
        return when(editor.kind) {
            "Bucket" -> {
                val item=Bucket(b?.id ?: newId,name,currency,portfolio,targets.associate { (k,v) -> k to v.trim().replace(',','.') },sells)
                p.copy(buckets=if(b==null) p.buckets+item else p.buckets.map { if(it.id==b.id) item else it })
            }
            "Holding" -> {
                val type=fields.getValue("type"); val wallet=type=="Wallet"; val account=type=="Account"; val remote=wallet || account
                val broker=if(account) fields.getValue("broker") else null
                val address=when { wallet -> canonicalAddress(Chain.entries.find { it.name==currency } ?: error(tr("Choose BTC, ETH, TON or TRX for a wallet")),fields.getValue("address")); account -> accountId(broker,fields.getValue("address")); else -> null }
                if(wallet) p.holdings.firstOrNull { it.id!=h?.id && it.address!=null && it.broker==null && it.asset==currency && canonicalAddress(Chain.valueOf(currency),it.address)==address }?.let { duplicate -> error(tr("Wallet already belongs to {0}. Edit or move it there.",p.buckets.first { it.id==duplicate.bucketId }.name)) }
                if(account) p.holdings.firstOrNull { it.id!=h?.id && it.broker==broker && it.address==address }?.let { duplicate -> error(tr("Account already belongs to {0}. Edit or move it there.",p.buckets.first { it.id==duplicate.bucketId }.name)) }
                val current=p.holdings.find { it.id==h?.id }
                require(h==null || current!=null) { tr("This holding was removed; reopen the editor") }
                val unchanged=(wallet && current?.address!=null && current.broker==null && current.asset==currency && canonicalAddress(Chain.valueOf(currency),current.address)==address) || (account && current!=null && current.broker==broker && current.address==address)
                // An account's currency comes from the broker on refresh; until then any fiat code is a placeholder.
                val asset=if(!account) currency else if(unchanged) current!!.asset else currency.takeIf { ':' !in it && it !in Chain.entries.map { c -> c.name } } ?: "USD"
                val item=Holding(
                    id=h?.id ?: newId,bucketId=fields.getValue("bucket"),label=name,asset=asset,address=address,
                    quantity=if(remote) if(unchanged) current?.quantity else null else if(convert) converted(requireNotNull(h?.quantity)) else number("quantity"),
                    observedAt=if(unchanged) current?.observedAt else if(!remote) System.currentTimeMillis() else null,
                    fetchedAt=if(unchanged) current?.fetchedAt else null,source=if(unchanged) current?.source ?: "Unknown" else if(remote) "Not refreshed" else "Manual",
                    error=if(unchanged) current?.error else null,
                    tokens=if(!wallet || currency==Chain.BTC.name) emptyList() else if(fetched) tokens else if(unchanged) current?.tokens.orEmpty() else emptyList(),
                    tokensError=if(wallet && !fetched && unchanged) current?.tokensError else null,
                    excluded=if(wallet && currency!=Chain.BTC.name) disabled.distinct() else emptyList(),
                    broker=broker,
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
        OutlinedButton(onClick={ val d=runCatching { LocalDate.parse(fields.getValue(key)) }.getOrDefault(LocalDate.now()); DatePickerDialog(context,{ _,y,m,day -> fields[key]=LocalDate.of(y,m+1,day).toString() },d.year,d.monthValue-1,d.dayOfMonth).show() }) { Text(tr("Choose date")) }
    }
    @Composable fun TokenEditor() {
        val chain=Chain.entries.find { it.name==fields.getValue("currency").trim().uppercase() }?.takeIf { it!=Chain.BTC } ?: return
        Heading(tr("Tokens"))
        Note(tr("Fetch the tokens held by this address and switch off the ones you do not want listed or counted."))
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
        },enabled=!saving && !fetching && fields.getValue("address").isNotBlank(),modifier=Modifier.testTag("fetch-tokens").heightIn(min=48.dp)) { Text(tr("Fetch tokens")) }
        if(fetching) Note(tr("Fetching tokens…"))
        tokenMessage?.let { Notice(it) }
        if(fetched && tokens.isEmpty()) Note(tr("No tokens found on this address."))
        tokens.forEach { t ->
            val short=shortContract(chain.name,t.contract)
            val priced=data.price(tokenAsset(chain.name,t.contract))!=null && t.quantity()!=null
            val title=if(priced) t.symbol.ifBlank { short } else tr("Unknown token")
            Row(Modifier.fillMaxWidth().heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(title,style=MaterialTheme.typography.titleMedium)
                    Note("${t.quantity()?.let { tokenQty(it,t.symbol) } ?: tr("raw units {0}",t.units)} · $short"+if(priced) "" else " · "+tr("reported as “{0}”",t.symbol.ifBlank { t.name.ifBlank { tr("unnamed") } }))
                }
                Switch(checked=t.contract !in disabled,onCheckedChange={ on -> if(on) disabled.remove(t.contract) else if(t.contract !in disabled) disabled.add(t.contract) },enabled=!saving,modifier=Modifier.testTag("token-${t.contract.take(10)}").semantics { contentDescription=tr("Include {0} {1}",title,short) })
            }
        }
    }
    Dialog(onDismissRequest={ close() },properties=DialogProperties(usePlatformDefaultWidth=false,dismissOnClickOutside=false)) {
        Surface(Modifier.fillMaxSize().semantics { testTagsAsResourceId=true },color=MaterialTheme.colorScheme.background) {
            Scaffold(topBar={ TopAppBar(title={ Text(editorTitle(editor.kind,editor.id.isBlank())) },navigationIcon={ TextButton(onClick={ close() },enabled=!saving) { Text(tr("Cancel")) } },actions={ TextButton(onClick={
                val checked=runCatching { transform(data) }
                if(checked.isFailure) error=checked.exceptionOrNull()?.message else onSave(::transform)
            },enabled=!saving) { Text(if(saving) tr("Saving…") else tr("Save")) } }) }) { padding ->
                Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                    error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
                    if(saveError!=null) {
                        Notice(tr("{0}\nYour changes remain in memory.",saveError))
                        Button(onClick=onDismiss) { Text(tr("Open save recovery")) }
                    }
                    if(editor.kind in listOf("Bucket","Holding","Goal","Planned")) field("name",if(editor.kind=="Goal") tr("Purpose") else tr("Name"))
                    if(editor.kind=="Holding") Choice(tr("Tracking"),fields.getValue("type"),listOf("Manual","Wallet","Account"),!saving,::shown) { fields["type"]=it; if(it=="Wallet" && fields.getValue("currency") !in Chain.entries.map { chain -> chain.name }) fields["currency"]="BTC" }
                    if(editor.kind!="Connection") {
                        if(editor.kind=="Holding" && fields.getValue("type")=="Wallet") Choice(tr("Mainnet chain"),fields.getValue("currency"),Chain.entries.map { it.name },!saving) { fields["currency"]=it }
                        else if(editor.kind=="Holding" && fields.getValue("type")=="Account") Choice(tr("Broker"),fields.getValue("broker"),brokerChoices,!saving) { if(it!=fields["broker"]) { fields["broker"]=it; fields["address"]=""; accounts=null; accountMessage=null } }
                        else { field("currency",tr("Currency code (EUR, USD, BTC…)")); Note(tr("Use an ISO currency code or a supported native asset. Conversion needs a quote from your selected provider.")) }
                    }
                    if(editor.kind in listOf("Holding","Connection")) {
                        val options=data.buckets.map { x ->
                            val text=if(data.buckets.count { it.name==x.name }>1) "${x.name} · ${x.currency}" else x.name
                            val twins=data.buckets.filter { it.name==x.name && it.currency==x.currency }
                            x.id to if(twins.size>1) "$text (${twins.indexOf(x)+1})" else text
                        }
                        Pick(tr("Bucket"),fields["bucket"],options,!saving) { fields["bucket"]=it }
                    }
                    when(editor.kind) {
                        "Bucket" -> {
                            Row(Modifier.fillMaxWidth().heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                Text(tr("Portfolio mode"),Modifier.weight(1f))
                                Switch(checked=portfolio,onCheckedChange={ portfolio=it; error=null },enabled=!saving,modifier=Modifier.testTag("portfolio-mode").semantics { contentDescription=tr("Portfolio mode") })
                            }
                            if(portfolio) {
                                Row(Modifier.fillMaxWidth().heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                    Text(tr("Allow sells during rebalance"),Modifier.weight(1f))
                                    Switch(checked=sells,onCheckedChange={ sells=it },enabled=!saving,modifier=Modifier.testTag("allow-sells").semantics { contentDescription=tr("Allow sells during rebalance") })
                                }
                                Heading(tr("Target weights"))
                                val held=data.holdings.filter { it.bucketId==b?.id }.flatMap { x -> listOf(x.asset)+x.tokens.filter { data.known(x,it) }.map { tokenAsset(x.asset,it.contract) } }.distinct().filter { a -> targets.none { it.first==a } }
                                if(targets.isEmpty() && held.isNotEmpty()) Note(tr("Add a target for each asset. Targets must total 100%."))
                                targets.forEachIndexed { i,(asset,value) ->
                                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                                        Text(assetName(data,asset),Modifier.weight(1f))
                                        OutlinedTextField(value,onValueChange={ targets[i]=asset to it; error=null },label={ Text(tr("Target %")) },modifier=Modifier.width(120.dp).testTag("target-$i"),singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),enabled=!saving)
                                        TextButton(onClick={ targets.removeAt(i); error=null },enabled=!saving,modifier=Modifier.heightIn(min=48.dp)) { Text(tr("Remove")) }
                                    }
                                }
                                val sum=targets.fold(ZERO) { s,(_,v) -> s+(v.trim().replace(',','.').toBigDecimalOrNull() ?: ZERO) }
                                Text(when { sum.compareTo(HUNDRED)==0 -> tr("Total 100%"); sum<HUNDRED -> tr("Total {0}% · {1}% missing",sum.text(),(HUNDRED-sum).text()); else -> tr("Total {0}% · {1}% too much",sum.text(),(sum-HUNDRED).text()) },fontWeight=FontWeight.SemiBold)
                                if(held.isNotEmpty()) Pick(tr("Add target"),null,held.map { it to assetName(data,it) },!saving,tr("Choose asset")) { targets.add(it to ""); error=null }
                                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(otherAsset,onValueChange={ otherAsset=it; error=null },label={ Text(tr("Other asset code (EUR, USD, BTC…)")) },modifier=Modifier.weight(1f).testTag("other-asset"),singleLine=true,enabled=!saving)
                                    TextButton(onClick={
                                        val code=otherAsset.trim().uppercase()
                                        if(!validAsset(code) || ':' in code) error=tr("Use an ISO currency code or BTC, ETH, TON, TRX. Tokens can be added only when held.")
                                        else if(targets.any { it.first==code }) error=tr("{0} already has a target",code)
                                        else { targets.add(code to ""); otherAsset="" }
                                    },enabled=!saving,modifier=Modifier.heightIn(min=48.dp)) { Text(tr("Add")) }
                                }
                            }
                        }
                        "Holding" -> when(fields.getValue("type")) {
                            "Wallet" -> { field("address",tr("Public wallet address")); Note(tr("Paste one address. No seed phrase, private key or HD wallet discovery.")); TokenEditor() }
                            "Account" -> {
                                when(val broker=fields.getValue("broker")) {
                                    "SnapTrade" -> {
                                        Actions {
                                            OutlinedButton(onClick={ accountMessage=null; onFetchAccounts(broker) { list,message -> if(list!=null) { accounts=list; if(list.isEmpty()) accountMessage=tr("No accounts connected yet. Connect a brokerage through SnapTrade first.") } else accountMessage=message } },enabled=!saving,modifier=Modifier.heightIn(min=48.dp).testTag("fetch-accounts")) { Text(tr("Fetch accounts")) }
                                            OutlinedButton(onClick=onConnectSnapTrade,enabled=!saving,modifier=Modifier.heightIn(min=48.dp)) { Text(tr("Connect a brokerage through SnapTrade")) }
                                        }
                                        accountMessage?.let { Notice(it) }
                                        accounts?.takeIf { it.isNotEmpty() }?.let { list -> Pick(tr("SnapTrade account"),fields["address"],list,!saving,tr("Choose account")) { fields["address"]=it; error=null } }
                                        field("address",tr("SnapTrade account id"))
                                    }
                                    "Trading 212" -> field("address",tr("Trading 212 account number"))
                                    "OANDA" -> field("address",tr("OANDA account id"))
                                    else -> field("address",tr("Flex Query id"))
                                }
                                Note(tr("The currency and value come from the broker on refresh. Enter the access token in Settings → Broker accounts."))
                                TextButton(onClick={ runCatching { context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(I18n.siteUrl("accounts")))) }.onFailure { error=tr("No browser available") } },modifier=Modifier.heightIn(min=48.dp)) { Text(tr("Setup guide for broker accounts")) }
                            }
                            else -> field("quantity",tr("Current quantity · no grouping separators"),true)
                        }
                        "Goal" -> {
                            field("target",tr("Target amount · no grouping separators"),true)
                            dateField("due",tr("Due date · YYYY-MM-DD"))
                        }
                        "Planned" -> { field("amount",tr("Amount · no grouping separators"),true); dateField("date",tr("Planned date · YYYY-MM-DD")) }
                        "Connection" -> {
                            val mode=Limit.valueOf(fields.getValue("mode"))
                            Choice(tr("Contribution limit"),limitLabel(mode),Limit.entries.map(::limitLabel),!saving) { chosen -> fields["mode"]=Limit.entries.first { limitLabel(it)==chosen }.name }
                            if(mode!=Limit.AUTO) field("value",if(mode==Limit.FIXED) tr("Amount in {0}",data.goals.first { it.id==editor.owner }.currency) else limitLabel(mode),true)
                            field("cap",tr("Optional maximum % of goal (0–100)"),true)
                            val preview=remember(fields.toMap()) { runCatching { transform(data).allocate() }.getOrNull() }
                            val goal=data.goals.first { it.id==editor.owner }
                            if(preview!=null) {
                                val key="${fields.getValue("bucket")}/${editor.owner}"
                                Notice(tr("Preview: {0} from this bucket.\nGoal funded: {1} of {2}",money(data.convert(preview.byConnection[key] ?: ZERO,"USD",goal.currency),goal.currency),money(data.convert(preview.goal(goal.id),"USD",goal.currency),goal.currency),money(goal.target.decimal(),goal.currency))+(if(preview.incomplete) "\n"+tr("Incomplete: missing rates or balances.") else ""))
                            }
                            Note(tr("Limits are ceilings. Goal order, available savings, and other connections can reduce the contribution."))
                        }
                        "Settings" -> Choice(tr("Appearance"),fields.getValue("theme"),listOf("System","Light","Dark"),!saving,::shown) { fields["theme"]=it }
                    }
                    val oldCurrency=if(editor.kind=="Holding") h?.asset else if(editor.kind=="Goal") g?.currency else null
                    if(oldCurrency!=null && oldCurrency!=fields.getValue("currency").trim().uppercase() && (editor.kind!="Holding" || fields.getValue("type")=="Manual")) {
                        Choice(tr("Currency changed from {0}",oldCurrency),fields.getValue("currencyAction"),listOf("Convert existing values","Replace with entered numbers"),!saving,::shown) { fields["currencyAction"]=it }
                        Note(tr("Convert uses the saved amount and cached rates. Replace treats entered numbers (including fixed goal connection limits) as values in the new currency."))
                    }
                    Note(tr("Changes stay in this editor until Save. Cancel discards them."))
                }
            }
        }
        BackHandler { close() }
        if(discard) AlertDialog(onDismissRequest={ discard=false },title={ Text(tr("Discard edits?")) },confirmButton={ TextButton(onClick=onDismiss) { Text(tr("Discard")) } },dismissButton={ TextButton(onClick={ discard=false }) { Text(tr("Keep editing")) } })
    }
}
