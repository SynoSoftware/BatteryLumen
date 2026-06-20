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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.synosoftware.battery.ui.theme.ChromeAlpha
import com.synosoftware.battery.ui.theme.NavigationAlpha
import com.synosoftware.battery.ui.theme.appBackdropColors
import com.synosoftware.battery.ui.theme.appChromeColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BatteryAppRoot(
    viewModel: BatteryViewModel,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val pagerState = rememberPagerState(pageCount = { BatteryTab.entries.size })
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val mainRoute = "main"
    val settingsRoute = "settings"
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: mainRoute
    val isSettingsRoute = currentRoute == settingsRoute
    val currentTab = BatteryTab.entries[pagerState.currentPage]
    val titleKey = when {
        isSettingsRoute -> "settings.title"
        else -> currentTab.titleKey
    }
    val headerIconRes = when {
        isSettingsRoute -> R.drawable.lucide_settings
        else -> currentTab.iconRes
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BatteryEvent.TargetReached -> snackbarHostState.showSnackbar(context.resolveText(T("target.reached.snackbar", event.targetPercent)))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    appBackdropColors(darkTheme),
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
                        currentTab = currentTab,
                        onNavigate = { tab ->
                            scope.launch {
                                pagerState.animateScrollToPage(tab.ordinal)
                            }
                        },
                    )
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = mainRoute,
            ) {
                composable(mainRoute) {
                    MainTabsPager(
                        state = state,
                        pagerState = pagerState,
                        contentPadding = padding,
                        onTargetSelected = viewModel::setTargetChargePercent,
                        onSeedDemoData = viewModel::seedDebugSessions,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainTabsPager(
    state: com.synosoftware.battery.ui.model.BatteryUiState,
    pagerState: androidx.compose.foundation.pager.PagerState,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    onTargetSelected: (Int) -> Unit,
    onSeedDemoData: () -> Unit,
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
    ) { page ->
        when (BatteryTab.entries[page]) {
            BatteryTab.NOW -> NowScreen(
                state = state,
                onTargetSelected = onTargetSelected,
                contentPadding = contentPadding,
            )
            BatteryTab.HEALTH -> HealthScreen(
                state = state,
                onSeedDemoData = onSeedDemoData,
                contentPadding = contentPadding,
            )
            BatteryTab.LEDGER -> LedgerScreen(
                state = state,
                contentPadding = contentPadding,
            )
            BatteryTab.HOW_IT_WORKS -> HowItWorksScreen(
                state = state,
                contentPadding = contentPadding,
            )
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
        color = appChromeColor(darkTheme).copy(alpha = ChromeAlpha),
        contentColor = MaterialTheme.colorScheme.onSurface,
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
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
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
    currentTab: BatteryTab,
    onNavigate: (BatteryTab) -> Unit,
) {
        NavigationBar(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = NavigationAlpha),
        tonalElevation = 0.dp,
    ) {
        BatteryTab.entries.forEach { tab ->
            val selected = currentTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(tab) },
                alwaysShowLabel = true,
                icon = {
                    LucideIcon(
                        resId = tab.iconRes,
                        contentDescription = null,
                    )
                },
                label = {
                    AppText(
                        text = LocalContext.current.resolveText(T(tab.navLabelKey)),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                ),
            )
        }
    }
}
