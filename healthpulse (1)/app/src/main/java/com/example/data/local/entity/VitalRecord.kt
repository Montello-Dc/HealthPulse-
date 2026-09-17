package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AlertSeverity
import com.example.data.model.VitalType

@Entity(tableName = "vital_records")
data class VitalRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: VitalType,
    val valuePrimary: Double,
    val valueSecondary: Double? = null,
    val unit: String,
    val severity: AlertSeverity = AlertSeverity.NORMAL,
    val alertMessage: String? = null,
    val measurementMethod: String = "Manual Entry",
    val notes: String = ""
)
