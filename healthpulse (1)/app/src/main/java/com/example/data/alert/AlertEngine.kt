package com.example.data.alert

import com.example.data.model.AlertSeverity
import com.example.data.model.VitalType

data class VitalEvaluation(
    val severity: AlertSeverity,
    val title: String,
    val message: String,
    val recommendation: String,
    val isAbnormal: Boolean
)

object AlertEngine {

    fun evaluate(type: VitalType, primary: Double, secondary: Double? = null): VitalEvaluation {
        return when (type) {
            VitalType.HEART_RATE -> evaluateHeartRate(primary.toInt())
            VitalType.BLOOD_PRESSURE -> evaluateBloodPressure(primary.toInt(), secondary?.toInt() ?: 80)
            VitalType.BLOOD_OXYGEN -> evaluateOxygen(primary)
            VitalType.TEMPERATURE -> evaluateTemperature(primary)
            VitalType.RESPIRATION -> evaluateRespiration(primary.toInt())
            VitalType.GLUCOSE -> evaluateGlucose(primary)
        }
    }

    private fun evaluateHeartRate(bpm: Int): VitalEvaluation {
        return when {
            bpm < 45 -> VitalEvaluation(
                severity = AlertSeverity.CRITICAL,
                title = "Severe Bradycardia",
                message = "Heart rate is critically low at $bpm bpm.",
                recommendation = "Sit down, rest immediately, and contact medical care or your cardiologist right away.",
                isAbnormal = true
            )
            bpm in 45..59 -> VitalEvaluation(
                severity = AlertSeverity.ELEVATED,
                title = "Mild Bradycardia",
                message = "Resting pulse is slightly below standard range at $bpm bpm.",
                recommendation = "Normal for endurance athletes; otherwise monitor if accompanied by dizziness or fatigue.",
                isAbnormal = true
            )
            bpm in 60..100 -> VitalEvaluation(
                severity = AlertSeverity.NORMAL,
                title = "Healthy Heart Rate",
                message = "Heart rate is optimal at $bpm bpm.",
                recommendation = "Great job! Keep maintaining your regular hydration and healthy routine.",
                isAbnormal = false
            )
            bpm in 101..120 -> VitalEvaluation(
                severity = AlertSeverity.WARNING,
                title = "Mild Tachycardia",
                message = "Heart rate is elevated at $bpm bpm.",
                recommendation = "Rest quietly for 5-10 minutes, take slow deep breaths, hydrate, and re-measure.",
                isAbnormal = true
            )
            else -> VitalEvaluation(
                severity = AlertSeverity.CRITICAL,
                title = "High Tachycardia Alert",
                message = "Heart rate is abnormally elevated at $bpm bpm.",
                recommendation = "Stop physical exertion immediately, remain calm, and seek medical attention if persistent.",
                isAbnormal = true
            )
        }
    }

    private fun evaluateBloodPressure(systolic: Int, diastolic: Int): VitalEvaluation {
        return when {
            systolic >= 180 || diastolic >= 120 -> VitalEvaluation(
                severity = AlertSeverity.CRITICAL,
                title = "Hypertensive Crisis Alert",
                message = "Blood pressure is dangerously high at $systolic/$diastolic mmHg.",
                recommendation = "Emergency situation: Rest 5 minutes and re-check. Seek immediate urgent medical care if reading stays high.",
                isAbnormal = true
            )
            systolic >= 140 || diastolic >= 90 -> VitalEvaluation(
                severity = AlertSeverity.WARNING,
                title = "Stage 2 Hypertension",
                message = "Blood pressure is elevated at $systolic/$diastolic mmHg.",
                recommendation = "Consult your healthcare provider, review sodium intake, and take prescribed anti-hypertensive medication if scheduled.",
                isAbnormal = true
            )
            systolic in 130..139 || diastolic in 80..89 -> VitalEvaluation(
                severity = AlertSeverity.WARNING,
                title = "Stage 1 Hypertension",
                message = "Blood pressure is slightly high at $systolic/$diastolic mmHg.",
                recommendation = "Engage in stress reduction, light walking, reduce caffeine, and monitor twice daily.",
                isAbnormal = true
            )
            systolic in 120..129 && diastolic < 80 -> VitalEvaluation(
                severity = AlertSeverity.ELEVATED,
                title = "Elevated Blood Pressure",
                message = "Systolic blood pressure is moderately elevated at $systolic/$diastolic mmHg.",
                recommendation = "Practice lifestyle modifications including healthy diet and regular physical activity.",
                isAbnormal = true
            )
            systolic < 90 || diastolic < 60 -> VitalEvaluation(
                severity = AlertSeverity.WARNING,
                title = "Hypotension (Low BP)",
                message = "Blood pressure is low at $systolic/$diastolic mmHg.",
                recommendation = "Drink a glass of water, stand up slowly to prevent orthostatic dizziness, and rest.",
                isAbnormal = true
            )
            else -> VitalEvaluation(
                severity = AlertSeverity.NORMAL,
                title = "Optimal Blood Pressure",
                message = "Blood pressure is well-regulated at $systolic/$diastolic mmHg.",
                recommendation = "Excellent cardiovascular stability. Maintain your balanced diet and exercise routine.",
                isAbnormal = false
            )
        }
    }

    private fun evaluateOxygen(spo2: Double): VitalEvaluation {
        return when {
            spo2 < 90.0 -> VitalEvaluation(
                severity = AlertSeverity.CRITICAL,
                title = "Severe Hypoxemia Alert",
                message = "Blood oxygen level is dangerously low at ${spo2.toInt()}%.",
                recommendation = "Urgent: Sit upright in a well-ventilated space and seek immediate medical assistance or supplemental oxygen.",
                isAbnormal = true
            )
            spo2 in 90.0..94.0 -> VitalEvaluation(
                severity = AlertSeverity.WARNING,
                title = "Low Blood Oxygen",
                message = "Oxygen saturation is slightly depressed at ${spo2.toInt()}%.",
                recommendation = "Practice slow diaphragmatic breathing. Re-check in 5 minutes and contact doctor if it does not rise.",
                isAbnormal = true
            )
            else -> VitalEvaluation(
                severity = AlertSeverity.NORMAL,
                title = "Normal Oxygenation",
                message = "Blood oxygen level is strong at ${spo2.toInt()}%.",
                recommendation = "Your respiratory transfer and oxygen delivery are functioning properly.",
                isAbnormal = false
            )
        }
    }

    private fun evaluateTemperature(tempC: Double): VitalEvaluation {
        return when {
            tempC >= 39.5 -> VitalEvaluation(
                severity = AlertSeverity.CRITICAL,
                title = "High Fever Alert",
                message = "Body temperature is high at ${"%.1f".format(tempC)}°C.",
                recommendation = "Apply cooling compress, stay well hydrated with electrolytes, and consult a physician promptly.",
                isAbnormal = true
            )
            tempC >= 38.0 -> VitalEvaluation(
                severity = AlertSeverity.WARNING,
                title = "Fever Detected",
                message = "Elevated temperature at ${"%.1f".format(tempC)}°C.",
                recommendation = "Rest in bed, hydrate frequently, monitor symptoms, and consider fever-reducing medication if advised.",
                isAbnormal = true
            )
            tempC in 37.3..37.9 -> VitalEvaluation(
                severity = AlertSeverity.ELEVATED,
                title = "Low-Grade Temperature",
                message = "Temperature is mildly elevated at ${"%.1f".format(tempC)}°C.",
                recommendation = "Stay hydrated and avoid heavy exertion while monitoring for other symptoms.",
                isAbnormal = true
            )
            tempC < 35.5 -> VitalEvaluation(
                severity = AlertSeverity.WARNING,
                title = "Hypothermia Warning",
                message = "Body temperature is low at ${"%.1f".format(tempC)}°C.",
                recommendation = "Warm up with blankets, drink warm fluids, and keep the ambient room heated.",
                isAbnormal = true
            )
            else -> VitalEvaluation(
                severity = AlertSeverity.NORMAL,
                title = "Normal Body Temperature",
                message = "Body temperature is normal at ${"%.1f".format(tempC)}°C.",
                recommendation = "Thermoregulation is balanced and healthy.",
                isAbnormal = false
            )
        }
    }

    private fun evaluateRespiration(rate: Int): VitalEvaluation {
        return when {
            rate < 10 -> VitalEvaluation(
                severity = AlertSeverity.WARNING,
                title = "Bradypnea (Slow Breathing)",
                message = "Breathing rate is low at $rate breaths/min.",
                recommendation = "Take intentional deep breaths and consult a provider if feeling lightheaded.",
                isAbnormal = true
            )
            rate in 12..20 -> VitalEvaluation(
                severity = AlertSeverity.NORMAL,
                title = "Normal Respiration",
                message = "Respiratory cadence is steady at $rate breaths/min.",
                recommendation = "Healthy pulmonary rhythm.",
                isAbnormal = false
            )
            else -> VitalEvaluation(
                severity = AlertSeverity.WARNING,
                title = "Tachypnea (Rapid Breathing)",
                message = "Elevated breathing rate at $rate breaths/min.",
                recommendation = "Sit comfortably, relax your shoulders, and practice calm 4-second box breathing.",
                isAbnormal = true
            )
        }
    }

    private fun evaluateGlucose(glucose: Double): VitalEvaluation {
        return when {
            glucose < 70.0 -> VitalEvaluation(
                severity = AlertSeverity.CRITICAL,
                title = "Hypoglycemia Alert",
                message = "Blood glucose is low at ${glucose.toInt()} mg/dL.",
                recommendation = "Consume 15-20g of fast-acting carbohydrates (juice, glucose tablet) and re-test in 15 minutes.",
                isAbnormal = true
            )
            glucose in 70.0..99.0 -> VitalEvaluation(
                severity = AlertSeverity.NORMAL,
                title = "Normal Fasting Glucose",
                message = "Blood sugar is well balanced at ${glucose.toInt()} mg/dL.",
                recommendation = "Optimal metabolic health. Continue balanced dietary habits.",
                isAbnormal = false
            )
            glucose in 100.0..125.0 -> VitalEvaluation(
                severity = AlertSeverity.ELEVATED,
                title = "Pre-Diabetes Range",
                message = "Fasting blood sugar is moderately elevated at ${glucose.toInt()} mg/dL.",
                recommendation = "Review carbohydrate intake, maintain physical activity, and track postprandial levels.",
                isAbnormal = true
            )
            else -> VitalEvaluation(
                severity = AlertSeverity.WARNING,
                title = "Hyperglycemia Alert",
                message = "High blood glucose at ${glucose.toInt()} mg/dL.",
                recommendation = "Hydrate with water, take prescribed insulin/medication as directed, and check ketones if needed.",
                isAbnormal = true
            )
        }
    }
}
