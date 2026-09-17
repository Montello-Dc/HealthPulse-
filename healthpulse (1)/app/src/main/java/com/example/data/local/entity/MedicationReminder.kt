package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medication_reminders")
data class MedicationReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val dosage: String,
    val timeHour: Int,
    val timeMinute: Int,
    val daysOfWeek: String = "Daily",
    val isActive: Boolean = true,
    val lastTakenTimestamp: Long? = null,
    val category: String = "Medication" // Medication, Vitals Check, Hydration, Exercise
)
