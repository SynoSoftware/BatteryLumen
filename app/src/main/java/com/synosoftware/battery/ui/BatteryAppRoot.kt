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
import androidx.compose.ui.platform.LocalConfiguration
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
import com.synosoftware.battery.data.preferences.AppLanguage
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
import com.synosoftware.battery.i18n.withLanguage
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BatteryAppRoot(
    viewModel: BatteryViewModel,
    darkTheme: Boolean,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val baseContext = LocalContext.current
    val localizedContext = remember(baseContext, state.language) { baseContext.withLanguage(state.language) }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration,
    ) {
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
        var activeQuickSetting by rememberSaveable { mutableStateOf<QuickSetting?>(null) }

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
                        currentThemeMode = state.themeMode,
                        currentTargetPercent = state.targetChargePercent,
                        currentLanguage = state.language,
                        onSettingsOpen = {
                            navController.navigate(settingsRoute) {
                                launchSingleTop = true
                            }
                        },
                        onSettingsClose = {
                            navController.popBackStack()
                        },
                        onSeedDemoData = viewModel::seedDebugSessions,
                        onOpenQuickSetting = { setting -> activeQuickSetting = setting },
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

        when (activeQuickSetting) {
            QuickSetting.LANGUAGE -> QuickSettingSheet(
                title = T(R.string.settings_language_title),
                options = AppLanguage.entries,
                optionLabel = { T(languageNameRes(it)) },
                isSelected = { it == state.language },
                onSelect = { language ->
                    viewModel.setLanguage(language)
                    activeQuickSetting = null
                },
                onDismiss = { activeQuickSetting = null },
            )

            QuickSetting.THEME -> QuickSettingSheet(
                title = T(R.string.settings_theme_title),
                options = ThemeMode.entries,
                optionLabel = { T(themeLabel(it)) },
                isSelected = { it == state.themeMode },
                onSelect = { mode ->
                    viewModel.setThemeMode(mode)
                    activeQuickSetting = null
                },
                onDismiss = { activeQuickSetting = null },
            )

            QuickSetting.TARGET -> QuickSettingSheet(
                title = T(R.string.settings_target_title),
                options = QUICK_TARGET_OPTIONS,
                optionLabel = { T(R.string.value_percent, it) },
                isSelected = { it == state.targetChargePercent },
                onSelect = { target ->
                    viewModel.setTargetChargePercent(target)
                    activeQuickSetting = null
                },
                onDismiss = { activeQuickSetting = null },
            )

            null -> Unit
        }
    }
}

private enum class QuickSetting {
    LANGUAGE,
    THEME,
    TARGET,
}

private val QUICK_TARGET_OPTIONS = listOf(80, 85, 90, 100)

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
    currentThemeMode: ThemeMode,
    currentTargetPercent: Int,
    currentLanguage: AppLanguage,
    onSettingsOpen: () -> Unit,
    onSettingsClose: () -> Unit,
    onSeedDemoData: () -> Unit,
    onOpenQuickSetting: (QuickSetting) -> Unit,
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
                        modifier = Modifier.widthIn(min = 240.dp),
                    ) {
                        AppMenuItem(
                            label = T(R.string.settings_title),
                            onClick = {
                                menuExpanded = false
                                if (!isSettingsRoute) {
                                    onSettingsOpen()
                                }
                            },
                        )
                        HorizontalDivider()
                        MenuSectionHeader(text = T(R.string.settings_quick_settings_label))
                        AppMenuValueItem(
                            label = T(R.string.settings_language_title),
                            value = T(languageNameRes(currentLanguage)),
                            onClick = {
                                menuExpanded = false
                                onOpenQuickSetting(QuickSetting.LANGUAGE)
                            },
                        )
                        AppMenuValueItem(
                            label = T(R.string.settings_theme_title),
                            value = T(themeLabel(currentThemeMode)),
                            onClick = {
                                menuExpanded = false
                                onOpenQuickSetting(QuickSetting.THEME)
                            },
                        )
                        AppMenuValueItem(
                            label = T(R.string.settings_target_title),
                            value = T(R.string.value_percent, currentTargetPercent),
                            onClick = {
                                menuExpanded = false
                                onOpenQuickSetting(QuickSetting.TARGET)
                            },
                        )
                        if (BuildConfig.DEBUG) {
                            HorizontalDivider()
                            AppMenuItem(
                                label = T(R.string.health_debug_seed_action),
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
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { AppText(text = label) },
        onClick = onClick,
    )
}

@Composable
private fun AppMenuValueItem(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppText(text = label)
                AppText(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
        },
        onClick = onClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> QuickSettingSheet(
    title: String,
    options: List<T>,
    optionLabel: @Composable (T) -> String,
    isSelected: (T) -> Boolean,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            AppText(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            AppText(
                text = T(R.string.settings_quick_picker_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            options.forEach { option ->
                QuickSettingOptionRow(
                    label = optionLabel(option),
                    selected = isSelected(option),
                    onClick = { onSelect(option) },
                )
            }
        }
    }
}

@Composable
private fun QuickSettingOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppText(
            text = label,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
        if (selected) {
            LucideIcon(
                resId = R.drawable.lucide_check,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private fun languageNameRes(language: AppLanguage): Int {
    return when (language) {
        AppLanguage.ENGLISH -> R.string.language_name_en
        AppLanguage.PORTUGUESE -> R.string.language_name_pt
        AppLanguage.GERMAN -> R.string.language_name_de
        AppLanguage.FRENCH -> R.string.language_name_fr
    }
}

private fun themeLabel(mode: ThemeMode): Int {
    return when (mode) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
    }
}
