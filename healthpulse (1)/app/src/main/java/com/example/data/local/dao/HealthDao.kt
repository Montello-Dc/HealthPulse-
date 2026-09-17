package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.CaregiverContact
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.MedicationReminder
import com.example.data.local.entity.VitalRecord
import com.example.data.model.VitalType
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    // Vital Records
    @Query("SELECT * FROM vital_records ORDER BY timestamp DESC")
    fun getAllVitalRecords(): Flow<List<VitalRecord>>

    @Query("SELECT * FROM vital_records WHERE type = :type ORDER BY timestamp DESC")
    fun getVitalRecordsByType(type: VitalType): Flow<List<VitalRecord>>

    @Query("SELECT * FROM vital_records WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getVitalRecordsSince(sinceTimestamp: Long): Flow<List<VitalRecord>>

    @Query("SELECT * FROM vital_records WHERE severity != 'NORMAL' ORDER BY timestamp DESC")
    fun getAlertRecords(): Flow<List<VitalRecord>>

    @Query("SELECT * FROM vital_records ORDER BY timestamp DESC LIMIT 10")
    fun getRecentVitalRecords(): Flow<List<VitalRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitalRecord(record: VitalRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitalRecords(records: List<VitalRecord>)

    @Delete
    suspend fun deleteVitalRecord(record: VitalRecord)

    @Query("DELETE FROM vital_records")
    suspend fun clearAllVitalRecords()

    // Reminders
    @Query("SELECT * FROM medication_reminders ORDER BY timeHour ASC, timeMinute ASC")
    fun getAllReminders(): Flow<List<MedicationReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: MedicationReminder): Long

    @Update
    suspend fun updateReminder(reminder: MedicationReminder)

    @Delete
    suspend fun deleteReminder(reminder: MedicationReminder)

    // Caregivers
    @Query("SELECT * FROM caregiver_contacts ORDER BY name ASC")
    fun getAllCaregivers(): Flow<List<CaregiverContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaregiver(contact: CaregiverContact): Long

    @Update
    suspend fun updateCaregiver(contact: CaregiverContact)

    @Delete
    suspend fun deleteCaregiver(contact: CaregiverContact)

    // Chat Messages Persistence
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()
}
