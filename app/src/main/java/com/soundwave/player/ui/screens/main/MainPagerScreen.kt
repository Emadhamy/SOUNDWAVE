package com.soundwave.player.ui.screens.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.soundwave.player.ui.navigation.BottomNavItem
import com.soundwave.player.ui.screens.home.HomeScreen
import com.soundwave.player.ui.screens.library.LibraryScreen
import com.soundwave.player.ui.screens.search.SearchScreen
import com.soundwave.player.ui.screens.settings.SettingsScreen

@Composable
fun MainPagerScreen(
    initialPage: Int = 0,
    onNavigateToPlayer: () -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
    onNavigateToArtist: (Long) -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    onNavigateToFolder: (String) -> Unit,
    onNavigateToGenre: (String) -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onPageChanged: (Int) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 4 }
    )

    // Sync pager state with external selection (e.g. bottom bar)
    LaunchedEffect(initialPage) {
        if (pagerState.currentPage != initialPage) {
            pagerState.animateScrollToPage(initialPage)
        }
    }

    // Notify when page changes via swipe
    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        beyondViewportPageCount = 1
    ) { page ->
        when (page) {
            0 -> HomeScreen(
                onNavigateToPlayer = onNavigateToPlayer,
                onNavigateToAlbum = onNavigateToAlbum,
                onNavigateToArtist = onNavigateToArtist,
                onNavigateToPlaylist = onNavigateToPlaylist,
                onNavigateToLibrary = onNavigateToLibrary,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToSearch = onNavigateToSearch
            )
            1 -> LibraryScreen(
                onNavigateToAlbum = onNavigateToAlbum,
                onNavigateToArtist = onNavigateToArtist,
                onNavigateToPlaylist = onNavigateToPlaylist,
                onNavigateToFolder = onNavigateToFolder,
                onNavigateToGenre = onNavigateToGenre
            )
            2 -> SearchScreen(
                onNavigateToAlbum = onNavigateToAlbum,
                onNavigateToArtist = onNavigateToArtist
            )
            3 -> SettingsScreen()
        }
    }
}
