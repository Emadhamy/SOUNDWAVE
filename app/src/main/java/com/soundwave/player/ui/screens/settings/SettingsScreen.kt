package com.soundwave.player.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCrossfadeDialog by remember { mutableStateOf(false) }
    
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
                SettingsSwitch(
                    icon = Icons.Default.FilterAlt,
                    title = "إخفاء الأغاني القصيرة",
                    subtitle = "أقل من 30 ثانية",
                    checked = state.filterShortSongs,
                    onCheckedChange = { viewModel.setFilterShortSongs(it) }
                )
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