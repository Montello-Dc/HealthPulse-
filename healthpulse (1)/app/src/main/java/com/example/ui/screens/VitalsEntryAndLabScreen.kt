package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.alert.AlertEngine
import com.example.data.model.VitalType
import com.example.ui.components.SeverityBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.HealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalsEntryAndLabScreen(
    viewModel: HealthViewModel,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Manual Entry, 1: Simulation Scenarios, 2: Test Suite

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("vitals_entry_and_lab_screen")
    ) {
        // Screen Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "HEALTHPULSE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = "Lab & Entry",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = GeoOnPrimaryContainer
            )
            Text(
                text = "Manual data entry, diagnostic scenarios, and test suites",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Tab Row
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = GeoPrimary,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTab),
                    color = GeoPrimary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Manual Entry", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Clinical Reference", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("System Tests", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.CheckCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        when (selectedTab) {
            0 -> ManualEntryTab(viewModel = viewModel, onRecordSaved = onNavigateToHistory)
            1 -> ClinicalReferenceTab()
            2 -> TestSuiteTab(viewModel = viewModel)
        }
    }
}

@Composable
fun ManualEntryTab(
    viewModel: HealthViewModel,
    onRecordSaved: () -> Unit
) {
    var selectedType by remember { mutableStateOf(VitalType.BLOOD_PRESSURE) }
    var primaryInput by remember { mutableStateOf("120") }
    var secondaryInput by remember { mutableStateOf("80") }
    var notesInput by remember { mutableStateOf("") }
    var showSuccessToast by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "SELECT VITAL SIGN",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VitalType.values().take(3).forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = {
                            selectedType = type
                            when (type) {
                                VitalType.HEART_RATE -> primaryInput = "72"
                                VitalType.BLOOD_PRESSURE -> { primaryInput = "120"; secondaryInput = "80" }
                                VitalType.BLOOD_OXYGEN -> primaryInput = "98"
                                else -> {}
                            }
                        },
                        label = { Text(type.displayName.take(12)) },
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

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VitalType.values().drop(3).forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = {
                            selectedType = type
                            when (type) {
                                VitalType.TEMPERATURE -> primaryInput = "36.6"
                                VitalType.RESPIRATION -> primaryInput = "16"
                                VitalType.GLUCOSE -> primaryInput = "95"
                                else -> {}
                            }
                        },
                        label = { Text(type.displayName.take(12)) },
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

        // Input Fields
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Enter ${selectedType.displayName} Reading",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = GeoOnPrimaryContainer
                    )

                    OutlinedTextField(
                        value = primaryInput,
                        onValueChange = { primaryInput = it },
                        label = {
                            Text(
                                if (selectedType == VitalType.BLOOD_PRESSURE) "Systolic (${selectedType.unit})"
                                else "Reading (${selectedType.unit})"
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("input_vital_primary"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    if (selectedType == VitalType.BLOOD_PRESSURE) {
                        OutlinedTextField(
                            value = secondaryInput,
                            onValueChange = { secondaryInput = it },
                            label = { Text("Diastolic (${selectedType.unit})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("input_vital_secondary"),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Optional Notes (e.g. morning rest, after meal)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_vital_notes"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Rule Preview
                    val pVal = primaryInput.toDoubleOrNull() ?: 0.0
                    val sVal = if (selectedType == VitalType.BLOOD_PRESSURE) secondaryInput.toDoubleOrNull() else null
                    val eval = AlertEngine.evaluate(selectedType, pVal, sVal)

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Alert Engine: ${eval.title}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = eval.recommendation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            SeverityBadge(severity = eval.severity)
                        }
                    }

                    Button(
                        onClick = {
                            val primary = primaryInput.toDoubleOrNull() ?: return@Button
                            val secondary = if (selectedType == VitalType.BLOOD_PRESSURE) secondaryInput.toDoubleOrNull() else null
                            viewModel.saveVitalRecord(
                                type = selectedType,
                                primary = primary,
                                secondary = secondary,
                                method = "Manual Entry",
                                notes = notesInput
                            )
                            showSuccessToast = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_vital_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Vital Reading", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showSuccessToast) {
            item {
                Surface(
                    color = AlertNormalContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Vital record logged and evaluated successfully!",
                            color = AlertNormalGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        TextButton(onClick = onRecordSaved) {
                            Text("View Trends", color = AlertNormalGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClinicalReferenceTab() {
    val references = listOf(
        Triple("Heart Rate (Pulse)", "60 - 100 BPM", "Normal resting range. <50 BPM is Bradycardia; >100 BPM is Tachycardia. >120 BPM triggers warning."),
        Triple("Blood Pressure", "<120 / <80 mmHg", "Normal systolic/diastolic. 120-129 is Elevated; 130-139 is Stage 1; ≥140 or ≥90 is Stage 2; ≥180/120 is Hypertensive Crisis."),
        Triple("Blood Oxygen (SpO2)", "95% - 100%", "Healthy room air saturation. 90-94% is Low (Elevated alert); <90% indicates severe Hypoxia (Critical alert)."),
        Triple("Body Temperature", "36.5°C - 37.5°C", "Normal core range. 37.6-38.2°C is Low Grade; ≥38.3°C is Fever (Pyrexia); <35.0°C is Hypothermia."),
        Triple("Blood Glucose (Fasting)", "70 - 99 mg/dL", "Normal fasting range. 100-125 mg/dL is Impaired (Pre-diabetes); ≥126 mg/dL is High; <70 mg/dL is Hypoglycemia."),
        Triple("Respiratory Rate", "12 - 20 breaths/min", "Standard adult resting rate. <10 is Bradypnea; >24 is Tachypnea.")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "CLINICAL THRESHOLDS & REFERENCE RANGES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "HealthPulse uses established clinical thresholds for rule-based abnormal detection and automatic alerts.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(references) { (title, range, description) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = GeoOnPrimaryContainer
                        )
                        Surface(
                            color = GeoPrimaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = range,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeoPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TestSuiteTab(viewModel: HealthViewModel) {
    val testResults by viewModel.testResults.collectAsState()
    val isTestRunning by viewModel.isTestRunning.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = GeoPrimaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Automated Diagnostic Test Suite",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = GeoOnPrimaryContainer
                    )
                    Text(
                        text = "Executes end-to-end testing for Google Authentication, Vital-Record Storage/Retrieval DAO, Rule-Based Abnormal Detection, Caregiver Notification Relay, and Gemini Health Intelligence API.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GeoOnPrimaryContainer.copy(alpha = 0.85f)
                    )

                    Button(
                        onClick = { viewModel.runComprehensiveTests() },
                        enabled = !isTestRunning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_test_suite_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary)
                    ) {
                        if (isTestRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Running Diagnostics...")
                        } else {
                            Icon(imageVector = Icons.Default.FactCheck, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Complete Test Suite")
                        }
                    }
                }
            }
        }

        if (testResults.isNotEmpty()) {
            item {
                Text(
                    text = "EXECUTION RESULTS (${testResults.count { it.isSuccess }}/${testResults.size} PASSED)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(testResults) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (item.isSuccess) AlertNormalGreen else AlertCriticalRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.testName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = item.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${item.durationMs}ms",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

