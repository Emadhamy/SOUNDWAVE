package com.soundwave.player.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// Custom shapes
val BottomSheetShape = RoundedCornerShape(
    topStart = 32.dp,
    topEnd = 32.dp
)

val AlbumArtShape = RoundedCornerShape(24.dp)

val CircleShape = RoundedCornerShape(percent = 50)

val ChipShape = RoundedCornerShape(24.dp)

val CardShape = RoundedCornerShape(24.dp)

val ButtonShape = RoundedCornerShape(16.dp)