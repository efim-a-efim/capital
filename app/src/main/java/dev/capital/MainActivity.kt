package dev.capital

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dev.capital.ui.CapitalApp

class MainActivity: ComponentActivity() {
    private val model: CapitalModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { CapitalApp(model) }
    }
    override fun onStart() { super.onStart(); model.resume() }
    override fun onStop() { model.background(); super.onStop() }
}
