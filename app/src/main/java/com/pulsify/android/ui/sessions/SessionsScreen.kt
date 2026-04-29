package com.pulsify.android.ui.sessions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulsify.android.data.local.ActivitySessionEntity
import com.pulsify.android.data.local.ContextMusicRuleEntity
import java.text.DateFormat
import java.util.Date

@Composable
fun SessionsScreen(
    viewModel: SessionsViewModel,
    modifier: Modifier = Modifier,
) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val rules by viewModel.rules.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "stats") {
            StatsBar(
                totalSessions = sessions.size,
                totalRules = rules.size,
            )
        }

        item(key = "sessions_header") {
            SectionHeader(
                icon = Icons.Default.History,
                title = "Recent mixes",
                count = sessions.size,
            )
        }

        if (sessions.isEmpty()) {
            item(key = "sessions_empty") {
                EmptyCard(
                    title = "No sessions yet",
                    body = "Tap Generate from Home to create your first contextual mix.",
                )
            }
        } else {
            items(sessions, key = { "session_${it.id}" }) { session ->
                SessionCard(session)
            }
        }

        item(key = "rules_header") {
            SectionHeader(
                icon = Icons.Default.Insights,
                title = "Learned preferences",
                count = rules.size,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "Pulsify remembers what music fits each activity so suggestions improve over time.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (rules.isEmpty()) {
            item(key = "rules_empty") {
                EmptyCard(
                    title = "Building your taste profile",
                    body = "Generate a few mixes and your activity-tuned preferences will appear here.",
                )
            }
        } else {
            items(rules, key = { "rule_${it.id}" }) { rule ->
                RuleCard(rule)
            }
        }
    }
}

@Composable
private fun StatsBar(totalSessions: Int, totalRules: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatTile(
            modifier = Modifier.weight(1f),
            label = "Mixes generated",
            value = totalSessions.toString(),
            tone = StatTone.Primary,
        )
        StatTile(
            modifier = Modifier.weight(1f),
            label = "Activities learned",
            value = totalRules.toString(),
            tone = StatTone.Secondary,
        )
    }
}

private enum class StatTone { Primary, Secondary }

@Composable
private fun StatTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    tone: StatTone,
) {
    val container = when (tone) {
        StatTone.Primary -> MaterialTheme.colorScheme.primaryContainer
        StatTone.Secondary -> MaterialTheme.colorScheme.secondaryContainer
    }
    val onContainer = when (tone) {
        StatTone.Primary -> MaterialTheme.colorScheme.onPrimaryContainer
        StatTone.Secondary -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = container,
            contentColor = onContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = onContainer.copy(alpha = 0.78f),
            )
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun SessionCard(session: ActivitySessionEntity) {
    val df = rememberDateFormat()
    val activityLabel = activityLabelFor(session.activityType)
    val tile = activityTile(session.activityType)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            shape = RoundedCornerShape(20.dp),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(tile.background),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    tile.icon,
                    contentDescription = null,
                    tint = tile.iconTint,
                    modifier = Modifier.size(26.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = activityLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = df.format(Date(session.timestampMillis)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                session.playlistSummary?.let {
                    Text(
                        text = it,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (session.latitude != null && session.longitude != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "${"%.4f".format(session.latitude)}, ${"%.4f".format(session.longitude)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleCard(rule: ContextMusicRuleEntity) {
    val activityLabel = activityLabelFor(rule.activityType)
    val tile = activityTile(rule.activityType)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(tile.background),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    tile.icon,
                    contentDescription = null,
                    tint = tile.iconTint,
                    modifier = Modifier.size(26.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = activityLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = rule.associationNote,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = "Used ${rule.useCount} ${if (rule.useCount == 1) "time" else "times"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun EmptyCard(title: String, body: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            shape = RoundedCornerShape(20.dp),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private data class ActivityTile(
    val icon: ImageVector,
    val background: Color,
    val iconTint: Color,
)

@Composable
private fun activityTile(activityType: String): ActivityTile {
    val scheme = MaterialTheme.colorScheme
    return when (activityType) {
        "Running" -> ActivityTile(
            icon = Icons.AutoMirrored.Filled.DirectionsRun,
            background = scheme.tertiaryContainer,
            iconTint = scheme.onTertiaryContainer,
        )
        "Walking" -> ActivityTile(
            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
            background = scheme.primaryContainer,
            iconTint = scheme.onPrimaryContainer,
        )
        "Sitting" -> ActivityTile(
            icon = Icons.Default.SelfImprovement,
            background = scheme.secondaryContainer,
            iconTint = scheme.onSecondaryContainer,
        )
        else -> ActivityTile(
            icon = Icons.Default.AutoAwesome,
            background = scheme.surfaceVariant,
            iconTint = scheme.onSurfaceVariant,
        )
    }
}

private fun activityLabelFor(activityType: String): String = when (activityType) {
    "Running" -> "Running"
    "Walking" -> "Walking"
    "Sitting" -> "Sitting / studying"
    else -> activityType
}

@Composable
private fun rememberDateFormat(): DateFormat {
    return remember {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    }
}
