package com.pulsify.android.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulsify.android.domain.ChatMessage
import com.pulsify.android.domain.DetectedActivity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activity = LocalContext.current as androidx.activity.ComponentActivity
    val windowSizeClass = calculateWindowSizeClass(activity)

    val detected by viewModel.detectedActivity.collectAsStateWithLifecycle()
    val loading by viewModel.isLoading.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val textMode by viewModel.textModePreferred.collectAsStateWithLifecycle()
    val draft by viewModel.userDraft.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val partialText by viewModel.partialText.collectAsStateWithLifecycle()

    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> locationGranted = granted }

    var audioGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        audioGranted = granted
        if (granted) viewModel.toggleListening()
    }

    val hero = @Composable {
        HeroPanel(
            activity = detected,
            loading = loading,
            isListening = isListening,
            partialText = partialText,
            onMicTap = {
                if (!audioGranted) {
                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                    viewModel.toggleListening()
                }
            },
            onSuggestPlaylist = {
                if (!locationGranted) {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                viewModel.requestPlaylist(onOpenPlaylist)
            },
        )
    }

    val conversation = @Composable {
        ConversationPanel(
            textMode = textMode,
            messages = messages,
            draft = draft,
            onDraftChange = viewModel::onUserDraftChange,
            onSend = { viewModel.sendUserMessage() },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Expanded -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(Modifier.weight(1f)) { hero() }
                Column(Modifier.weight(1f)) { conversation() }
            }

            else -> Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                hero()
                conversation()
            }
        }

        if (!locationGranted) {
            Text(
                text = "Tip · grant location to tag sessions with approximate place. Pulsify works fine without it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HeroPanel(
    activity: DetectedActivity,
    loading: Boolean,
    isListening: Boolean,
    partialText: String,
    onMicTap: () -> Unit,
    onSuggestPlaylist: () -> Unit,
) {
    val container = MaterialTheme.colorScheme.primary
    val onContainer = MaterialTheme.colorScheme.onPrimary
    val fadedOnContainer = onContainer.copy(alpha = 0.78f)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = container,
            contentColor = onContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ActivityChip(activity = activity, onContainer = onContainer)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Tuned for",
                    style = MaterialTheme.typography.labelMedium,
                    color = fadedOnContainer,
                )
                Text(
                    text = activity.heroHeadline(),
                    style = MaterialTheme.typography.displaySmall,
                    color = onContainer,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = activity.heroSubtitle(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = onContainer.copy(alpha = 0.85f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PulsingMicButton(isListening = isListening, onTap = onMicTap)
                Text(
                    text = when {
                        isListening && partialText.isNotBlank() -> "\u201C$partialText\u201D"
                        isListening -> "Listening…"
                        else -> "Tap the mic to ask for a vibe"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = onContainer,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
            }

            GenerateButton(loading = loading, onClick = onSuggestPlaylist)
        }
    }
}

@Composable
private fun ActivityChip(activity: DetectedActivity, onContainer: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = CircleShape,
        color = onContainer.copy(alpha = 0.16f),
        contentColor = onContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = activity.icon(),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "Live · ${activity.shortLabel()}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun PulsingMicButton(
    isListening: Boolean,
    onTap: () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "mic")
    val ringScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.35f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ringScale",
    )
    val ringAlpha by transition.animateFloat(
        initialValue = if (isListening) 0.4f else 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ringAlpha",
    )
    val tint by animateColorAsState(
        targetValue = if (isListening) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary,
        label = "micTint",
    )
    val ringColor = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = Modifier.size(72.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                        scaleX = ringScale
                        scaleY = ringScale
                        alpha = ringAlpha
                    }
                    .clip(CircleShape)
                    .background(ringColor),
            )
        }
        Surface(
            onClick = onTap,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .size(64.dp)
                .semantics {
                    contentDescription = if (isListening) "Stop listening" else "Start voice input"
                },
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = tint,
                )
            }
        }
    }
}

@Composable
private fun GenerateButton(
    loading: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = !loading,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.onPrimary,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(12.dp))
            } else {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = if (loading) "Generating mix…" else "Generate contextual mix",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ConversationPanel(
    textMode: Boolean,
    messages: List<ChatMessage>,
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Assistant",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (textMode) "Text mode" else "Voice mode",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }

        AnimatedVisibility(visible = textMode) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Type to Pulsify") },
                    singleLine = false,
                    minLines = 2,
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                Surface(
                    onClick = onSend,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = "Send message" },
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(20.dp),
                ),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(message = msg)
                }
            }
        }
        if (!textMode) {
            Text(
                text = "Enable text mode in Settings for chat-style input in quiet spaces.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val align = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val container = if (message.isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val onContainer = if (message.isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val shape = if (message.isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = align,
    ) {
        Surface(
            color = container,
            contentColor = onContainer,
            shape = shape,
            modifier = Modifier.padding(
                end = if (message.isUser) 0.dp else 36.dp,
                start = if (message.isUser) 36.dp else 0.dp,
            ),
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = if (message.isUser) TextAlign.End else TextAlign.Start,
            )
        }
    }
}

private fun DetectedActivity.heroHeadline(): String = when (this) {
    DetectedActivity.Running -> "your run"
    DetectedActivity.Walking -> "your walk"
    DetectedActivity.Sitting -> "deep focus"
    DetectedActivity.Unknown -> "the moment"
}

private fun DetectedActivity.heroSubtitle(): String = when (this) {
    DetectedActivity.Running -> "We'll surface high-energy tracks paced to your stride."
    DetectedActivity.Walking -> "Mid-tempo rhythms that match a relaxed pace."
    DetectedActivity.Sitting -> "Lo-fi and ambient picks for steady concentration."
    DetectedActivity.Unknown -> "Move around for a few seconds and we'll dial in your context."
}

private fun DetectedActivity.shortLabel(): String = when (this) {
    DetectedActivity.Running -> "Running"
    DetectedActivity.Walking -> "Walking"
    DetectedActivity.Sitting -> "Sitting"
    DetectedActivity.Unknown -> "Sensing"
}

private fun DetectedActivity.icon(): ImageVector = when (this) {
    DetectedActivity.Running -> Icons.AutoMirrored.Filled.DirectionsRun
    DetectedActivity.Walking -> Icons.AutoMirrored.Filled.DirectionsWalk
    DetectedActivity.Sitting -> Icons.Default.SelfImprovement
    DetectedActivity.Unknown -> Icons.Default.AutoAwesome
}
