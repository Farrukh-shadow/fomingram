package com.fomingram.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.fomingram.ui.screens.chat.ChatScreen
import com.fomingram.ui.screens.chat.FirebaseChatScreen
import com.fomingram.ui.screens.chatlist.ChatListScreen
import com.fomingram.ui.screens.contacts.ContactsScreen
import com.fomingram.ui.screens.profile.ProfileScreen
import com.fomingram.ui.screens.settings.SettingsScreen
import com.fomingram.ui.theme.FomingramViolet
import com.fomingram.ui.theme.TextSecondary

sealed class Screen(val route: String) {
    object ChatList : Screen("chat_list")
    object Contacts : Screen("contacts")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Chat : Screen("chat/{chatId}/{contactName}") {
        fun createRoute(chatId: String, contactName: String) =
            "chat/$chatId/$contactName"
    }
    object FirebaseChat : Screen("firebase_chat/{chatId}/{contactName}") {
        fun createRoute(chatId: String, contactName: String) =
            "firebase_chat/$chatId/$contactName"
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun FomingramNavGraph(
    isDarkTheme: Boolean = true,
    onThemeChange: (Boolean) -> Unit = {}
) {
    val navController = rememberNavController()
    var showFirebaseDialog by remember { mutableStateOf(false) }

    val bottomNavItems = listOf(
        BottomNavItem("Чаты", Screen.ChatList.route, Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat),
        BottomNavItem("Контакты", Screen.Contacts.route, Icons.Filled.Contacts, Icons.Outlined.Contacts),
        BottomNavItem("Профиль", Screen.Profile.route, Icons.Filled.Person, Icons.Outlined.Person),
        BottomNavItem("Настройки", Screen.Settings.route, Icons.Filled.Settings, Icons.Outlined.Settings),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.ChatList.route,
        Screen.Contacts.route,
        Screen.Profile.route,
        Screen.Settings.route
    )

    if (showFirebaseDialog) {
        FirebaseChatDialog(
            onDismiss = { showFirebaseDialog = false },
            onConfirm = { chatId, chatName ->
                showFirebaseDialog = false
                navController.navigate(Screen.FirebaseChat.createRoute(chatId, chatName))
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = FomingramViolet,
                                selectedTextColor = FomingramViolet,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.ChatList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.ChatList.route) {
                ChatListScreen(
                    onChatClick = { chatId, contactName ->
                        navController.navigate(Screen.Chat.createRoute(chatId, contactName))
                    },
                    onFirebaseChatClick = { showFirebaseDialog = true }
                )
            }
            composable(Screen.Contacts.route) {
                ContactsScreen(
                    onContactClick = { chatId, contactName ->
                        navController.navigate(Screen.Chat.createRoute(chatId, contactName))
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange
                )
            }
            composable(
                route = Screen.Chat.route,
                arguments = listOf(
                    navArgument("chatId") { type = NavType.StringType },
                    navArgument("contactName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                val contactName = backStackEntry.arguments?.getString("contactName") ?: ""
                ChatScreen(
                    chatId = chatId,
                    contactName = contactName,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.FirebaseChat.route,
                arguments = listOf(
                    navArgument("chatId") { type = NavType.StringType },
                    navArgument("contactName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                val contactName = backStackEntry.arguments?.getString("contactName") ?: ""
                FirebaseChatScreen(
                    chatId = chatId,
                    contactName = contactName,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun FirebaseChatDialog(
    onDismiss: () -> Unit,
    onConfirm: (chatId: String, chatName: String) -> Unit
) {
    var chatId by remember { mutableStateOf("") }
    var chatName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Firebase чат",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Введи одинаковый ID чата на обоих устройствах чтобы общаться",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = chatId,
                    onValueChange = {
                        chatId = it
                        showError = false
                    },
                    label = { Text("ID чата (например: room123)") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Cloud,
                            contentDescription = null,
                            tint = FomingramViolet,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    isError = showError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FomingramViolet,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = FomingramViolet,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = chatName,
                    onValueChange = { chatName = it },
                    label = { Text("Название (необязательно)") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = FomingramViolet,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FomingramViolet,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = FomingramViolet,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (showError) {
                    Text(
                        "Введи ID чата",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (chatId.isBlank()) showError = true
                    else onConfirm(chatId.trim(), chatName.ifBlank { chatId.trim() })
                },
                colors = ButtonDefaults.buttonColors(containerColor = FomingramViolet)
            ) {
                Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Войти в чат", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
