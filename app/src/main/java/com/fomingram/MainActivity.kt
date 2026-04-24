package com.fomingram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fomingram.ui.navigation.FomingramNavGraph
import com.fomingram.ui.theme.FomingramTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FomingramTheme {
                FomingramNavGraph()
            }
        }
    }
}
