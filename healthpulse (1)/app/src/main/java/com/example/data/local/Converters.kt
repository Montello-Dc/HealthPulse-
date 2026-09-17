package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AlertSeverity
import com.example.data.model.VitalType

class Converters {
    @TypeConverter
    fun fromVitalType(value: VitalType): String = value.name

    @TypeConverter
    fun toVitalType(value: String): VitalType = try {
        VitalType.valueOf(value)
    } catch (e: Exception) {
        VitalType.HEART_RATE
    }

    @TypeConverter
    fun fromAlertSeverity(value: AlertSeverity): String = value.name

    @TypeConverter
    fun toAlertSeverity(value: String): AlertSeverity = try {
        AlertSeverity.valueOf(value)
    } catch (e: Exception) {
        AlertSeverity.NORMAL
    }
}
