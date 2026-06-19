package com.synosoftware.battery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text as AppText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.synosoftware.battery.i18n.T
import com.synosoftware.battery.i18n.resolveText
import com.synosoftware.battery.R
import com.synosoftware.battery.ui.components.IconBadge
import com.synosoftware.battery.ui.components.LucideIcon
import com.synosoftware.battery.ui.model.BatteryEvent
import com.synosoftware.battery.ui.model.BatteryTab
import com.synosoftware.battery.ui.screens.HealthScreen
import com.synosoftware.battery.ui.screens.HowItWorksScreen
import com.synosoftware.battery.ui.screens.LedgerScreen
import com.synosoftware.battery.ui.screens.NowScreen
import com.synosoftware.battery.ui.screens.SettingsScreen

@Composable
fun BatteryAppRoot(
    viewModel: BatteryViewModel,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val settingsRoute = "settings"
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: BatteryTab.NOW.route
    val currentTab = BatteryTab.entries.firstOrNull { it.route == currentRoute } ?: BatteryTab.NOW
    val isSettingsRoute = currentRoute == settingsRoute
    val titleKey = when {
        isSettingsRoute -> "settings_title"
        currentTab == BatteryTab.NOW -> "battery_health_title"
        else -> currentTab.label
    }
    val headerIconRes = when {
        isSettingsRoute -> R.drawable.lucide_settings
        currentTab == BatteryTab.NOW -> R.drawable.lucide_battery_full
        else -> currentTab.iconRes
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BatteryEvent.TargetReached -> snackbarHostState.showSnackbar(context.resolveText(T("target_reached_snackbar", event.targetPercent)))
                is BatteryEvent.Message -> snackbarHostState.showSnackbar(context.resolveText(event.text))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        if (darkTheme) Color(0xFF07101F) else Color(0xFFF4FBF8),
                        if (darkTheme) Color(0xFF0B1326) else Color(0xFFF7FBF9),
                        if (darkTheme) Color(0xFF060E20) else Color(0xFFF4FBF8),
                    ),
                ),
            ),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                AppTopBar(
                    title = context.resolveText(T(titleKey)),
                    iconRes = headerIconRes,
                    darkTheme = darkTheme,
                    isSettingsRoute = isSettingsRoute,
                    onSettingsOpen = {
                        navController.navigate(settingsRoute) {
                            launchSingleTop = true
                        }
                    },
                    onSettingsClose = {
                        navController.popBackStack()
                    },
                    onThemeToggle = onThemeToggle,
                )
            },
            bottomBar = {
                if (!isSettingsRoute) {
                    AppBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
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
                composable(settingsRoute) {
                    SettingsScreen(
                        state = state,
                        onTargetSelected = viewModel::setTargetChargePercent,
                        onTemperatureUnitSelected = viewModel::setTemperatureUnit,
                        onExperimentalMetricsChanged = viewModel::setExperimentalMetricsEnabled,
                        onThemeModeSelected = viewModel::setThemeMode,
                        contentPadding = padding,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppTopBar(
    title: String,
    iconRes: Int,
    darkTheme: Boolean,
    isSettingsRoute: Boolean,
    onSettingsOpen: () -> Unit,
    onSettingsClose: () -> Unit,
    onThemeToggle: () -> Unit,
) {
    Surface(
        color = if (darkTheme) Color(0xFF07101F).copy(alpha = 0.92f) else Color(0xFFF7FBF9).copy(alpha = 0.92f),
        contentColor = Color.Unspecified,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    resId = iconRes,
                    contentDescription = title,
                )
                Column(
                    modifier = Modifier.padding(start = 12.dp),
                ) {
                    AppText(
                        text = title,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSettingsRoute) {
                    IconButton(
                        onClick = onSettingsClose,
                        modifier = Modifier.size(40.dp),
                    ) {
                        LucideIcon(
                            resId = R.drawable.lucide_x,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                } else {
                    IconButton(
                        onClick = onSettingsOpen,
                        modifier = Modifier.size(40.dp),
                    ) {
                        LucideIcon(
                            resId = R.drawable.lucide_settings,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    IconButton(
                        onClick = onThemeToggle,
                        modifier = Modifier.size(40.dp),
                    ) {
                        LucideIcon(
                            resId = if (darkTheme) R.drawable.lucide_sun else R.drawable.lucide_moon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
) {
    NavigationBar(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.96f),
        tonalElevation = 0.dp,
    ) {
        BatteryTab.entries.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(tab.route) },
                alwaysShowLabel = true,
                icon = {
                    LucideIcon(
                        resId = tab.iconRes,
                        contentDescription = null,
                    )
                },
                label = {
                    AppText(text = LocalContext.current.resolveText(T(tab.label)))
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                ),
            )
        }
    }
}
