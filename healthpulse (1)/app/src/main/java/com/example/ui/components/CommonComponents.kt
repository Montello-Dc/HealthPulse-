package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.UserProfile
import com.example.data.local.entity.VitalRecord
import com.example.data.model.AlertSeverity
import com.example.data.model.VitalType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SeverityBadge(severity: AlertSeverity, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (severity) {
        AlertSeverity.NORMAL -> Triple(AlertNormalContainer, AlertNormalGreen, Icons.Default.CheckCircle)
        AlertSeverity.ELEVATED -> Triple(AlertElevatedContainer, AlertElevatedYellow, Icons.Default.Info)
        AlertSeverity.WARNING -> Triple(AlertWarningContainer, AlertWarningOrange, Icons.Default.Warning)
        AlertSeverity.CRITICAL -> Triple(AlertCriticalContainer, AlertCriticalRed, Icons.Default.Error)
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = severity.levelName.uppercase(),
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
fun VitalCard(
    record: VitalRecord,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val (icon, tintColor) = when (record.type) {
        VitalType.HEART_RATE -> Pair(Icons.Default.Favorite, GeoPrimary)
        VitalType.BLOOD_PRESSURE -> Pair(Icons.Default.Speed, Color(0xFF0061A4))
        VitalType.BLOOD_OXYGEN -> Pair(Icons.Default.Air, Color(0xFF0284C7))
        VitalType.TEMPERATURE -> Pair(Icons.Default.DeviceThermostat, Color(0xFFEA580C))
        VitalType.RESPIRATION -> Pair(Icons.Default.Waves, Color(0xFF0D9488))
        VitalType.GLUCOSE -> Pair(Icons.Default.WaterDrop, Color(0xFF7C3AED))
    }

    val displayValue = if (record.valueSecondary != null) {
        "${record.valuePrimary.toInt()}/${record.valueSecondary.toInt()}"
    } else {
        if (record.valuePrimary % 1.0 == 0.0) "${record.valuePrimary.toInt()}" else "%.1f".format(record.valuePrimary)
    }

    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(record.timestamp))

    val isAlert = record.severity == AlertSeverity.CRITICAL || record.severity == AlertSeverity.WARNING

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("vital_record_${record.id}")
            .then(
                if (isAlert) Modifier.border(1.5.dp, AlertCriticalContainer, RoundedCornerShape(24.dp))
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isAlert) AlertCriticalContainer else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = record.type.displayName,
                            tint = if (isAlert) AlertCriticalRed else tintColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = record.type.displayName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isAlert) AlertCriticalRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$formattedTime • ${record.measurementMethod}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                SeverityBadge(severity = record.severity)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = displayValue,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAlert) AlertCriticalRed else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = record.unit,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            if (!record.alertMessage.isNullOrBlank()) {
                Surface(
                    color = when (record.severity) {
                        AlertSeverity.CRITICAL -> AlertCriticalContainer
                        AlertSeverity.WARNING -> AlertWarningContainer
                        else -> AlertElevatedContainer
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PriorityHigh,
                            contentDescription = null,
                            tint = when (record.severity) {
                                AlertSeverity.CRITICAL -> AlertCriticalRed
                                AlertSeverity.WARNING -> AlertWarningOrange
                                else -> AlertElevatedYellow
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = record.alertMessage,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PpgLiveWaveformCanvas(
    wavePoints: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = PulseWaveColor
) {
    Canvas(modifier = modifier) {
        if (wavePoints.size < 2) {
            drawLine(
                color = lineColor.copy(alpha = 0.35f),
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 3f
            )
            return@Canvas
        }

        val maxVal = wavePoints.maxOrNull() ?: 1f
        val minVal = wavePoints.minOrNull() ?: 0f
        val range = if (maxVal - minVal > 0.01f) maxVal - minVal else 1f

        val stepX = size.width / (wavePoints.size - 1).coerceAtLeast(1)
        val path = Path()

        for (i in wavePoints.indices) {
            val normalizedY = 1f - ((wavePoints[i] - minVal) / range)
            val y = normalizedY * (size.height * 0.7f) + (size.height * 0.15f)
            val x = i * stepX

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun TopUserHeader(
    user: UserProfile?,
    alertCount: Int,
    onAuthClick: () -> Unit,
    onAlertsClick: () -> Unit,
    modifier: Modifier = Modifier,
    screenTitle: String = "Overview"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .clickable { onAuthClick() }
                .testTag("user_profile_button")
        ) {
            Text(
                text = "HEALTHPULSE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
            Text(
                text = screenTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = GeoOnPrimaryContainer
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onAlertsClick,
                modifier = Modifier.testTag("top_alerts_button")
            ) {
                BadgedBox(
                    badge = {
                        if (alertCount > 0) {
                            Badge(
                                containerColor = AlertCriticalRed,
                                contentColor = Color.White
                            ) {
                                Text("$alertCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Alerts and Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Geometric Balanced Avatar
            val initials = if (user != null && user.name.isNotBlank()) {
                val parts = user.name.split(" ")
                if (parts.size >= 2) "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
                else user.name.take(2).uppercase()
            } else "JD"

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(GeoPrimaryContainer)
                    .border(2.dp, Color.White, CircleShape)
                    .clickable { onAuthClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = GeoOnPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

