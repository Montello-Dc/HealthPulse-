package com.example

import android.app.Application
import com.example.data.auth.AuthManager
import com.example.data.local.AppDatabase
import com.example.data.remote.GeminiHealthService
import com.example.data.repository.HealthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HealthPulseApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { HealthRepository(database.healthDao()) }
    val authManager by lazy { AuthManager(this) }
    val geminiService by lazy { GeminiHealthService() }

    override fun onCreate() {
        super.onCreate()
    }
}
