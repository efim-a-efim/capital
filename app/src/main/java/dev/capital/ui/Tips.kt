package dev.capital.ui

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder
import dev.capital.R
import dev.capital.Tips
import dev.capital.domain.tr

@Composable internal fun TipsScreen(onNotice: (String)->Unit) {
    val context=LocalContext.current
    var qr by remember { mutableStateOf<String?>(null) }
    Text(tr("If you want to tip the developer, use one of the methods below. It won't change anything in the application's functionality, but will help the developer."))
    Tips.shown.forEach { (network,address) ->
        Heading(network)
        SelectionContainer { Text(address,fontFamily=FontFamily.Monospace,style=MaterialTheme.typography.bodyMedium) }
        Row {
            IconButton(onClick={ context.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText(network,address)); onNotice(tr("Address copied")) },modifier=Modifier.testTag("copy-$network")) { Icon(painterResource(R.drawable.ic_copy),contentDescription=tr("Copy {0} address",network)) }
            IconButton(onClick={ qr=if(qr==network) null else network },modifier=Modifier.testTag("qr-$network")) { Icon(painterResource(R.drawable.ic_qr),contentDescription=if(qr==network) tr("Hide QR code") else tr("Show QR code"),tint=if(qr==network) MaterialTheme.colorScheme.primary else LocalContentColor.current) }
        }
        if(qr==network) QrCode(address)
    }
}

// Plain address as payload, so any reader shows it. Rounded modules in the app colour on a white card: enough contrast for cameras.
@Composable private fun QrCode(text: String) {
    val matrix=remember(text) { Encoder.encode(text,ErrorCorrectionLevel.M,mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")).matrix }
    val ink=MaterialTheme.colorScheme.primary
    Surface(color=Color.White,shape=MaterialTheme.shapes.medium,modifier=Modifier.padding(vertical=8.dp)) {
        Canvas(Modifier.size(232.dp).padding(16.dp).testTag("qr")) {
            val module=size.width/matrix.width
            val radius=CornerRadius(module*0.3f)
            for(y in 0 until matrix.height) for(x in 0 until matrix.width) if(matrix.get(x,y).toInt()==1)
                drawRoundRect(ink,Offset(x*module,y*module),Size(module,module),radius)
        }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable internal fun TipButton(onClick: ()->Unit) {
    IconButton(onClick=onClick,modifier=Modifier.testTag("tip")) { Icon(painterResource(R.drawable.ic_tip),contentDescription=tr("Tip the developer")) }
}
