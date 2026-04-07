package com.pulsify.android.ui.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Session history",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Each generated mix is saved locally so Pulsify can learn your preferences over time.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(sessions, key = { "session_${it.id}" }) { session ->
            SessionCard(session)
        }
        item(key = "rules_header") {
            Text(
                text = "Learned preferences",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "Pulsify remembers what music fits each activity so suggestions improve over time.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(rules, key = { "rule_${it.id}" }) { rule ->
            RuleCard(rule)
        }
        if (sessions.isEmpty()) {
            item {
                Text(
                    text = "No sessions yet—generate a mix from Home.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun SessionCard(session: ActivitySessionEntity) {
    val df = rememberDateFormat()
    val activityLabel = when (session.activityType) {
        "Running" -> "Running"
        "Walking" -> "Walking"
        "Sitting" -> "Sitting / studying"
        else -> session.activityType
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(activityLabel, style = MaterialTheme.typography.titleMedium)
                Text(
                    df.format(Date(session.timestampMillis)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            session.playlistSummary?.let {
                Text(it, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
            }
            if (session.latitude != null && session.longitude != null) {
                Text(
                    "Location: ${"%.4f".format(session.latitude)}, ${"%.4f".format(session.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RuleCard(rule: ContextMusicRuleEntity) {
    val activityEmoji = when (rule.activityType) {
        "Running" -> "Running"
        "Walking" -> "Walking"
        "Sitting" -> "Sitting / studying"
        else -> rule.activityType
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(activityEmoji, style = MaterialTheme.typography.titleMedium)
            Text(rule.associationNote, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Used ${rule.useCount} ${if (rule.useCount == 1) "time" else "times"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun rememberDateFormat(): DateFormat {
    return remember {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    }
}
