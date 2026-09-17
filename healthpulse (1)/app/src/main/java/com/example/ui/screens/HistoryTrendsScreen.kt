package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.VitalRecord
import com.example.data.model.AlertSeverity
import com.example.data.model.VitalType
import com.example.ui.components.VitalCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.HealthViewModel
import java.util.concurrent.TimeUnit

@Composable
fun HistoryTrendsScreen(
    viewModel: HealthViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allVitals by viewModel.allVitals.collectAsState()

    var selectedTypeFilter by remember { mutableStateOf<VitalType?>(null) }
    var selectedTimeRange by remember { mutableStateOf("All") } // "Day", "Week", "Month", "All"

    val now = System.currentTimeMillis()
    val filteredVitals = remember(allVitals, selectedTypeFilter, selectedTimeRange) {
        allVitals.filter { record ->
            val typeMatches = (selectedTypeFilter == null || record.type == selectedTypeFilter)
            val timeMatches = when (selectedTimeRange) {
                "Day" -> now - record.timestamp <= TimeUnit.DAYS.toMillis(1)
                "Week" -> now - record.timestamp <= TimeUnit.DAYS.toMillis(7)
                "Month" -> now - record.timestamp <= TimeUnit.DAYS.toMillis(30)
                else -> true
            }
            typeMatches && timeMatches
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 90.dp)
            .testTag("history_trends_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "HEALTHPULSE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "History & Trends",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeoOnPrimaryContainer
                    )
                }
                if (allVitals.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearAllVitals() }) {
                        Text("Reset Log", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // Time Range Filter Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Day", "Week", "Month").forEach { range ->
                    FilterChip(
                        selected = selectedTimeRange == range,
                        onClick = { selectedTimeRange = range },
                        label = { Text(range, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GeoPrimaryContainer,
                            selectedLabelColor = GeoOnPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Vital Type Filter Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null },
                        label = { Text("All Types") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GeoPrimaryContainer,
                            selectedLabelColor = GeoOnPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                items(VitalType.values()) { type ->
                    FilterChip(
                        selected = selectedTypeFilter == type,
                        onClick = { selectedTypeFilter = type },
                        label = { Text(type.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GeoPrimaryContainer,
                            selectedLabelColor = GeoOnPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Interactive Trend Line Chart
        item {
            val chartData = remember(filteredVitals, selectedTypeFilter) {
                val targetType = selectedTypeFilter ?: VitalType.HEART_RATE
                filteredVitals.filter { it.type == targetType }.sortedBy { it.timestamp }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val activeType = selectedTypeFilter ?: VitalType.HEART_RATE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${activeType.displayName.uppercase()} TREND",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${chartData.size} Data Point(s)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = GeoOnPrimaryContainer
                            )
                        }

                        if (chartData.isNotEmpty()) {
                            val avg = chartData.map { it.valuePrimary }.average()
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Avg: ${avg.toInt()} ${activeType.unit}",
                                    fontWeight = FontWeight.Bold,
                                    color = GeoPrimary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                val max = chartData.maxOf { it.valuePrimary }.toInt()
                                val min = chartData.minOf { it.valuePrimary }.toInt()
                                Text(
                                    text = "Min: $min • Max: $max",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (chartData.size >= 2) {
                        VitalTrendChartCanvas(
                            records = chartData,
                            lineColor = GeoPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(12.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Log at least 2 readings to generate trend graphs",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Summary Stats Row
        if (filteredVitals.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val abnormalCount = filteredVitals.count { it.severity != AlertSeverity.NORMAL }

                    StatCard(
                        title = "TOTAL LOGS",
                        value = "${filteredVitals.size}",
                        icon = Icons.Default.Assessment,
                        color = GeoPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "ABNORMAL ALERTS",
                        value = "$abnormalCount",
                        icon = Icons.Default.Warning,
                        color = if (abnormalCount > 0) AlertWarningOrange else AlertNormalGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Historical Records List
        item {
            Text(
                text = "HISTORICAL RECORDS (${filteredVitals.size})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (filteredVitals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No vital records match your filter criteria.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredVitals) { record ->
                VitalCard(
                    record = record,
                    onClick = { viewModel.deleteVitalRecord(record) }
                )
            }
        }
    }
}

@Composable
fun VitalTrendChartCanvas(
    records: List<VitalRecord>,
    modifier: Modifier = Modifier,
    lineColor: Color = GeoPrimary
) {
    Canvas(modifier = modifier) {
        if (records.size < 2) return@Canvas

        val values = records.map { it.valuePrimary.toFloat() }
        val maxVal = (values.maxOrNull() ?: 100f).coerceAtLeast(10f)
        val minVal = (values.minOrNull() ?: 0f).coerceAtLeast(0f)
        val range = if (maxVal - minVal > 0.01f) maxVal - minVal else 1f

        val stepX = size.width / (records.size - 1)

        val path = Path()
        val fillPath = Path()

        for (i in records.indices) {
            val normalizedY = 1f - ((values[i] - minVal) / range)
            val y = normalizedY * (size.height * 0.7f) + (size.height * 0.15f)
            val x = i * stepX

            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, size.height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            // Draw data point circles
            drawCircle(
                color = lineColor,
                radius = 5f,
                center = Offset(x, y)
            )
            drawCircle(
                color = Color.White,
                radius = 2.5f,
                center = Offset(x, y)
            )
        }

        fillPath.lineTo(size.width, size.height)
        fillPath.close()

        // Draw gradient under curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent)
            )
        )

        // Draw main line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
