package com.synosoftware.battery.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text as AppText
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.synosoftware.battery.BuildConfig
import com.synosoftware.battery.R
import com.synosoftware.battery.data.preferences.ThemeMode
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
import com.synosoftware.battery.i18n.T
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BatteryAppRoot(
    viewModel: BatteryViewModel,
    darkTheme: Boolean,
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
    val titleRes = if (isSettingsRoute) R.string.settings_title else currentTab.titleRes
    val headerIconRes = when {
        isSettingsRoute -> R.drawable.lucide_settings
        else -> currentTab.iconRes
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BatteryEvent.TargetReached -> snackbarHostState.showSnackbar(
                    context.T(R.string.target_reached_snackbar, event.targetPercent),
                )
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
                    titleRes = titleRes,
                    iconRes = headerIconRes,
                    darkTheme = darkTheme,
                    isSettingsRoute = isSettingsRoute,
                    currentTab = currentTab,
                    currentThemeMode = state.themeMode,
                    currentTargetPercent = state.targetChargePercent,
                    onSettingsOpen = {
                        navController.navigate(settingsRoute) {
                            launchSingleTop = true
                        }
                    },
                    onSettingsClose = {
                        navController.popBackStack()
                    },
                    onNavigateToTab = { tab ->
                        if (isSettingsRoute) {
                            navController.popBackStack()
                        }
                        scope.launch {
                            pagerState.animateScrollToPage(tab.ordinal)
                        }
                    },
                    onThemeModeSelected = viewModel::setThemeMode,
                    onTargetSelected = viewModel::setTargetChargePercent,
                    onSeedDemoData = viewModel::seedDebugSessions,
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
                        onDesignCapacitySelected = viewModel::setDesignCapacityMah,
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
    titleRes: Int,
    iconRes: Int,
    darkTheme: Boolean,
    isSettingsRoute: Boolean,
    currentTab: BatteryTab,
    currentThemeMode: ThemeMode,
    currentTargetPercent: Int,
    onSettingsOpen: () -> Unit,
    onSettingsClose: () -> Unit,
    onNavigateToTab: (BatteryTab) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onTargetSelected: (Int) -> Unit,
    onSeedDemoData: () -> Unit,
) {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    resId = iconRes,
                    contentDescription = T(titleRes),
                )
                Column(
                    modifier = Modifier.padding(start = 12.dp),
                ) {
                    AppText(
                        text = T(titleRes),
                        style = MaterialTheme.typography.titleLarge,
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
                }
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(40.dp),
                    ) {
                        LucideIcon(
                            resId = R.drawable.lucide_more_vertical,
                            contentDescription = T(R.string.menu_more),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        AppMenuItem(
                            label = T(BatteryTab.NOW.navLabelRes),
                            selected = currentTab == BatteryTab.NOW,
                            onClick = {
                                menuExpanded = false
                                onNavigateToTab(BatteryTab.NOW)
                            },
                        )
                        AppMenuItem(
                            label = T(BatteryTab.HEALTH.navLabelRes),
                            selected = currentTab == BatteryTab.HEALTH,
                            onClick = {
                                menuExpanded = false
                                onNavigateToTab(BatteryTab.HEALTH)
                            },
                        )
                        AppMenuItem(
                            label = T(BatteryTab.LEDGER.navLabelRes),
                            selected = currentTab == BatteryTab.LEDGER,
                            onClick = {
                                menuExpanded = false
                                onNavigateToTab(BatteryTab.LEDGER)
                            },
                        )
                        AppMenuItem(
                            label = T(BatteryTab.HOW_IT_WORKS.navLabelRes),
                            selected = currentTab == BatteryTab.HOW_IT_WORKS,
                            onClick = {
                                menuExpanded = false
                                onNavigateToTab(BatteryTab.HOW_IT_WORKS)
                            },
                        )
                        AppMenuItem(
                            label = T(R.string.settings_title),
                            selected = isSettingsRoute,
                            onClick = {
                                menuExpanded = false
                                if (!isSettingsRoute) {
                                    onSettingsOpen()
                                }
                            },
                        )
                        HorizontalDivider()
                        MenuSectionHeader(text = T(R.string.settings_target_title))
                        listOf(80, 85, 90, 100).forEach { target ->
                            AppMenuItem(
                                label = T(R.string.value_percent, target),
                                selected = currentTargetPercent == target,
                                onClick = {
                                    menuExpanded = false
                                    onTargetSelected(target)
                                },
                            )
                        }
                        HorizontalDivider()
                        MenuSectionHeader(text = T(R.string.settings_theme_title))
                        ThemeMode.entries.forEach { mode ->
                            AppMenuItem(
                                label = T(themeLabel(mode)),
                                selected = currentThemeMode == mode,
                                onClick = {
                                    menuExpanded = false
                                    onThemeModeSelected(mode)
                                },
                            )
                        }
                        if (BuildConfig.DEBUG) {
                            HorizontalDivider()
                            AppMenuItem(
                                label = T(R.string.health_debug_seed_action),
                                selected = false,
                                onClick = {
                                    menuExpanded = false
                                    onSeedDemoData()
                                },
                            )
                        }
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
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = NavigationAlpha),
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
                        text = T(tab.navLabelRes),
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

@Composable
private fun MenuSectionHeader(text: String) {
    AppText(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun AppMenuItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            AppText(
                text = label,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
        },
        leadingIcon = if (selected) {
            {
                LucideIcon(
                    resId = R.drawable.lucide_check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            null
        },
        onClick = onClick,
    )
}

private fun themeLabel(mode: ThemeMode): Int {
    return when (mode) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
    }
}
