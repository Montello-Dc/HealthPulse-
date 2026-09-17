package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MedicationReminder
import com.example.data.local.entity.VitalRecord
import com.example.data.model.AlertSeverity
import com.example.data.model.VitalType
import com.example.ui.components.PpgLiveWaveformCanvas
import com.example.ui.components.SeverityBadge
import com.example.ui.components.TopUserHeader
import com.example.ui.components.VitalCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.HealthViewModel

@Composable
fun HomeScreen(
    viewModel: HealthViewModel,
    onNavigateToPpg: () -> Unit,
    onNavigateToLab: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vitals by viewModel.allVitals.collectAsState()
    val alertRecords by viewModel.alertRecords.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAuthDialog by remember { mutableStateOf(false) }

    val latestHeartRate = vitals.firstOrNull { it.type == VitalType.HEART_RATE }
    val latestBloodPressure = vitals.firstOrNull { it.type == VitalType.BLOOD_PRESSURE }
    val latestOxygen = vitals.firstOrNull { it.type == VitalType.BLOOD_OXYGEN }
    val latestTemp = vitals.firstOrNull { it.type == VitalType.TEMPERATURE }
    val latestGlucose = vitals.firstOrNull { it.type == VitalType.GLUCOSE }

    val nextReminder = reminders.firstOrNull { it.isActive }

    // Waveform simulation baseline for the hero card
    val heroWaveform = remember {
        listOf(
            0.5f, 0.5f, 0.52f, 0.48f, 0.5f, 0.25f, 0.85f, 0.5f, 0.55f, 0.48f,
            0.5f, 0.5f, 0.2f, 0.9f, 0.5f, 0.53f, 0.49f, 0.5f, 0.5f, 0.15f, 0.95f, 0.5f
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TopUserHeader(
                user = currentUser,
                alertCount = alertRecords.size,
                onAuthClick = { showAuthDialog = true },
                onAlertsClick = onNavigateToAlerts,
                screenTitle = "Overview"
            )
        }

        // Geometric Balance Live PPG Monitoring Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigateToPpg() }
                    .testTag("hero_ppg_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GeoPrimaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(195.dp)
                        .padding(20.dp)
                ) {
                    // Subtle Waveform Path at bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(85.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        PpgLiveWaveformCanvas(
                            wavePoints = heroWaveform,
                            modifier = Modifier.fillMaxSize(),
                            lineColor = GeoPrimary.copy(alpha = 0.45f)
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(GeoPrimary)
                                )
                                Text(
                                    text = "Live PPG Monitoring",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GeoPrimary
                                )
                            }

                            val hrDisplay = latestHeartRate?.valuePrimary?.toInt()?.toString() ?: "--"
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = hrDisplay,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoOnPrimaryContainer
                                )
                                Text(
                                    text = "BPM",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = GeoOnPrimaryContainer.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            Text(
                                text = if (latestHeartRate != null && latestHeartRate.severity != AlertSeverity.NORMAL)
                                    (latestHeartRate.alertMessage ?: "Abnormal heart rate detected")
                                else if (latestHeartRate != null) "Resting heart rate is stable"
                                else "No pulse scan recorded • Tap to measure via camera",
                                fontSize = 13.sp,
                                color = GeoOnPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = onNavigateToPpg,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GeoPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("start_ppg_scan_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Scan Pulse (15s)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Geometric Balance 2x2 Square Grid
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VITAL SIGNS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = onNavigateToHistory,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "View Trends",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeoPrimary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GeometricSquareCard(
                        title = "Blood Oxygen",
                        value = latestOxygen?.valuePrimary?.toInt()?.toString() ?: "--",
                        unit = "%",
                        isAlert = latestOxygen?.severity == AlertSeverity.CRITICAL || latestOxygen?.severity == AlertSeverity.WARNING,
                        icon = Icons.Outlined.Air,
                        iconSymbol = "◌",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHistory
                    )
                    GeometricSquareCard(
                        title = "BP Check",
                        value = if (latestBloodPressure != null) "${latestBloodPressure.valuePrimary.toInt()}/${latestBloodPressure.valueSecondary?.toInt() ?: 80}" else "--/--",
                        unit = "",
                        isAlert = latestBloodPressure?.severity == AlertSeverity.CRITICAL || latestBloodPressure?.severity == AlertSeverity.WARNING,
                        icon = Icons.Outlined.Speed,
                        iconSymbol = "!",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHistory
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GeometricSquareCard(
                        title = "Temperature",
                        value = if (latestTemp != null) "%.1f".format(latestTemp.valuePrimary) else "--",
                        unit = "°C",
                        isAlert = latestTemp?.severity == AlertSeverity.CRITICAL || latestTemp?.severity == AlertSeverity.WARNING,
                        icon = Icons.Outlined.DeviceThermostat,
                        iconSymbol = "🌡",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHistory
                    )
                    GeometricSquareCard(
                        title = "Blood Glucose",
                        value = latestGlucose?.valuePrimary?.toInt()?.toString() ?: "--",
                        unit = "mg/dL",
                        isAlert = latestGlucose?.severity == AlertSeverity.CRITICAL || latestGlucose?.severity == AlertSeverity.WARNING,
                        icon = Icons.Outlined.WaterDrop,
                        iconSymbol = "◆",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHistory
                    )
                }
            }
        }

        // Geometric Balance Health Assistant Prompt Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigateToChat() }
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                    .testTag("home_chat_cta_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GeoTertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = GeoOnTertiaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "HEALTH ASSISTANT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "“Hi! Your resting vitals look steady today. Would you like a quick checkup breakdown?”",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Geometric Balance Next Reminder Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigateToReminders() }
                    .testTag("home_reminder_bar"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GeoPrimary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "NEXT REMINDER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val reminderText = if (nextReminder != null) {
                            val timeStr = "%02d:%02d".format(nextReminder.timeHour, nextReminder.timeMinute)
                            "${nextReminder.title} • $timeStr"
                        } else {
                            "Medication • 8:00 PM"
                        }
                        Text(
                            text = reminderText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Next reminder",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Active Health Alerts (if any)
        if (alertRecords.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE ALERTS (${alertRecords.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = AlertCriticalRed
                        )
                        TextButton(
                            onClick = onNavigateToAlerts,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Caregiver Alerts",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AlertCriticalRed
                            )
                        }
                    }

                    alertRecords.take(2).forEach { record ->
                        VitalCard(record = record, onClick = onNavigateToAlerts)
                    }
                }
            }
        }

        // Today's Schedule
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAILY PLAN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = onNavigateToReminders,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Manage All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeoPrimary
                        )
                    }
                }

                if (reminders.isEmpty()) {
                    Text(
                        text = "No reminders scheduled for today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    reminders.take(3).forEach { reminder ->
                        ReminderItemRow(
                            reminder = reminder,
                            onToggle = { viewModel.toggleReminder(reminder) },
                            onMarkTaken = { viewModel.markReminderTaken(reminder) }
                        )
                    }
                }
            }
        }
    }

    if (showAuthDialog) {
        var editName by remember { mutableStateOf(currentUser?.name ?: "Caro Thom") }
        var editEmail by remember { mutableStateOf(currentUser?.email ?: "carothom12@gmail.com") }
        var editBloodType by remember { mutableStateOf(currentUser?.bloodType ?: "O+") }
        var editAge by remember { mutableStateOf((currentUser?.age ?: 32).toString()) }

        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = GeoPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Patient Account",
                        fontWeight = FontWeight.Bold,
                        color = GeoOnPrimaryContainer
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Signed in with Google Authentication for secure local biometric record persistence.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_dialog_name_input")
                    )

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Google Account Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_dialog_email_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editBloodType,
                            onValueChange = { editBloodType = it },
                            label = { Text("Blood Type") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("auth_dialog_blood_input")
                        )

                        OutlinedTextField(
                            value = editAge,
                            onValueChange = { editAge = it },
                            label = { Text("Age") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("auth_dialog_age_input")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedAge = editAge.toIntOrNull() ?: 32
                        viewModel.updateProfile(
                            name = editName.ifBlank { "User" },
                            email = editEmail.ifBlank { "carothom12@gmail.com" },
                            bloodType = editBloodType.ifBlank { "O+" },
                            age = parsedAge
                        )
                        showAuthDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("dialog_save_profile_button")
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.signInGoogle("carothom12@gmail.com", "Caro Thom")
                        showAuthDialog = false
                    }
                ) {
                    Text("Google Re-Auth", color = GeoPrimary)
                }
            }
        )
    }
}

@Composable
fun GeometricSquareCard(
    title: String,
    value: String,
    unit: String,
    isAlert: Boolean,
    icon: ImageVector,
    iconSymbol: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() }
            .testTag("mini_card_$title")
            .then(
                if (isAlert) Modifier.border(2.dp, AlertCriticalContainer, RoundedCornerShape(28.dp))
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isAlert) AlertCriticalContainer else Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (iconSymbol.isNotBlank()) {
                    Text(
                        text = iconSymbol,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAlert) AlertCriticalRed else GeoPrimary
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isAlert) AlertCriticalRed else GeoPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = if (isAlert) AlertCriticalRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = value,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isAlert) AlertCriticalRed else MaterialTheme.colorScheme.onSurface
                    )
                    if (unit.isNotBlank()) {
                        Text(
                            text = unit,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isAlert) AlertCriticalRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderItemRow(
    reminder: MedicationReminder,
    onToggle: () -> Unit,
    onMarkTaken: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onMarkTaken,
                    modifier = Modifier.size(36.dp)
                ) {
                    val isTakenToday = reminder.lastTakenTimestamp != null &&
                            (System.currentTimeMillis() - reminder.lastTakenTimestamp < 12 * 3600 * 1000)

                    Icon(
                        imageVector = if (isTakenToday) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = "Mark Taken",
                        tint = if (isTakenToday) AlertNormalGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column {
                    Text(
                        text = reminder.title,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val timeStr = "%02d:%02d".format(reminder.timeHour, reminder.timeMinute)
                    Text(
                        text = "$timeStr • ${reminder.dosage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = reminder.isActive,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = GeoPrimary
                )
            )
        }
    }
}

