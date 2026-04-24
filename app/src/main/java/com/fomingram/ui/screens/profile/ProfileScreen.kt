package com.fomingram.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fomingram.ui.components.AvatarCircle
import com.fomingram.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Профиль", fontWeight = FontWeight.Bold, color = TextPrimary) },
                actions = {
                    IconButton(onClick = { viewModel.loadUserProfile() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить", tint = FomingramViolet)
                    }
                },
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
            when (val state = uiState) {
                is ProfileUiState.Fetching -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = FomingramViolet)
                            Spacer(Modifier.height(16.dp))
                            Text("Загрузка профиля…", color = TextSecondary)
                        }
                    }
                }

                is ProfileUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CloudOff,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(state.message, color = MaterialTheme.colorScheme.error, fontSize = 16.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Нет подключения к интернету", color = TextSecondary, fontSize = 13.sp)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadUserProfile() },
                                colors = ButtonDefaults.buttonColors(containerColor = FomingramViolet)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Повторить")
                            }
                        }
                    }
                }

                is ProfileUiState.Success -> {
                    val user = state.user

                    // Profile header with gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(DarkSurface, DarkBackground)
                                )
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box {
                                AsyncImage(
                                    model = user.picture.large,
                                    contentDescription = "Аватар",
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .border(
                                            2.dp,
                                            brush = Brush.linearGradient(
                                                colors = listOf(FomingramGradientStart, FomingramGradientEnd)
                                            ),
                                            CircleShape
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(DarkBackground)
                                        .padding(3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(OnlineGreen)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "${user.name.first} ${user.name.last}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("@${user.login.username}", color = FomingramViolet, fontSize = 14.sp)
                            Spacer(Modifier.height(8.dp))
                            // API Status badge
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF1A3A1A))
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(OnlineGreen)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(state.apiStatus, color = OnlineGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Info cards
                    ProfileInfoCard(
                        items = listOf(
                            Triple(Icons.Default.Phone, "Телефон", user.phone),
                            Triple(Icons.Default.Email, "Email", user.email),
                            Triple(Icons.Default.LocationOn, "Город", "${user.location.city}, ${user.location.country}")
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    // App info card
                    ProfileSection(title = "О приложении") {
                        InfoRow(Icons.Default.Info, "Версия", "1.0.0")
                        InfoRow(Icons.Default.Storage, "База данных", "Room SQLite")
                        InfoRow(Icons.Default.CloudDone, "API", "RandomUser API")
                        InfoRow(Icons.Default.Architecture, "Архитектура", "MVVM + Clean")
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(items: List<Triple<ImageVector, String, String>>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            items.forEachIndexed { idx, (icon, label, value) ->
                InfoRow(icon, label, value)
                if (idx < items.lastIndex) {
                    HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
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
            Column(modifier = Modifier.padding(4.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = FomingramViolet, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = TextSecondary)
            Text(value, fontSize = 15.sp, color = TextPrimary)
        }
    }
}
