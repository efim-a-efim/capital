package dev.capital.ui

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dev.capital.CapitalModel
import dev.capital.ScreenState
import dev.capital.data.*
import dev.capital.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.crypto.Cipher

private fun Context.fragmentActivity(): FragmentActivity? { var c=this; while(c is ContextWrapper) { if(c is FragmentActivity) return c; c=c.baseContext }; return null }

/** Cancel and "Use PIN" are silent; every other error is passed on. */
private fun biometricPrompt(context: Context,cipher: Cipher,negative: String,onOk: (Cipher)->Unit,onError: (String)->Unit) {
    val activity=context.fragmentActivity() ?: return onError("Biometric prompt unavailable")
    val callback=object: BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { result.cryptoObject?.cipher?.let(onOk) ?: onError("Biometric check failed") }
        override fun onAuthenticationError(code: Int,message: CharSequence) { if(code !in listOf(BiometricPrompt.ERROR_NEGATIVE_BUTTON,BiometricPrompt.ERROR_USER_CANCELED,BiometricPrompt.ERROR_CANCELED)) onError(message.toString()) }
    }
    BiometricPrompt(activity,ContextCompat.getMainExecutor(activity),callback).authenticate(
        BiometricPrompt.PromptInfo.Builder().setTitle("Unlock Capital").setNegativeButtonText(negative).setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG).build(),
        BiometricPrompt.CryptoObject(cipher))
}

// ponytail: Compose text fields hold Strings; callers convert at submit and clear the field at once, the String itself waits for GC.
@Composable private fun Secret(value: String,label: String,tag: String,enabled: Boolean=true,numeric: Boolean=false,last: Boolean=false,onDone: ()->Unit={},onChange: (String)->Unit) {
    OutlinedTextField(value,onValueChange=onChange,label={ Text(label) },modifier=Modifier.fillMaxWidth().testTag(tag),singleLine=true,enabled=enabled,visualTransformation=PasswordVisualTransformation(),
        keyboardOptions=KeyboardOptions(keyboardType=if(numeric) KeyboardType.NumberPassword else KeyboardType.Password,imeAction=if(last) ImeAction.Done else ImeAction.Next),keyboardActions=KeyboardActions(onDone={ onDone() }))
}
@Composable private fun Checking(rewrite: Rewrite?,label: String) {
    Column(Modifier.fillMaxWidth().testTag("checking"),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        if(rewrite!=null && rewrite.total>0) LinearProgressIndicator(progress={ rewrite.done.toFloat()/rewrite.total },modifier=Modifier.fillMaxWidth()) else LinearProgressIndicator(Modifier.fillMaxWidth())
        Text(if(rewrite!=null) "$label ${rewrite.done} of ${rewrite.total} files" else "$label…")
    }
}

// ---- lock screens: shown alone while security != OPEN ----
@Composable internal fun LockedScreen(model: CapitalModel,state: ScreenState,onUnlocked: ()->Unit) {
    var viaPassword by rememberSaveable { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding().semantics { testTagsAsResourceId=true }) {
        if(state.security=="PIN" && !viaPassword) PinScreen(model,state) { viaPassword=true }
        else PasswordScreen(model,state,onUnlocked,if(state.security=="PIN" && model.lock.hasPin && model.lock.blockedForMillis()==0L) ({ viaPassword=false }) else null)
        state.message?.let { Box(Modifier.align(Alignment.TopCenter)) { Popup(it,model::dismissMessage) } }
    }
}
@Composable private fun PasswordScreen(model: CapitalModel,state: ScreenState,onUnlocked: ()->Unit,onBack: (()->Unit)?) {
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    fun submit() {
        if(password.isEmpty() || state.checking) return
        val chars=password.toCharArray(); password=""; error=null
        model.submitPassword(chars) { error=it; if(it==null) onUnlocked() }
    }
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("Capital is encrypted",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.SemiBold)
        Secret(password,"Password","password",!state.checking,last=true,onDone=::submit) { password=it; error=null }
        error?.let { Text(it,color=MaterialTheme.colorScheme.error,modifier=Modifier.semantics { liveRegion=LiveRegionMode.Polite }.testTag("error")) }
        if(state.checking) Checking(state.rewrite,"Checking")
        Button(onClick=::submit,enabled=!state.checking && password.isNotEmpty(),modifier=Modifier.heightIn(min=48.dp).testTag("unlock")) { Text("Unlock") }
        onBack?.let { TextButton(onClick=it,modifier=Modifier.heightIn(min=48.dp).testTag("back-to-pin")) { Text("Back to PIN") } }
        Note("There is no password recovery.")
    }
}
@Composable private fun RowScope.PinKey(label: String,tag: String,enabled: Boolean,primary: Boolean=false,onClick: ()->Unit) {
    val modifier=Modifier.weight(1f).widthIn(min=64.dp).heightIn(min=56.dp).testTag(tag)
    if(primary) Button(onClick=onClick,enabled=enabled,modifier=modifier) { Text(label) } else OutlinedButton(onClick=onClick,enabled=enabled,modifier=modifier) { Text(label) }
}
@Composable private fun PinScreen(model: CapitalModel,state: ScreenState,onPassword: ()->Unit) {
    val lock=model.lock; val context=LocalContext.current
    val digits=remember { CharArray(12) }
    var count by remember { mutableIntStateOf(0) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var error by remember { mutableStateOf<String?>(null) }
    var working by remember { mutableStateOf(false) }
    var bio by remember { mutableStateOf(lock.biometricEnabled) }
    LaunchedEffect(Unit) { while(true) { delay(1000); now=System.currentTimeMillis() } }
    val blocked=lock.blockedForMillis(now)
    val idle=!working && !state.checking
    fun biometricOpened(cipher: Cipher) {
        val key=try { lock.openBiometric(cipher) } catch(_: Exception) { lock.disableBiometric(); bio=false; error="Biometric unlock failed. Use your PIN."; return }
        model.unlockWith(key) { key.wipe(); error=it }
    }
    fun useBiometrics() {
        val cipher=lock.biometricDecryptCipher()
        if(cipher==null) { bio=false; error="Biometric unlock was switched off, for example because fingerprints changed. Use your PIN."; return }
        biometricPrompt(context,cipher,"Use PIN",::biometricOpened) { error=it }
    }
    var prompted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { if(!prompted && bio) { prompted=true; useBiometrics() } }
    fun submit() {
        if(count<4 || !idle || blocked>0) return
        val pin=digits.copyOf(count); digits.fill('\u0000'); count=0; error=null; working=true
        model.submitPin(pin) { error=it; working=false; now=System.currentTimeMillis() }
    }
    Column(Modifier.fillMaxSize().padding(horizontal=20.dp)) {
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp,Alignment.CenterVertically)) {
            Text("Enter PIN",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.SemiBold)
            Text(if(count==0) " " else "●".repeat(count),style=MaterialTheme.typography.headlineMedium,modifier=Modifier.heightIn(min=48.dp).semantics { contentDescription="$count digits entered" }.testTag("pin-dots"))
            if(blocked>0) Text("Too many wrong entries. Try again in ${formatWait(blocked)}",color=MaterialTheme.colorScheme.error,modifier=Modifier.testTag("pin-blocked"))
            else error?.let { Text(it,color=MaterialTheme.colorScheme.error,modifier=Modifier.semantics { liveRegion=LiveRegionMode.Polite }.testTag("error")) }
            if(working) LinearProgressIndicator(Modifier.fillMaxWidth())
            if(bio) OutlinedButton(onClick=::useBiometrics,enabled=idle,modifier=Modifier.heightIn(min=48.dp).testTag("use-biometrics")) { Text("Use biometrics") }
            val open=idle && blocked==0L
            Column(Modifier.widthIn(max=320.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                listOf(listOf("1","2","3"),listOf("4","5","6"),listOf("7","8","9")).forEach { row -> Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { row.forEach { d -> PinKey(d,"pin-$d",open) { if(count<12) { digits[count]=d[0]; count++; error=null } } } } }
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    PinKey("Delete","pin-delete",open) { if(count>0) { count--; digits[count]='\u0000' } }
                    PinKey("0","pin-0",open) { if(count<12) { digits[count]='0'; count++; error=null } }
                    PinKey("OK","pin-ok",open && count>=4,primary=true) { submit() }
                }
            }
        }
        // Always available: also while blocked and after the PIN was removed.
        TextButton(onClick=onPassword,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("use-password")) { Text("Use password") }
    }
}

// ---- Settings ----
@Composable private fun SwitchRow(label: String,checked: Boolean,enabled: Boolean,tag: String,onChange: (Boolean)->Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(label,Modifier.weight(1f))
        Switch(checked=checked,onCheckedChange=onChange,enabled=enabled,modifier=Modifier.testTag(tag).semantics { contentDescription=label })
    }
}
@Composable internal fun SecuritySection(model: CapitalModel,state: ScreenState,rev: Int,onLock: ()->Unit,open: (String)->Unit) {
    val lock=model.lock; val context=LocalContext.current
    val hasPin=remember(rev,state.encrypted) { lock.hasPin }
    val bio=remember(rev,state.encrypted) { lock.biometricEnabled }
    val canBio=remember(rev,state.encrypted) { lock.canUseBiometrics(context) }
    val times=listOf("Immediately","1 minute","5 minutes")
    fun enrollBiometrics() {
        val cipher=try { lock.biometricEncryptCipher() } catch(e: Exception) { return model.notice(e.message ?: "Biometrics unavailable") }
        biometricPrompt(context,cipher,"Cancel",{ c ->
            try { val key=model.currentKey() ?: error("Unlock the folder first"); try { lock.storeBiometric(c,key) } finally { key.wipe() }; onLock(); model.notice("Biometric unlock is on") }
            catch(e: Exception) { model.notice(e.message ?: "Could not save biometric unlock") }
        }) { model.notice(it) }
    }
    Heading("Security")
    SwitchRow("Encryption",state.encrypted,state.ready && !state.checking,"encryption") { open(if(it) "risk" else "disable") }
    Note(if(state.encrypted) "Snapshots in the folder are encrypted with your password. There is no password recovery." else "Snapshots in the folder are readable by anyone with access to it.")
    if(state.pendingRewrite!=null) {
        Notice("An encryption change was interrupted.")
        Button(onClick={ model.resumeRewrite { it?.let(model::notice) } },enabled=!state.checking,modifier=Modifier.heightIn(min=48.dp).testTag("finish-rewrite")) { Text("Finish now") }
    }
    Actions {
        OutlinedButton(onClick={ open("change") },enabled=state.encrypted && state.ready,modifier=Modifier.heightIn(min=48.dp).testTag("change-password")) { Text("Change password") }
        if(hasPin) {
            OutlinedButton(onClick={ open("pin-change") },enabled=state.encrypted,modifier=Modifier.heightIn(min=48.dp).testTag("change-pin")) { Text("Change PIN") }
            OutlinedButton(onClick={ open("pin-remove") },enabled=state.encrypted,modifier=Modifier.heightIn(min=48.dp).testTag("remove-pin")) { Text("Remove PIN") }
        } else OutlinedButton(onClick={ open("pin-set") },enabled=state.encrypted && state.ready,modifier=Modifier.heightIn(min=48.dp).testTag("set-pin")) { Text("Set PIN") }
    }
    if(!state.encrypted) Note("PIN and biometric unlock are available when encryption is on. Without encryption the app opens directly.")
    val why=when { !state.encrypted -> "Biometric unlock needs encryption to be on."; !hasPin -> "Set a PIN first."; !canBio -> "No strong biometrics (fingerprint or face) are enrolled on this device."; else -> null }
    SwitchRow("Unlock with biometrics",bio,why==null,"biometrics") { if(it) enrollBiometrics() else { lock.disableBiometric(); onLock() } }
    why?.let { Note(it) }
    Choice("Lock after time in background",times[when(lock.lockAfterSeconds) { 0 -> 0; 300 -> 2; else -> 1 }],times,hasPin) { lock.lockAfterSeconds=listOf(0,60,300)[times.indexOf(it)]; onLock() }
}

// ---- dialogs ----
@Composable private fun Ask(state: ScreenState,title: String,text: String?,confirm: String,tag: String,label: String,enabled: Boolean,error: String?,working: Boolean=false,onDismiss: ()->Unit,onConfirm: ()->Unit,fields: @Composable ColumnScope.()->Unit) {
    val busy=state.checking || working
    AlertDialog(onDismissRequest={ if(!busy) onDismiss() },modifier=Modifier.semantics { testTagsAsResourceId=true },properties=DialogProperties(dismissOnClickOutside=false),title={ Text(title) },
        text={ Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            text?.let { Text(it) }
            fields()
            error?.let { Text(it,color=MaterialTheme.colorScheme.error,modifier=Modifier.semantics { liveRegion=LiveRegionMode.Polite }.testTag("error")) }
            if(state.checking) Checking(state.rewrite,label) else if(working) LinearProgressIndicator(Modifier.fillMaxWidth())
        } },
        confirmButton={ TextButton(onClick=onConfirm,enabled=enabled && !busy,modifier=Modifier.heightIn(min=48.dp).testTag(tag)) { Text(confirm) } },
        dismissButton={ TextButton(onClick=onDismiss,enabled=!busy,modifier=Modifier.heightIn(min=48.dp)) { Text("Cancel") } })
}
private fun pinOp(model: CapitalModel,kind: String,current: CharArray,pin: CharArray): String? {
    val lock=model.lock
    val key=if(kind=="pin-set") model.currentKey() ?: return "Unlock the folder first"
        else lock.unlockWithPin(current) ?: return if(lock.hasPin) "Wrong PIN" else "PIN removed after 10 wrong entries. Use your password."
    try { if(kind=="pin-remove") lock.removePin() else lock.setPin(pin,key) } finally { key.wipe() }
    return null
}
@Composable private fun PinDialog(kind: String,model: CapitalModel,state: ScreenState,onLock: ()->Unit,close: ()->Unit) {
    val scope=rememberCoroutineScope()
    var current by remember { mutableStateOf("") }; var pin by remember { mutableStateOf("") }; var again by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }; var working by remember { mutableStateOf(false) }
    val askCurrent=kind!="pin-set"; val askNew=kind!="pin-remove"
    Ask(state,when(kind) { "pin-set" -> "Set PIN"; "pin-change" -> "Change PIN"; else -> "Remove PIN" },if(askNew) "4 to 12 digits." else "PIN and biometric unlock will be removed.",if(askNew) "Save PIN" else "Remove PIN","pin-confirm","Checking",
        (!askCurrent || current.isNotEmpty()) && (!askNew || (pin.isNotEmpty() && again.isNotEmpty())),error,working,close,{
            val cur=current.toCharArray(); val a=pin.toCharArray(); val b=again.toCharArray()
            current=""; pin=""; again=""
            val problem=if(askNew) validPin(a) ?: if(!a.contentEquals(b)) "PINs do not match" else null else null
            b.fill('\u0000')
            if(problem!=null) { cur.fill('\u0000'); a.fill('\u0000'); error=problem } else {
                error=null; working=true
                scope.launch {
                    val result=try { withContext(Dispatchers.Default) { pinOp(model,kind,cur,a) } } catch(e: Exception) { e.message ?: "PIN operation failed" }
                    finally { cur.fill('\u0000'); a.fill('\u0000') }
                    working=false; onLock()
                    if(result==null) { close(); model.notice(when(kind) { "pin-set" -> "PIN set"; "pin-change" -> "PIN changed"; else -> "PIN removed" }) } else error=result
                }
            }
        }) {
        if(askCurrent) Secret(current,"Current PIN","current-pin",!working,true) { current=it; error=null }
        if(askNew) { Secret(pin,"New PIN","new-pin",!working,true) { pin=it; error=null }; Secret(again,"Confirm PIN","confirm-pin",!working,true,last=true) { again=it; error=null } }
    }
}

/** One dialog at a time, chosen by [dialog]; also the fallback progress for password checks that run without a dialog. */
@Composable internal fun SecurityDialogs(model: CapitalModel,state: ScreenState,dialog: String?,onLock: ()->Unit,close: ()->Unit,open: (String)->Unit) {
    val pending=remember { arrayOfNulls<CharArray>(1) }
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val password=pending[0]; pending[0]=null
        if(password!=null) { if(uri==null) password.fill('\u0000') else model.exportPlaintext(uri,password) { it?.let(model::notice) } }
    }
    when(dialog) {
        "risk" -> AlertDialog(onDismissRequest=close,modifier=Modifier.semantics { testTagsAsResourceId=true },title={ Text("Before you encrypt") },text={
            Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text("There is no password recovery. If you lose the password, the data cannot be opened.")
                Text(if(state.plaintextExports>0) "You exported ${state.plaintextExports} plaintext ${if(state.plaintextExports==1) "backup" else "backups"}, the last on ${time(state.lastPlaintextExport)}." else "Backups exported earlier, including with older versions, are not tracked.")
                Text("Backups exported without encryption stay readable. The app cannot encrypt or delete them.")
                Text("Copies already held by sync tools are outside the app's control.")
            } },
            confirmButton={ TextButton(onClick={ open("enable") },modifier=Modifier.heightIn(min=48.dp).testTag("accept-risk")) { Text("I accept the risk") } },
            dismissButton={ TextButton(onClick=close,modifier=Modifier.heightIn(min=48.dp).testTag("cancel-risk")) { Text("Cancel") } })
        "enable" -> {
            var pw by remember { mutableStateOf("") }; var again by remember { mutableStateOf("") }; var error by remember { mutableStateOf<String?>(null) }
            Ask(state,"Choose a password","There is no password recovery.","Encrypt","encrypt","Encrypting",pw.isNotEmpty() && again.isNotEmpty(),error,onDismiss=close,onConfirm={
                val a=pw.toCharArray(); val b=again.toCharArray()
                val problem=validPassword(a) ?: if(!a.contentEquals(b)) "Passwords do not match" else null
                b.fill('\u0000')
                if(problem!=null) { a.fill('\u0000'); error=problem } else {
                    pw=""; again=""; error=null
                    model.enableEncryption(a,true) { if(it==null) { onLock(); model.notice("Encryption is on"); open("pin-offer") } else error=it }
                }
            }) {
                Secret(pw,"Password","new-password",!state.checking) { pw=it; error=null }
                Secret(again,"Confirm password","confirm-password",!state.checking,last=true) { again=it; error=null }
            }
        }
        "disable" -> {
            var pw by remember { mutableStateOf("") }; var error by remember { mutableStateOf<String?>(null) }
            Ask(state,"Turn off encryption","All files in the folder will be decrypted. PIN and biometric unlock will be removed.","Decrypt","decrypt","Decrypting",pw.isNotEmpty(),error,onDismiss=close,onConfirm={
                val a=pw.toCharArray(); pw=""; error=null
                model.disableEncryption(a) { onLock(); if(it==null) { close(); model.notice("Encryption is off") } else error=it }
            }) { Secret(pw,"Password","password",!state.checking,last=true) { pw=it; error=null } }
        }
        "change" -> {
            var cur by remember { mutableStateOf("") }; var pw by remember { mutableStateOf("") }; var again by remember { mutableStateOf("") }; var error by remember { mutableStateOf<String?>(null) }
            Ask(state,"Change password","There is no password recovery. Your PIN will be removed; set it again afterwards.","Change","change-confirm","Re-encrypting",cur.isNotEmpty() && pw.isNotEmpty() && again.isNotEmpty(),error,onDismiss=close,onConfirm={
                val c=cur.toCharArray(); val a=pw.toCharArray(); val b=again.toCharArray()
                val problem=if(!a.contentEquals(b)) "Passwords do not match" else null
                b.fill('\u0000')
                if(problem!=null) { c.fill('\u0000'); a.fill('\u0000'); error=problem } else {
                    cur=""; pw=""; again=""; error=null
                    model.changePassword(c,a) { onLock(); if(it==null) { close(); model.notice("Password changed. Set your PIN again.") } else error=it }
                }
            }) {
                Secret(cur,"Current password","current-password",!state.checking) { cur=it; error=null }
                Secret(pw,"New password","new-password",!state.checking) { pw=it; error=null }
                Secret(again,"Confirm new password","confirm-password",!state.checking,last=true) { again=it; error=null }
            }
        }
        "pin-set","pin-change","pin-remove" -> PinDialog(dialog,model,state,onLock,close)
        "pin-offer" -> AlertDialog(onDismissRequest=close,modifier=Modifier.semantics { testTagsAsResourceId=true },title={ Text("Set a PIN?") },text={ Text("A PIN unlocks Capital without typing your password each time.") },
            confirmButton={ TextButton(onClick={ open("pin-set") },modifier=Modifier.heightIn(min=48.dp).testTag("offer-pin")) { Text("Set a PIN") } },
            dismissButton={ TextButton(onClick=close,modifier=Modifier.heightIn(min=48.dp).testTag("skip-pin")) { Text("Not now") } })
        "plain" -> {
            var pw by remember { mutableStateOf("") }
            Ask(state,"Export without encryption","This file will be readable by anyone who gets it.","Continue","export-continue","Checking",pw.isNotEmpty(),null,onDismiss=close,onConfirm={
                pending[0]=pw.toCharArray(); pw=""; close(); model.expectReturn(); picker.launch("capital-backup-plaintext-${LocalDate.now()}.json")
            }) { Secret(pw,"Password","password",!state.checking,last=true) { pw=it } }
        }
    }
    if(state.restorePassword!=null) {
        var pw by remember { mutableStateOf("") }; var error by remember { mutableStateOf<String?>(null) }
        Ask(state,"Backup password","This backup is encrypted. Enter the password it was exported with.","Open","restore-unlock","Checking",pw.isNotEmpty(),error,onDismiss=model::cancelRestore,onConfirm={
            val a=pw.toCharArray(); pw=""; error=null
            model.inspectRestoreWith(a) { error=it }
        }) { Secret(pw,"Backup password","backup-password",!state.checking,last=true) { pw=it; error=null } }
    }
    // Password checks that run outside a dialog (plaintext export after the file picker, "Finish now").
    if(state.checking && dialog==null && state.restorePassword==null) Dialog(onDismissRequest={},properties=DialogProperties(dismissOnBackPress=false,dismissOnClickOutside=false)) {
        Surface(shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surface) { Box(Modifier.padding(24.dp)) { Checking(state.rewrite,when(state.pendingRewrite) { "on" -> "Encrypting"; "off" -> "Decrypting"; "change" -> "Re-encrypting"; else -> "Checking" }) } }
    }
}
