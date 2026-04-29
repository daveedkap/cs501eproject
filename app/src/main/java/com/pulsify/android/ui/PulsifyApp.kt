package com.pulsify.android.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pulsify.android.di.PulsifyViewModelFactory
import com.pulsify.android.navigation.PulsifyDestinations
import com.pulsify.android.ui.home.HomeScreen
import com.pulsify.android.ui.home.HomeViewModel
import com.pulsify.android.ui.map.MapScreen
import com.pulsify.android.ui.map.MapViewModel
import com.pulsify.android.ui.playlist.PlaylistScreen
import com.pulsify.android.ui.playlist.PlaylistViewModel
import com.pulsify.android.ui.sessions.SessionsScreen
import com.pulsify.android.ui.sessions.SessionsViewModel
import com.pulsify.android.ui.settings.SettingsScreen
import com.pulsify.android.ui.settings.SettingsViewModel

private data class TabSpec(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val tabs = listOf(
    TabSpec(PulsifyDestinations.HOME, "Home", Icons.Default.Home),
    TabSpec(PulsifyDestinations.SESSIONS, "Sessions", Icons.AutoMirrored.Filled.List),
    TabSpec(PulsifyDestinations.MAP, "Map", Icons.Default.Map),
    TabSpec(PulsifyDestinations.SETTINGS, "Settings", Icons.Default.Settings),
)

@Composable
fun PulsifyApp(factory: PulsifyViewModelFactory) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val showBottomBar = currentRoute != null && currentRoute != PulsifyDestinations.PLAYLIST

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                ) {
                    tabs.forEach { tab ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == tab.route } == true
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
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(22.dp),
                                )
                            },
                            label = {
                                Text(
                                    tab.label,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            NavHost(
                navController = navController,
                startDestination = PulsifyDestinations.HOME,
            ) {
                composable(PulsifyDestinations.HOME) {
                    val vm: HomeViewModel = viewModel(factory = factory)
                    HomeScreen(
                        viewModel = vm,
                        onOpenPlaylist = { navController.navigate(PulsifyDestinations.PLAYLIST) },
                    )
                }
                composable(PulsifyDestinations.SESSIONS) {
                    val vm: SessionsViewModel = viewModel(factory = factory)
                    SessionsScreen(viewModel = vm)
                }
                composable(PulsifyDestinations.MAP) {
                    val vm: MapViewModel = viewModel(factory = factory)
                    MapScreen(viewModel = vm)
                }
                composable(PulsifyDestinations.SETTINGS) {
                    val vm: SettingsViewModel = viewModel(factory = factory)
                    SettingsScreen(viewModel = vm)
                }
                composable(PulsifyDestinations.PLAYLIST) {
                    val vm: PlaylistViewModel = viewModel(factory = factory)
                    PlaylistScreen(
                        viewModel = vm,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
