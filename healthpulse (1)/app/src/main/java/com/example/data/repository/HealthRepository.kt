package com.example.data.repository

import com.example.data.alert.AlertEngine
import com.example.data.local.dao.HealthDao
import com.example.data.local.entity.CaregiverContact
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.MedicationReminder
import com.example.data.local.entity.VitalRecord
import com.example.data.model.AlertSeverity
import com.example.data.model.VitalType
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class HealthRepository(private val healthDao: HealthDao) {

    val allVitalRecords: Flow<List<VitalRecord>> = healthDao.getAllVitalRecords()
    val alertRecords: Flow<List<VitalRecord>> = healthDao.getAlertRecords()
    val recentVitalRecords: Flow<List<VitalRecord>> = healthDao.getRecentVitalRecords()
    val allReminders: Flow<List<MedicationReminder>> = healthDao.getAllReminders()
    val allCaregivers: Flow<List<CaregiverContact>> = healthDao.getAllCaregivers()
    val allChatMessages: Flow<List<ChatMessageEntity>> = healthDao.getAllChatMessages()

    fun getRecordsByType(type: VitalType): Flow<List<VitalRecord>> {
        return healthDao.getVitalRecordsByType(type)
    }

    fun getRecordsSince(sinceTimestamp: Long): Flow<List<VitalRecord>> {
        return healthDao.getVitalRecordsSince(sinceTimestamp)
    }

    suspend fun recordVital(
        type: VitalType,
        primary: Double,
        secondary: Double? = null,
        method: String = "Manual Entry",
        notes: String = ""
    ): VitalRecord {
        val evaluation = AlertEngine.evaluate(type, primary, secondary)
        val record = VitalRecord(
            timestamp = System.currentTimeMillis(),
            type = type,
            valuePrimary = primary,
            valueSecondary = secondary,
            unit = type.unit,
            severity = evaluation.severity,
            alertMessage = if (evaluation.isAbnormal) "${evaluation.title}: ${evaluation.message}" else null,
            measurementMethod = method,
            notes = notes
        )
        val insertedId = healthDao.insertVitalRecord(record)
        return record.copy(id = insertedId)
    }

    suspend fun deleteVitalRecord(record: VitalRecord) {
        healthDao.deleteVitalRecord(record)
    }

    suspend fun clearAllRecords() {
        healthDao.clearAllVitalRecords()
    }

    suspend fun addReminder(
        title: String,
        dosage: String,
        hour: Int,
        minute: Int,
        category: String = "Medication"
    ) {
        val reminder = MedicationReminder(
            title = title,
            dosage = dosage,
            timeHour = hour,
            timeMinute = minute,
            category = category
        )
        healthDao.insertReminder(reminder)
    }

    suspend fun toggleReminderActive(reminder: MedicationReminder) {
        healthDao.updateReminder(reminder.copy(isActive = !reminder.isActive))
    }

    suspend fun markReminderTaken(reminder: MedicationReminder) {
        healthDao.updateReminder(reminder.copy(lastTakenTimestamp = System.currentTimeMillis()))
    }

    suspend fun deleteReminder(reminder: MedicationReminder) {
        healthDao.deleteReminder(reminder)
    }

    suspend fun addCaregiver(name: String, relationship: String, phone: String, email: String) {
        val caregiver = CaregiverContact(
            name = name,
            relationship = relationship,
            phone = phone,
            email = email,
            isEmergencyAlertEnabled = true
        )
        healthDao.insertCaregiver(caregiver)
    }

    suspend fun toggleCaregiverAlert(caregiver: CaregiverContact) {
        healthDao.updateCaregiver(caregiver.copy(isEmergencyAlertEnabled = !caregiver.isEmergencyAlertEnabled))
    }

    suspend fun deleteCaregiver(caregiver: CaregiverContact) {
        healthDao.deleteCaregiver(caregiver)
    }

    // Chat persistence
    suspend fun saveChatMessage(text: String, isUser: Boolean): Long {
        val messageEntity = com.example.data.local.entity.ChatMessageEntity(
            text = text,
            isUser = isUser,
            timestamp = System.currentTimeMillis()
        )
        return healthDao.insertChatMessage(messageEntity)
    }

    suspend fun clearChatMessages() {
        healthDao.clearChatMessages()
    }
}
