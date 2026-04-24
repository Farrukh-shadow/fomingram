package com.fomingram.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.fomingram.ui.screens.chat.ChatScreen
import com.fomingram.ui.screens.chatlist.ChatListScreen
import com.fomingram.ui.screens.profile.ProfileScreen
import com.fomingram.ui.screens.contacts.ContactsScreen
import com.fomingram.ui.screens.settings.SettingsScreen
import com.fomingram.ui.theme.DarkBackground
import com.fomingram.ui.theme.DarkSurface
import com.fomingram.ui.theme.FomingramViolet
import com.fomingram.ui.theme.TextSecondary

sealed class Screen(val route: String) {
    object ChatList : Screen("chat_list")
    object Contacts : Screen("contacts")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Chat : Screen("chat/{chatId}/{contactName}") {
        fun createRoute(chatId: String, contactName: String) =
            "chat/$chatId/${contactName}"
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun FomingramNavGraph() {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem("Чаты", Screen.ChatList.route, Icons.Filled.Chat, Icons.Outlined.Chat),
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

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DarkSurface,
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
                                indicatorColor = DarkSurface
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
                    }
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
                SettingsScreen()
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
        }
    }
}
