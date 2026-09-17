package com.example.data.model

enum class VitalType(val displayName: String, val unit: String) {
    HEART_RATE("Heart Rate", "bpm"),
    BLOOD_PRESSURE("Blood Pressure", "mmHg"),
    BLOOD_OXYGEN("Blood Oxygen (SpO2)", "%"),
    TEMPERATURE("Body Temperature", "°C"),
    RESPIRATION("Respiratory Rate", "br/min"),
    GLUCOSE("Blood Glucose", "mg/dL")
}

enum class AlertSeverity(val levelName: String) {
    NORMAL("Normal"),
    ELEVATED("Elevated"),
    WARNING("Warning"),
    CRITICAL("Critical Alert")
}
