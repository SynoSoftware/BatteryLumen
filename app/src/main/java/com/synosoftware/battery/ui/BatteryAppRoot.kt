package com.synosoftware.battery.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.synosoftware.battery.R
import com.synosoftware.battery.i18n.resolveText
import com.synosoftware.battery.i18n.text
import com.synosoftware.battery.ui.model.BatteryEvent
import com.synosoftware.battery.ui.model.BatteryTab
import com.synosoftware.battery.ui.components.LucideIcon
import com.synosoftware.battery.ui.screens.HealthScreen
import com.synosoftware.battery.ui.screens.HowItWorksScreen
import com.synosoftware.battery.ui.screens.LedgerScreen
import com.synosoftware.battery.ui.screens.NowScreen

@Composable
fun BatteryAppRoot(
    viewModel: BatteryViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BatteryEvent.TargetReached -> snackbarHostState.showSnackbar(context.resolveText(text("target_reached_snackbar", event.targetPercent)))
                is BatteryEvent.Message -> snackbarHostState.showSnackbar(context.resolveText(event.text))
            }
        }
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: BatteryTab.NOW.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                BatteryTab.entries.forEach { tab ->
                    val selected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            val label = context.resolveText(text(tab.label))
                            when (tab) {
                                BatteryTab.NOW -> LucideIcon(R.drawable.lucide_battery_charging, contentDescription = label)
                                BatteryTab.HEALTH -> LucideIcon(R.drawable.lucide_heart, contentDescription = label)
                                BatteryTab.LEDGER -> LucideIcon(R.drawable.lucide_history, contentDescription = label)
                                BatteryTab.HOW_IT_WORKS -> LucideIcon(R.drawable.lucide_info, contentDescription = label)
                            }
                        },
                        label = { Text(context.resolveText(text(tab.label))) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BatteryTab.NOW.route,
        ) {
            composable(BatteryTab.NOW.route) {
                NowScreen(
                    state = state,
                    onTargetSelected = viewModel::setTargetChargePercent,
                    contentPadding = padding,
                )
            }
            composable(BatteryTab.HEALTH.route) {
                HealthScreen(
                    state = state,
                    contentPadding = padding,
                )
            }
            composable(BatteryTab.LEDGER.route) {
                LedgerScreen(
                    state = state,
                    contentPadding = padding,
                )
            }
            composable(BatteryTab.HOW_IT_WORKS.route) {
                HowItWorksScreen(
                    state = state,
                    contentPadding = padding,
                )
            }
        }
    }
}
