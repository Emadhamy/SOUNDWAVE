package com.soundwave.player.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Library : Screen("library")
    object Player : Screen("player")
    object Search : Screen("search")
    object Equalizer : Screen("equalizer")
    object Settings : Screen("settings")
    object Lyrics : Screen("lyrics")
    object SleepTimer : Screen("sleep_timer")
    object Queue : Screen("queue")
    
    object Playlist : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
    
    object Album : Screen("album/{albumId}") {
        fun createRoute(albumId: Long) = "album/$albumId"
    }
    
    object Artist : Screen("artist/{artistId}") {
        fun createRoute(artistId: Long) = "artist/$artistId"
    }
    
    object Folder : Screen("folder/{folderPath}") {
        fun createRoute(folderPath: String) = "folder/${folderPath.encodeUrl()}"
    }
    
    object Genre : Screen("genre/{genreName}") {
        fun createRoute(genreName: String) = "genre/${genreName.encodeUrl()}"
    }
}

private fun String.encodeUrl(): String = 
    java.net.URLEncoder.encode(this, "UTF-8")

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Home : BottomNavItem(
        route = Screen.Home.route,
        title = "الرئيسية",
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home
    )
    
    object Library : BottomNavItem(
        route = Screen.Library.route,
        title = "المكتبة",
        icon = Icons.Outlined.LibraryMusic,
        selectedIcon = Icons.Filled.LibraryMusic
    )
    
    object Search : BottomNavItem(
        route = Screen.Search.route,
        title = "بحث",
        icon = Icons.Outlined.Search,
        selectedIcon = Icons.Filled.Search
    )
    
    object Settings : BottomNavItem(
        route = Screen.Settings.route,
        title = "الإعدادات",
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings
    )
    
    companion object {
        val items = listOf(Home, Library, Search, Settings)
    }
}