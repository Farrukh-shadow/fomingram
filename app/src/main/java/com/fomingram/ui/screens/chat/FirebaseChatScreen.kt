package com.fomingram.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fomingram.data.repository.FirebaseMessage
import com.fomingram.ui.components.AvatarCircle
import com.fomingram.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FirebaseChatScreen(
    chatId: String,
    contactName: String,
    onBack: () -> Unit,
    viewModel: FirebaseChatViewModel = viewModel(
        factory = FirebaseChatViewModel.factory(chatId)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val myName by viewModel.myName.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var selectedMessage by remember { mutableStateOf<FirebaseMessage?>(null) }
    var showNameDialog by remember { mutableStateOf(true) }

    // Скроллим вниз при новых сообщениях
    LaunchedEffect(uiState) {
        if (uiState is FirebaseChatUiState.Success) {
            val messages = (uiState as FirebaseChatUiState.Success).messages
            if (messages.isNotEmpty()) {
                scope.launch { listState.animateScrollToItem(messages.size - 1) }
            }
        }
    }

    // Диалог: введи своё имя при первом входе
    if (showNameDialog) {
        NameInputDialog(
            onConfirm = { name ->
                viewModel.setMyName(name)
                showNameDialog = false
            }
        )
    }

    // Диалог удаления сообщения
    selectedMessage?.let { message ->
        if (message.isFromMe) {
            AlertDialog(
                onDismissRequest = { selectedMessage = null },
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        "Удалить сообщение?",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        message.text,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteMessage(message.id)
                        selectedMessage = null
                    }) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedMessage = null }) {
                        Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarCircle(name = contactName, size = 38.dp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                contactName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(OnlineGreen)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Firebase · В сети",
                                    fontSize = 12.sp,
                                    color = OnlineGreen
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Показываем своё имя
                    TextButton(onClick = { showNameDialog = true }) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = FomingramViolet,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(myName, color = FomingramViolet, fontSize = 13.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            FirebaseInputBar(
                text = inputText,
                onTextChange = viewModel::onInputChange,
                onSend = viewModel::sendMessage
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is FirebaseChatUiState.Loading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = FomingramViolet)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Подключение к Firebase…",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is FirebaseChatUiState.Error -> {
                    Text(
                        state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is FirebaseChatUiState.Success -> {
                    if (state.messages.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Чат создан!",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Поделись названием чата: $chatId",
                                    color = FomingramViolet,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "Другой пользователь войдёт в тот же чат",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(state.messages, key = { it.id }) { message ->
                                FirebaseMessageBubble(
                                    message = message,
                                    onLongPress = { selectedMessage = message }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FirebaseMessageBubble(
    message: FirebaseMessage,
    onLongPress: () -> Unit
) {
    val isMe = message.isFromMe
    val bubbleColor = if (isMe) BubbleMe else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
    val timeColor = if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val shape = if (isMe)
        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    else
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        // Имя отправителя (для чужих сообщений)
        if (!isMe) {
            Text(
                message.senderName,
                fontSize = 12.sp,
                color = FomingramViolet,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bubbleColor)
                .combinedClickable(onClick = {}, onLongClick = onLongPress)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = formatFirebaseTime(message.timestamp),
                    fontSize = 11.sp,
                    color = timeColor,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun FirebaseInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        "Написать сообщение…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )

            Spacer(Modifier.width(8.dp))

            val canSend = text.isNotBlank()
            FloatingActionButton(
                onClick = onSend,
                containerColor = if (canSend) FomingramViolet
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Отправить",
                    tint = if (canSend) Color.White
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun NameInputDialog(onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { },
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Как тебя зовут?",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                Text(
                    "Введи имя — его увидят другие участники чата",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Твоё имя") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                colors = ButtonDefaults.buttonColors(containerColor = FomingramViolet)
            ) {
                Text("Войти", color = Color.White)
            }
        }
    )
}

private fun formatFirebaseTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
