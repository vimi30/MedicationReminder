package com.example.cursorreminder.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.cursorreminder.R
import kotlinx.coroutines.delay

@Composable
fun <T> SwipeToDeleteContainer(
    item: T,
    onDelete: (T) -> Unit,
    animationDuration: Int = 500,
    content: @Composable (T) -> Unit,
) {
    var show by remember { mutableStateOf(true) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    show = false
                    true
                }

                else -> false
            }
        }
    )

    LaunchedEffect(show) {
        if (!show) {
            delay(animationDuration.toLong())
            onDelete(item)
        }
    }

    AnimatedVisibility(
        visible = show,
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = animationDuration),
            shrinkTowards = Alignment.Top
        )
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                DismissBackground(dismissState = dismissState)
            },
            content = { content(item) },
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true
        )
    }
}

@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState) {
    val direction = dismissState.dismissDirection
    val color = when (dismissState.targetValue) {
        SwipeToDismissBoxValue.Settled -> Color.Transparent
        SwipeToDismissBoxValue.StartToEnd -> Color.Transparent
        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
    }
    val icon = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> null
        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 20.dp),
        contentAlignment = when (direction) {
            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
            SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
            else -> Alignment.Center
        }
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = stringResource(R.string.content_description_delete),
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}