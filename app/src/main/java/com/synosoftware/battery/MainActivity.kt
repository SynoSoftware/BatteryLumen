package com.synosoftware.battery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.synosoftware.battery.ui.BatteryAppRoot
import com.synosoftware.battery.ui.theme.BatteryTheme

class MainActivity : ComponentActivity() {
    private val viewModel: com.synosoftware.battery.ui.BatteryViewModel by viewModels {
        (application as BatteryApp).container.batteryViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BatteryTheme {
                BatteryAppRoot(viewModel = viewModel)
            }
        }
    }
}
