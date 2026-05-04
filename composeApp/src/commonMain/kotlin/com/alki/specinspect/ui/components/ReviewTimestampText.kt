package com.alki.specinspect.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alki.specinspect.ui.theme.AppColors
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import specinspect.composeapp.generated.resources.Res
import specinspect.composeapp.generated.resources.last_reviewed_format
import kotlin.time.Instant

@Composable
fun ReviewTimestampText(
    timestamp: Long?,
    modifier: Modifier = Modifier,
) {
    if (timestamp == null) return
    Text(
        text = stringResource(Res.string.last_reviewed_format, formatReviewTimestamp(timestamp)),
        style = MaterialTheme.typography.bodySmall,
        color = AppColors.GreyViolet,
        modifier = modifier,
    )
}

private fun formatReviewTimestamp(timestamp: Long): String {
    val dateTime = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.date.day.twoDigits()}.${(dateTime.date.month.ordinal + 1).twoDigits()}.${dateTime.date.year} ${dateTime.hour.twoDigits()}:${dateTime.minute.twoDigits()}"
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')
