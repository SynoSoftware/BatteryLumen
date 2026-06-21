package com.synosoftware.battery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.synosoftware.battery.ui.BatteryAppRoot
import com.synosoftware.battery.data.preferences.ThemeMode
import com.synosoftware.battery.ui.theme.BatteryTheme

class MainActivity : ComponentActivity() {
    private val viewModel: com.synosoftware.battery.ui.BatteryViewModel by viewModels {
        (application as BatteryApp).container.batteryViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            BatteryTheme(darkTheme = darkTheme) {
                BatteryAppRoot(
                    viewModel = viewModel,
                    darkTheme = darkTheme,
                )
            }
        }
    }
}
