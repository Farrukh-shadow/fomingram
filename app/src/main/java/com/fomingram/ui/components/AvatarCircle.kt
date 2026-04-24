package com.fomingram.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.fomingram.ui.theme.AvatarColors
import com.fomingram.ui.theme.TextPrimary

@Composable
fun AvatarCircle(
    name: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val initial = name.firstOrNull()?.uppercaseChar() ?: '?'
    val colorIndex = (name.hashCode() and 0x7FFFFFFF) % AvatarColors.size
    val bgColor = AvatarColors[colorIndex]
    val fontSize = (size.value * 0.42f).sp

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial.toString(),
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}
