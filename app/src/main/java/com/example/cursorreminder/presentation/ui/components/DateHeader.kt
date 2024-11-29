package com.example.cursorreminder.presentation.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.cursorreminder.R

@Composable
fun DateHeader(dateGroup: GroupDate, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = when (dateGroup) {
                GroupDate.Today -> MaterialTheme.colorScheme.primaryContainer
                GroupDate.Tomorrow -> MaterialTheme.colorScheme.secondaryContainer
                GroupDate.Future -> MaterialTheme.colorScheme.tertiaryContainer
                GroupDate.Past -> MaterialTheme.colorScheme.surfaceVariant
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Text(
                text = stringResource(
                    when (dateGroup) {
                        GroupDate.Today -> R.string.today
                        GroupDate.Tomorrow -> R.string.tomorrow
                        GroupDate.Future -> R.string.upcoming
                        GroupDate.Past -> R.string.past
                    }
                ),
                style = MaterialTheme.typography.titleMedium,
                color = when (dateGroup) {
                    GroupDate.Today -> MaterialTheme.colorScheme.onPrimaryContainer
                    GroupDate.Tomorrow -> MaterialTheme.colorScheme.onSecondaryContainer
                    GroupDate.Future -> MaterialTheme.colorScheme.onTertiaryContainer
                    GroupDate.Past -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

enum class GroupDate {
    Today, Tomorrow, Future, Past
}