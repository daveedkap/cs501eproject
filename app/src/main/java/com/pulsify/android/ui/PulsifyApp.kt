package com.pulsify.android.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulsifyApp(factory: PulsifyViewModelFactory) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val onPlaylist = PulsifyDestinations.PLAYLIST
    val showBottomBar = currentRoute != null && currentRoute != onPlaylist

    Scaffold(
        topBar = {
            if (currentRoute == onPlaylist) {
                TopAppBar(
                    title = { Text("Playlist") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
                        }
                    },
                )
            } else {
                TopAppBar(title = { Text("Pulsify") })
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
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
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = PulsifyDestinations.HOME,
            modifier = Modifier.padding(innerPadding),
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
                PlaylistScreen(viewModel = vm)
            }
        }
    }
}
