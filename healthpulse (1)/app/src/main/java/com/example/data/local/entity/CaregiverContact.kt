package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "caregiver_contacts")
data class CaregiverContact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val relationship: String,
    val phone: String,
    val email: String,
    val isEmergencyAlertEnabled: Boolean = true
)
