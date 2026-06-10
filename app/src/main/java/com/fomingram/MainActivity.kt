package com.fomingram

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.content.edit
import com.fomingram.ui.navigation.FomingramNavGraph
import com.fomingram.ui.theme.FomingramTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
            var darkTheme by remember { mutableStateOf(prefs.getBoolean("dark_theme", true)) }

            FomingramTheme(darkTheme = darkTheme) {
                FomingramNavGraph(
                    onThemeChange = { isDark: Boolean ->
                        darkTheme = isDark
                        prefs.edit { putBoolean("dark_theme", isDark) }
                    }
                )
            }
        }
    }
}
