package com.fomingram.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fomingram.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean = true,
    onThemeChange: (Boolean) -> Unit = {}
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Настройки", fontWeight = FontWeight.Bold, color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            SettingsSection(title = "Уведомления") {
                SwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Уведомления",
                    subtitle = "Получать push-уведомления",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))
                SwitchItem(
                    icon = Icons.Default.VolumeUp,
                    title = "Звук",
                    subtitle = "Звуки уведомлений",
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it }
                )
            }

            Spacer(Modifier.height(16.dp))
            SwitchItem(
                icon = Icons.Default.DarkMode,
                title = "Тёмная тема",
                subtitle = "Используется тёмное оформление",
                checked = isDarkTheme,
                onCheckedChange = onThemeChange
            )

            Spacer(Modifier.height(16.dp))

            SettingsSection(title = "Данные") {
                SettingsItem(Icons.Default.Storage, "Очистить кэш", "Освободить место") { }
                HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))
                SettingsItem(Icons.Default.CloudDownload, "Резервная копия", "Сохранить данные") { }
            }

            Spacer(Modifier.height(16.dp))

            SettingsSection(title = "О приложении") {
                SettingsItem(Icons.Default.Info, "Версия", "Fomingram 1.0.0") { }
                HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))
                SettingsItem(Icons.Default.Code, "Разработчик", "Razzokov Farrukh") { }
                HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))
                SettingsItem(Icons.Default.Shield, "Политика конфиденциальности", "") { }
            }

            Spacer(Modifier.height(24.dp))

            // Version watermark
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Fomingram v1.0.0 · MVVM + Room + Retrofit", fontSize = 12.sp, color = TextHint)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            title,
            fontSize = 13.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun SwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = FomingramViolet, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 15.sp)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, color = TextSecondary, fontSize = 13.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = FomingramViolet,
                checkedTrackColor = FomingramViolet.copy(alpha = 0.4f)
            )
        )
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = FomingramViolet, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 15.sp)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, color = TextSecondary, fontSize = 13.sp)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextHint)
    }
}
