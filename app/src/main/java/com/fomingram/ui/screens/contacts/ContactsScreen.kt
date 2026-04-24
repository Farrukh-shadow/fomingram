package com.fomingram.ui.screens.contacts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fomingram.ui.components.AvatarCircle
import com.fomingram.ui.screens.chatlist.ChatListUiState
import com.fomingram.ui.screens.chatlist.ChatListViewModel
import com.fomingram.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onContactClick: (chatId: String, contactName: String) -> Unit,
    viewModel: ChatListViewModel = viewModel(factory = ChatListViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Контакты", fontWeight = FontWeight.Bold, color = TextPrimary) },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Добавить", tint = FomingramViolet)
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
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Поиск контактов…", color = TextHint) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            when (val state = uiState) {
                is ChatListUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FomingramViolet)
                    }
                }
                is ChatListUiState.Empty -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Нет контактов", color = TextSecondary)
                    }
                }
                is ChatListUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is ChatListUiState.Success -> {
                    val sorted = state.chats.sortedBy { it.name }
                    val grouped = sorted.groupBy { it.name.first().uppercaseChar() }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        grouped.forEach { (letter, contacts) ->
                            item {
                                Text(
                                    letter.toString(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FomingramViolet,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                            items(contacts, key = { it.id }) { contact ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onContactClick(contact.id, contact.name) }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AvatarCircle(name = contact.name, size = 46.dp)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(contact.name, fontWeight = FontWeight.Medium, color = TextPrimary, fontSize = 16.sp)
                                        if (contact.phone.isNotEmpty()) {
                                            Text(contact.phone, color = TextSecondary, fontSize = 13.sp)
                                        } else {
                                            Text(
                                                if (contact.isOnline) "В сети" else "Не в сети",
                                                color = if (contact.isOnline) OnlineGreen else TextHint,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(start = 74.dp), color = Divider, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}
