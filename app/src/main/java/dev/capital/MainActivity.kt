package dev.capital

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import dev.capital.ui.CapitalApp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// FragmentActivity (a ComponentActivity) because BiometricPrompt needs one.
class MainActivity: FragmentActivity() {
    private val model: CapitalModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // no screenshots and a blank recent-apps preview while encryption is on
        lifecycleScope.launch { model.state.map { it.encrypted }.distinctUntilChanged().collect { if(it) window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE) else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) } }
        setContent { CapitalApp(model) }
    }
    override fun onStart() { super.onStart(); model.resume() }
    override fun onStop() { if(!isChangingConfigurations) model.background(); super.onStop() }
}
