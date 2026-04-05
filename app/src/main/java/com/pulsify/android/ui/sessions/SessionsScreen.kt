package com.pulsify.android.ui.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
                text = "Saved sessions",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Room persists lightweight session rows for grading and future personalization.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(sessions, key = { it.id }) { session ->
            SessionCard(session)
        }
        item {
            Text(
                text = "Learned associations (stub)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        items(rules, key = { it.id }) { rule ->
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
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(df.format(Date(session.timestampMillis)), style = MaterialTheme.typography.labelLarge)
            Text(session.activityType, style = MaterialTheme.typography.titleMedium)
            session.playlistSummary?.let {
                Text(it, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (session.latitude != null && session.longitude != null) {
                Text(
                    "Tagged @ ${"%.4f".format(session.latitude)}, ${"%.4f".format(session.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RuleCard(rule: ContextMusicRuleEntity) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(rule.activityType, style = MaterialTheme.typography.titleMedium)
            Text(rule.associationNote, style = MaterialTheme.typography.bodyMedium)
            Text("Uses recorded: ${rule.useCount}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun rememberDateFormat(): DateFormat {
    return remember {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    }
}
