package com.example.ui.screens

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.local.entity.VitalRecord
import com.example.data.model.AlertSeverity
import com.example.data.ppg.PpgPulseAnalyzer
import com.example.ui.components.PpgLiveWaveformCanvas
import com.example.ui.components.SeverityBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.HealthViewModel
import com.example.ui.viewmodel.PpgScanSummary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PpgScanScreen(
    viewModel: HealthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val isPpgActive by viewModel.isPpgActive.collectAsState()
    val isFingerDetected by viewModel.isFingerDetected.collectAsState()
    val liveBpm by viewModel.liveBpm.collectAsState()
    val signalQuality by viewModel.signalQuality.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val ppgWavePoints by viewModel.ppgWavePoints.collectAsState()
    val latestScanResult by viewModel.latestScanResult.collectAsState()
    val latestScanSummary by viewModel.latestScanSummary.collectAsState()

    var isSimulationMode by remember { mutableStateOf(false) }

    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPpgMeasurement()
        }
    }

    ScangementContent(
        isPpgActive = isPpgActive,
        isFingerDetected = isFingerDetected,
        liveBpm = liveBpm,
        signalQuality = signalQuality,
        scanProgress = scanProgress,
        ppgWavePoints = ppgWavePoints,
        latestScanResult = latestScanResult,
        latestScanSummary = latestScanSummary,
        pulseScale = pulseScale,
        hasCameraPermission = cameraPermissionState.status.isGranted,
        onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
        isSimulationMode = isSimulationMode,
        onToggleSimulationMode = {
            isSimulationMode = it
            if (isPpgActive) viewModel.stopPpgMeasurement()
        },
        onStartScan = {
            viewModel.startPpgMeasurement(isSimulation = isSimulationMode)
        },
        onStopScan = {
            viewModel.stopPpgMeasurement()
        },
        onResetScan = {
            viewModel.stopPpgMeasurement()
        },
        onNavigateBack = onNavigateBack,
        onNavigateToHistory = onNavigateToHistory,
        onFrameAnalyzed = { finger, bpm, sqi, intensity ->
            viewModel.onPpgFrameAnalyzed(finger, bpm, sqi, intensity)
        },
        modifier = modifier
    )
}

@Composable
private fun ScangementContent(
    isPpgActive: Boolean,
    isFingerDetected: Boolean,
    liveBpm: Int?,
    signalQuality: Int,
    scanProgress: Float,
    ppgWavePoints: List<Float>,
    latestScanResult: VitalRecord?,
    latestScanSummary: PpgScanSummary?,
    pulseScale: Float,
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit,
    isSimulationMode: Boolean,
    onToggleSimulationMode: (Boolean) -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onResetScan: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onFrameAnalyzed: (Boolean, Int?, Int, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
            .testTag("ppg_scan_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "HEALTHPULSE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = "Pulse Scanner",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GeoOnPrimaryContainer
                )
            }
            IconButton(onClick = onNavigateToHistory) {
                Icon(imageVector = Icons.Default.History, contentDescription = "History")
            }
        }

        // Camera Preview or Simulation View
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF191C1E))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isPpgActive && !isSimulationMode && hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()

                                val analyzer = PpgPulseAnalyzer { result ->
                                    onFrameAnalyzed(
                                        result.isFingerCoveringLens,
                                        result.estimatedBpm,
                                        result.signalQualityPercentage,
                                        result.redIntensity
                                    )
                                }
                                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), analyzer)

                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                try {
                                    cameraProvider.unbindAll()
                                    val camera = cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageAnalysis
                                    )
                                    // Turn on flashlight
                                    if (camera.cameraInfo.hasFlashUnit()) {
                                        camera.cameraControl.enableTorch(true)
                                    }
                                } catch (e: Exception) {
                                    // Fallback
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(28.dp))
                    )
                }

                // Overlay graphics & Pulsing heart visualizer
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(125.dp)
                            .scale(if (isPpgActive && isFingerDetected) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = if (isFingerDetected) listOf(Color(0xFFEF4444).copy(alpha = 0.85f), Color(0xFF7F1D1D).copy(alpha = 0.4f))
                                    else listOf(Color(0xFF334155), Color(0xFF1E293B))
                                )
                            )
                            .border(
                                width = 3.dp,
                                color = if (isFingerDetected) Color(0xFFEF4444) else Color(0xFF64748B),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Pulse Heart",
                            tint = if (isFingerDetected) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (!isPpgActive) "Tap Start Scan Below"
                        else if (isFingerDetected) "Fingertip Detected • Measuring Pulse..."
                        else "Place fingertip over camera lens & flash",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )

                    if (isPpgActive) {
                        Text(
                            text = "Signal Quality: $signalQuality%",
                            color = if (signalQuality > 60) AlertNormalGreen else AlertElevatedYellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Real-Time Waveform & BPM Readout
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "REAL-TIME PPG WAVEFORM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (liveBpm != null) "$liveBpm" else "--",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeoPrimary
                        )
                        Text(
                            text = "BPM",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                PpgLiveWaveformCanvas(
                    wavePoints = ppgWavePoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    lineColor = if (isFingerDetected) PulseWaveColor else Color(0xFF475569)
                )

                if (isPpgActive) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Scan Progress (15s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(scanProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { scanProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = GeoPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        // Action Buttons & Simulation Switch
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!hasCameraPermission && !isSimulationMode) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = AlertElevatedYellow),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Grant Camera Permission for PPG")
                }
            }

            if (!isPpgActive) {
                Button(
                    onClick = onStartScan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_scan_action_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSimulationMode) "Start Simulated PPG Scan" else "Start Camera PPG Scan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onStopScan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("stop_scan_action_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCriticalRed)
                ) {
                    Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cancel Scan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Mode Selector Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = GeoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Simulation Mode (Prototype)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Switch(
                        checked = isSimulationMode,
                        onCheckedChange = onToggleSimulationMode,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GeoPrimary
                        )
                    )
                }
            }
        }

        // Complete Multi-Vital Scan Assessment
        if (latestScanSummary != null || latestScanResult != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, GeoPrimary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .testTag("ppg_scan_results_panel"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = GeoPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = GeoPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Scan Completed & Saved",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GeoOnPrimaryContainer
                                )
                                Text(
                                    text = "All 5 vital metrics saved to your history",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Vital Metric Rows
                    val hrRecord = latestScanSummary?.heartRate ?: latestScanResult
                    if (hrRecord != null) {
                        VitalScanResultItem(
                            icon = Icons.Default.Favorite,
                            iconColor = Color(0xFFEF4444),
                            title = "Heart Rate (Pulse)",
                            value = "${hrRecord.valuePrimary.toInt()}",
                            unit = "BPM",
                            severity = hrRecord.severity
                        )
                    }

                    if (latestScanSummary != null) {
                        VitalScanResultItem(
                            icon = Icons.Outlined.Speed,
                            iconColor = GeoPrimary,
                            title = "Blood Pressure",
                            value = "${latestScanSummary.bloodPressure.valuePrimary.toInt()}/${latestScanSummary.bloodPressure.valueSecondary?.toInt() ?: 80}",
                            unit = "mmHg",
                            severity = latestScanSummary.bloodPressure.severity
                        )

                        VitalScanResultItem(
                            icon = Icons.Outlined.Air,
                            iconColor = Color(0xFF0284C7),
                            title = "Blood Oxygen (SpO2)",
                            value = "${latestScanSummary.bloodOxygen.valuePrimary.toInt()}%",
                            unit = "",
                            severity = latestScanSummary.bloodOxygen.severity
                        )

                        VitalScanResultItem(
                            icon = Icons.Outlined.WaterDrop,
                            iconColor = Color(0xFF8B5CF6),
                            title = "Blood Glucose",
                            value = "${latestScanSummary.bloodGlucose.valuePrimary.toInt()}",
                            unit = "mg/dL",
                            severity = latestScanSummary.bloodGlucose.severity
                        )

                        VitalScanResultItem(
                            icon = Icons.Outlined.DeviceThermostat,
                            iconColor = Color(0xFFF97316),
                            title = "Body Temperature",
                            value = String.format(java.util.Locale.US, "%.1f", latestScanSummary.bodyTemperature.valuePrimary),
                            unit = "°C",
                            severity = latestScanSummary.bodyTemperature.severity
                        )
                    }

                    // Alert note if any abnormal reading
                    val alertRecord = latestScanSummary?.let { summary ->
                        listOf(summary.heartRate, summary.bloodPressure, summary.bloodOxygen, summary.bloodGlucose, summary.bodyTemperature)
                            .firstOrNull { it.severity != AlertSeverity.NORMAL }
                    } ?: latestScanResult?.takeIf { it.severity != AlertSeverity.NORMAL }

                    if (alertRecord != null && !alertRecord.alertMessage.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = AlertElevatedYellow.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AlertElevatedYellow.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AlertElevatedYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = alertRecord.alertMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GeoOnPrimaryContainer
                                )
                            }
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onStartScan,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Scan Again", color = GeoPrimary)
                        }
                        Button(
                            onClick = onNavigateToHistory,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary)
                        ) {
                            Text("View Trends")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VitalScanResultItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    unit: String,
    severity: AlertSeverity
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = value,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeoOnPrimaryContainer
                    )
                    if (unit.isNotBlank()) {
                        Text(
                            text = unit,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 1.dp)
                        )
                    }
                }
                SeverityBadge(severity = severity)
            }
        }
    }
}
