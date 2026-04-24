package com.fomingram.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.fomingram.ui.theme.TextHint
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TimeText(timestamp: Long) {
    val text = formatTimestamp(timestamp)
    Text(text = text, fontSize = 12.sp, color = TextHint)
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val date = Date(timestamp)
    return when {
        diff < 60_000 -> "сейчас"
        diff < 86_400_000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        diff < 172_800_000 -> "Вчера"
        diff < 604_800_000 -> SimpleDateFormat("EEE", Locale("ru")).format(date)
            .replaceFirstChar { it.uppercaseChar() }
        else -> SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(date)
    }
}
