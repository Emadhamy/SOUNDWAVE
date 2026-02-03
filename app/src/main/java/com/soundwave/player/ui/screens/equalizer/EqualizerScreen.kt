package com.soundwave.player.ui.screens.equalizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soundwave.player.domain.model.EqualizerPreset
import com.soundwave.player.domain.model.ReverbPreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    onBackClick: () -> Unit,
    viewModel: EqualizerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val bandFrequencies by viewModel.bandFrequencies.collectAsState()
    val numberOfBands by viewModel.numberOfBands.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("معادل الصوت") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetToDefault() }) {
                        Icon(Icons.Default.Refresh, "إعادة ضبط")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Enable/Disable Switch
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "تفعيل المعادل",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (state.isEnabled) "مفعّل" else "معطّل",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.isEnabled,
                        onCheckedChange = { viewModel.setEnabled(it) }
                    )
                }
            }
            
            // Presets
            Text(
                text = "الإعدادات المسبقة",
                style = MaterialTheme.typography.titleMedium
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(EqualizerPreset.entries) { preset ->
                    FilterChip(
                        selected = state.preset == preset,
                        onClick = { viewModel.setPreset(preset) },
                        label = { Text(preset.displayName) },
                        enabled = state.isEnabled
                    )
                }
            }
            
            // Equalizer Bands
            Text(
                text = "نطاقات التردد",
                style = MaterialTheme.typography.titleMedium
            )
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(200.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    state.bandLevels.take(numberOfBands).forEachIndexed { index, level ->
                        EqualizerBand(
                            frequency = bandFrequencies.getOrElse(index) { "" },
                            level = level,
                            enabled = state.isEnabled,
                            onLevelChange = { viewModel.setBandLevel(index, it) }
                        )
                    }
                }
            }
            
            // Bass Boost
            EffectSlider(
                title = "تعزيز الباس",
                value = state.bassBoost,
                maxValue = 1000,
                enabled = state.isEnabled,
                onValueChange = { viewModel.setBassBoost(it) }
            )
            
            // Virtualizer
            EffectSlider(
                title = "المؤثر ثلاثي الأبعاد",
                value = state.virtualizerStrength,
                maxValue = 1000,
                enabled = state.isEnabled,
                onValueChange = { viewModel.setVirtualizer(it) }
            )
            
            // Loudness
            EffectSlider(
                title = "تعزيز الصوت",
                value = state.loudnessGain,
                maxValue = 10,
                enabled = state.isEnabled,
                onValueChange = { viewModel.setLoudnessGain(it) }
            )
            
            // Reverb
            Text(
                text = "الصدى",
                style = MaterialTheme.typography.titleMedium
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ReverbPreset.entries) { preset ->
                    FilterChip(
                        selected = state.reverbPreset == preset,
                        onClick = { viewModel.setReverb(preset) },
                        label = { Text(preset.displayName) },
                        enabled = state.isEnabled
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EqualizerBand(
    frequency: String,
    level: Int,
    enabled: Boolean,
    onLevelChange: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(40.dp)
    ) {
        Text(
            text = "+12",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Slider(
            value = level.toFloat(),
            onValueChange = { onLevelChange(it.toInt()) },
            valueRange = -12f..12f,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .graphicsLayer { rotationZ = 270f }
                .width(150.dp),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Text(
            text = "-12",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = frequency,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EffectSlider(
    title: String,
    value: Int,
    maxValue: Int,
    enabled: Boolean,
    onValueChange: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${(value.toFloat() / maxValue * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 0f..maxValue.toFloat(),
                enabled = enabled
            )
        }
    }
}