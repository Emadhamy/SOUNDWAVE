package com.soundwave.player.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.soundwave.player.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    var showThemeDialog by remember { mutableStateOf(false) }
    var showMiniPlayerDialog by remember { mutableStateOf(false) }
    var showAccentColorDialog by remember { mutableStateOf(false) }
    var showCrossfadeDialog by remember { mutableStateOf(false) }
    var showMinDurationDialog by remember { mutableStateOf(false) }
    var showFoldersDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Appearance Section
            item {
                SettingsSection(title = "المظهر")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "الوضع",
                    subtitle = when (state.themeMode) {
                        ThemeMode.LIGHT -> "فاتح"
                        ThemeMode.DARK -> "داكن"
                        ThemeMode.SYSTEM -> "حسب النظام"
                    },
                    onClick = { showThemeDialog = true }
                )
            }
            
            item {
                SettingsSwitch(
                    icon = Icons.Default.ColorLens,
                    title = "الألوان الديناميكية",
                    subtitle = "استخدام ألوان من صورة الألبوم",
                    checked = state.dynamicColors,
                    onCheckedChange = { viewModel.setDynamicColors(it) }
                )
            }
            
            if (!state.dynamicColors) {
                item {
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "لون السمة",
                        subtitle = "اختر لونك المفضل",
                        onClick = { showAccentColorDialog = true }
                    )
                }
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.ViewQuilt,
                    title = "نمط المشغل المصغر",
                    subtitle = when (state.miniPlayerStyle) {
                        MiniPlayerStyle.DOCKED -> "مثبت"
                        MiniPlayerStyle.FLOATING -> "عائم"
                    },
                    onClick = { showMiniPlayerDialog = true }
                )
            }
            
            // Playback Section
            item {
                SettingsSection(title = "التشغيل")
            }
            
            item {
                SettingsSwitch(
                    icon = Icons.Default.Tune,
                    title = "تشغيل بدون فجوات",
                    subtitle = "Gapless Playback",
                    checked = state.gaplessPlayback,
                    onCheckedChange = { viewModel.setGaplessPlayback(it) }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Compare,
                    title = "التلاشي المتقاطع",
                    subtitle = if (state.crossfadeDuration > 0) "${state.crossfadeDuration} ثانية" else "معطّل",
                    onClick = { showCrossfadeDialog = true }
                )
            }
            
            item {
                SettingsSwitch(
                    icon = Icons.Default.VolumeUp,
                    title = "توحيد مستوى الصوت",
                    subtitle = "ReplayGain",
                    checked = state.replayGain,
                    onCheckedChange = { viewModel.setReplayGain(it) }
                )
            }
            
            // Library Section
            item {
                SettingsSection(title = "المكتبة")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Refresh,
                    title = "مسح المكتبة",
                    subtitle = if (state.isScanning) "جاري المسح..." else "${state.songCount} أغنية",
                    onClick = { viewModel.scanLibrary() },
                    enabled = !state.isScanning
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Folder,
                    title = "إدارة المجلدات",
                    subtitle = "اختيار المجلدات المراد استبعادها",
                    onClick = { showFoldersDialog = true }
                )
            }
            
            item {
                SettingsSwitch(
                    icon = Icons.Default.FilterAlt,
                    title = "إخفاء الأغاني القصيرة",
                    subtitle = "أقل من ${state.minSongDuration} ثانية",
                    checked = state.filterShortSongs,
                    onCheckedChange = { viewModel.setFilterShortSongs(it) }
                )
            }
            
            if (state.filterShortSongs) {
                item {
                    SettingsItem(
                        icon = Icons.Default.Timer,
                        title = "مدة الفلترة",
                        subtitle = "${state.minSongDuration} ثانية",
                        onClick = { showMinDurationDialog = true }
                    )
                }
            }
            
            // Display Section
            item {
                SettingsSection(title = "العرض")
            }
            
            item {
                SettingsSwitch(
                    icon = Icons.Default.Screenshot,
                    title = "إبقاء الشاشة مفتوحة",
                    subtitle = "أثناء التشغيل",
                    checked = state.keepScreenOn,
                    onCheckedChange = { viewModel.setKeepScreenOn(it) }
                )
            }
            
            // About Section
            item {
                SettingsSection(title = "حول")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "الإصدار",
                    subtitle = "1.0.0",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Code,
                    title = "المطور",
                    subtitle = "SoundWave Team",
                    onClick = { }
                )
            }
        }
    }
    
    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("اختر الوضع") },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = state.themeMode == mode,
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                when (mode) {
                                    ThemeMode.LIGHT -> "فاتح"
                                    ThemeMode.DARK -> "داكن"
                                    ThemeMode.SYSTEM -> "حسب النظام"
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = { }
        )
    }
    
    // Mini Player Style Dialog
    if (showMiniPlayerDialog) {
        AlertDialog(
            onDismissRequest = { showMiniPlayerDialog = false },
            title = { Text("نمط المشغل المصغر") },
            text = {
                Column {
                    MiniPlayerStyle.entries.forEach { style ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setMiniPlayerStyle(style)
                                    showMiniPlayerDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.miniPlayerStyle == style,
                                onClick = {
                                    viewModel.setMiniPlayerStyle(style)
                                    showMiniPlayerDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                when (style) {
                                    MiniPlayerStyle.DOCKED -> "مثبت (كلاسيكي)"
                                    MiniPlayerStyle.FLOATING -> "عائم (حديث)"
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = { }
        )
    }
    
    // Accent Color Dialog
    if (showAccentColorDialog) {
        val accentColors = listOf(
            null to "الافتراضي",
            AccentPurple to "بنفسجي",
            AccentCyan to "سماوي",
            AccentPink to "وردي",
            AccentGreen to "أخضر",
            AccentOrange to "برتقالي",
            AccentRed to "أحمر",
            AccentBlue to "أزرق",
            AccentYellow to "أصفر"
        )
        
        AlertDialog(
            onDismissRequest = { showAccentColorDialog = false },
            title = { Text("لون السمة") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(accentColors) { (color, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAccentColor(color?.toArgb())
                                    showAccentColorDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(4.dp)
                                    .background(color ?: MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(name)
                            Spacer(modifier = Modifier.weight(1f))
                            RadioButton(
                                selected = if (color == null) state.accentColor == null
                                          else state.accentColor == color.toArgb(),
                                onClick = {
                                    viewModel.setAccentColor(color?.toArgb())
                                    showAccentColorDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = { }
        )
    }
    
    // Crossfade Dialog
    if (showCrossfadeDialog) {
        var sliderValue by remember { mutableFloatStateOf(state.crossfadeDuration.toFloat()) }
        
        AlertDialog(
            onDismissRequest = { showCrossfadeDialog = false },
            title = { Text("التلاشي المتقاطع") },
            text = {
                Column {
                    Text(
                        text = if (sliderValue > 0) "${sliderValue.toInt()} ثانية" else "معطّل",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 0f..12f,
                        steps = 11
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setCrossfadeDuration(sliderValue.toInt())
                    showCrossfadeDialog = false
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCrossfadeDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Min Duration Dialog
    if (showMinDurationDialog) {
        AlertDialog(
            onDismissRequest = { showMinDurationDialog = false },
            title = { Text("مدة الفلترة") },
            text = {
                Column {
                    listOf(30, 50, 60).forEach { duration ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setMinSongDuration(duration)
                                    showMinDurationDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.minSongDuration == duration,
                                onClick = {
                                    viewModel.setMinSongDuration(duration)
                                    showMinDurationDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$duration ثانية")
                        }
                    }
                }
            },
            confirmButton = { }
        )
    }
    // Folders Selection Dialog
    if (showFoldersDialog) {
        AlertDialog(
            onDismissRequest = { showFoldersDialog = false },
            title = { Text("إدارة المجلدات") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(state.folders) { folderPath ->
                        val isExcluded = state.excludedFolders.contains(folderPath)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleExcludedFolder(folderPath) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = !isExcluded,
                                onCheckedChange = { viewModel.toggleExcludedFolder(folderPath) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = folderPath.substringAfterLast("/"),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFoldersDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 72.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick)
    )
}

@Composable
private fun SettingsSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}