package com.soundwave.player.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.soundwave.player.ui.components.MiniPlayer
import com.soundwave.player.ui.screens.album.AlbumScreen
import com.soundwave.player.ui.screens.artist.ArtistScreen
import com.soundwave.player.ui.screens.equalizer.EqualizerScreen
import com.soundwave.player.ui.screens.home.HomeScreen
import com.soundwave.player.ui.screens.splash.SplashScreen
import com.soundwave.player.ui.screens.library.LibraryScreen
import com.soundwave.player.ui.screens.library.FolderScreen
import com.soundwave.player.ui.screens.library.GenreScreen
import com.soundwave.player.ui.screens.lyrics.LyricsScreen
import com.soundwave.player.ui.screens.player.PlayerScreen
import com.soundwave.player.ui.screens.player.QueueScreen
import com.soundwave.player.ui.screens.playlist.PlaylistScreen
import com.soundwave.player.ui.screens.search.SearchScreen
import com.soundwave.player.ui.screens.settings.SettingsScreen
import com.soundwave.player.ui.screens.main.MainPagerScreen
import com.soundwave.player.ui.screens.timer.SleepTimerScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // الشاشات التي لا يظهر فيها شريط التنقل السفلي
    val hideBottomBarRoutes = listOf(
        Screen.Splash.route,
        Screen.Player.route,
        Screen.Equalizer.route,
        Screen.Lyrics.route,
        Screen.SleepTimer.route,
        Screen.Queue.route,
        Screen.Folder.route,
        Screen.Genre.route
    )
    
    val showBottomBar = currentDestination?.route !in hideBottomBarRoutes
    
    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                // Bottom Navigation Bar
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    BottomNavItem.items.forEach { item ->
                        val selected = when (item.route) {
                            Screen.Home.route -> currentDestination?.route == "main_pager/0" || currentDestination?.route == Screen.Home.route
                            Screen.Library.route -> currentDestination?.route == "main_pager/1" || currentDestination?.route == Screen.Library.route
                            Screen.Search.route -> currentDestination?.route == "main_pager/2" || currentDestination?.route == Screen.Search.route
                            Screen.Settings.route -> currentDestination?.route == "main_pager/3" || currentDestination?.route == Screen.Settings.route
                            else -> false
                        }
                        
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                val pageIndex = when (item.route) {
                                    Screen.Home.route -> 0
                                    Screen.Library.route -> 1
                                    Screen.Search.route -> 2
                                    Screen.Settings.route -> 3
                                    else -> 0
                                }
                                navController.navigate("main_pager/$pageIndex") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.padding(
                    bottom = if (showBottomBar) paddingValues.calculateBottomPadding() else 0.dp
                ),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                    initialOffsetX = { 50 },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                    targetOffsetX = { 50 },
                    animationSpec = tween(300)
                )
            }
        ) {
            // Main Pager (Home, Library, Search, Settings)
            composable(
                route = "main_pager/{page}",
                arguments = listOf(navArgument("page") { type = NavType.IntType })
            ) { backStackEntry ->
                val page = backStackEntry.arguments?.getInt("page") ?: 0
                MainPagerScreen(
                    initialPage = page,
                    onNavigateToPlayer = { navController.navigate(Screen.Player.route) },
                    onNavigateToAlbum = { navController.navigate(Screen.Album.createRoute(it)) },
                    onNavigateToArtist = { navController.navigate(Screen.Artist.createRoute(it)) },
                    onNavigateToPlaylist = { navController.navigate(Screen.Playlist.createRoute(it)) },
                    onNavigateToFolder = { navController.navigate(Screen.Folder.createRoute(it)) },
                    onNavigateToGenre = { navController.navigate(Screen.Genre.createRoute(it)) },
                    onNavigateToLibrary = { navController.navigate("main_pager/1") },
                    onNavigateToSettings = { navController.navigate("main_pager/3") },
                    onNavigateToSearch = { navController.navigate("main_pager/2") },
                    onPageChanged = { newPage ->
                        // Optional: update route without adding to backstack to reflect current page
                        // but navigate is simpler for synchronization with bottom bar
                    }
                )
            }

            // Splash Screen
            composable(Screen.Splash.route) {
                SplashScreen(
                    onTimeout = {
                        navController.navigate("main_pager/0") {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            
            // Legacy Home route (redirect to pager)
            composable(Screen.Home.route) {
                LaunchedEffect(Unit) {
                    navController.navigate("main_pager/0") {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            }
            
            // Legacy Library route (redirect to pager)
            composable(Screen.Library.route) {
                LaunchedEffect(Unit) {
                    navController.navigate("main_pager/1") {
                        popUpTo(Screen.Library.route) { inclusive = true }
                    }
                }
            }
            
            // Player Screen
            composable(Screen.Player.route) {
                PlayerScreen(
                    onBackClick = { navController.popBackStack() },
                    onNavigateToEqualizer = { navController.navigate(Screen.Equalizer.route) },
                    onNavigateToLyrics = { navController.navigate(Screen.Lyrics.route) },
                    onNavigateToQueue = { navController.navigate(Screen.Queue.route) },
                    onNavigateToTimer = { navController.navigate(Screen.SleepTimer.route) },
                    onNavigateToArtist = { navController.navigate(Screen.Artist.createRoute(it)) },
                    onNavigateToAlbum = { navController.navigate(Screen.Album.createRoute(it)) }
                )
            }
            
            // Search Screen (handled by pager, but kept for direct navigation if needed)
            composable(Screen.Search.route) {
                LaunchedEffect(Unit) {
                    navController.navigate("main_pager/2") {
                        popUpTo(Screen.Search.route) { inclusive = true }
                    }
                }
            }
            
            // Equalizer Screen
            composable(Screen.Equalizer.route) {
                EqualizerScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            // Settings Screen (handled by pager)
            composable(Screen.Settings.route) {
                LaunchedEffect(Unit) {
                    navController.navigate("main_pager/3") {
                        popUpTo(Screen.Settings.route) { inclusive = true }
                    }
                }
            }
            
            // Lyrics Screen
            composable(Screen.Lyrics.route) {
                LyricsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            // Sleep Timer Screen
            composable(Screen.SleepTimer.route) {
                SleepTimerScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            // Queue Screen
            composable(Screen.Queue.route) {
                QueueScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            // Playlist Screen
            composable(
                route = Screen.Playlist.route,
                arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
            ) { backStackEntry ->
                PlaylistScreen(
                    playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L,
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            // Album Screen
            composable(
                route = Screen.Album.route,
                arguments = listOf(navArgument("albumId") { type = NavType.LongType })
            ) { backStackEntry ->
                AlbumScreen(
                    albumId = backStackEntry.arguments?.getLong("albumId") ?: 0L,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToArtist = { navController.navigate(Screen.Artist.createRoute(it)) }
                )
            }
            
            // Artist Screen
            composable(
                route = Screen.Artist.route,
                arguments = listOf(navArgument("artistId") { type = NavType.LongType })
            ) { backStackEntry ->
                ArtistScreen(
                    artistId = backStackEntry.arguments?.getLong("artistId") ?: 0L,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToAlbum = { navController.navigate(Screen.Album.createRoute(it)) }
                )
            }
            
            // Folder Screen
            composable(
                route = Screen.Folder.route,
                arguments = listOf(navArgument("folderPath") { type = NavType.StringType })
            ) { backStackEntry ->
                val encodedPath = backStackEntry.arguments?.getString("folderPath") ?: ""
                val folderPath = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.toString())
                FolderScreen(
                    folderPath = folderPath,
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            // Genre Screen
            composable(
                route = Screen.Genre.route,
                arguments = listOf(navArgument("genreName") { type = NavType.StringType })
            ) { backStackEntry ->
                val encodedName = backStackEntry.arguments?.getString("genreName") ?: ""
                val genreName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.toString())
                GenreScreen(
                    genreName = genreName,
                    onBackClick = { navController.popBackStack() }
                )
            }
            }
        }
        
        // Global Mini Player Overlay
        // Positional logic is handled inside MiniPlayer component
        if (showBottomBar) {
            MiniPlayer(
                onClick = { navController.navigate(Screen.Player.route) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}