package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val isGoogleAuthenticated: Boolean = true,
    val isCaregiverAlertConsentGiven: Boolean = true,
    val bloodType: String = "O+",
    val age: Int = 32
)

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("healthpulse_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    init {
        loadPersistedUser()
    }

    private fun loadPersistedUser() {
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        if (isLoggedIn) {
            val name = prefs.getString("user_name", "User") ?: "User"
            val email = prefs.getString("user_email", "user@gmail.com") ?: "user@gmail.com"
            val photoUrl = prefs.getString("photo_url", null)
            val isGoogle = prefs.getBoolean("is_google_auth", true)
            val bloodType = prefs.getString("blood_type", "O+") ?: "O+"
            val age = prefs.getInt("age", 32)
            val userId = prefs.getString("user_id", "usr_${System.currentTimeMillis()}") ?: "usr_1"

            _currentUser.value = UserProfile(
                id = userId,
                name = name,
                email = email,
                photoUrl = photoUrl,
                isGoogleAuthenticated = isGoogle,
                bloodType = bloodType,
                age = age
            )
        } else {
            // Default to logged in as Google user if first launch
            val defaultEmail = "carothom12@gmail.com"
            val defaultName = "Caro Thom"
            val userId = "usr_google_${System.currentTimeMillis() % 100000}"
            val profile = UserProfile(
                id = userId,
                name = defaultName,
                email = defaultEmail,
                photoUrl = null,
                isGoogleAuthenticated = true,
                bloodType = "O+",
                age = 32
            )
            persistUser(profile)
            _currentUser.value = profile
        }
    }

    fun signInWithGoogle(email: String = "carothom12@gmail.com", name: String = "Caro Thom") {
        val userId = "usr_google_${System.currentTimeMillis() % 100000}"
        val profile = UserProfile(
            id = userId,
            name = if (name.isNotBlank()) name else "Google User",
            email = if (email.isNotBlank()) email else "user@gmail.com",
            photoUrl = null,
            isGoogleAuthenticated = true,
            bloodType = prefs.getString("blood_type", "O+") ?: "O+",
            age = prefs.getInt("age", 32)
        )
        persistUser(profile)
        _currentUser.value = profile
    }

    fun signInAsGuest() {
        val userId = "usr_guest_${System.currentTimeMillis() % 10000}"
        val profile = UserProfile(
            id = userId,
            name = "Guest Patient",
            email = "guest@healthpulse.local",
            photoUrl = null,
            isGoogleAuthenticated = false,
            bloodType = "O+",
            age = 30
        )
        persistUser(profile)
        _currentUser.value = profile
    }

    fun signOut() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .apply()
        _currentUser.value = null
    }

    fun updateProfile(name: String, email: String, bloodType: String, age: Int) {
        val current = _currentUser.value ?: UserProfile(
            id = "usr_${System.currentTimeMillis() % 10000}",
            name = name,
            email = email,
            bloodType = bloodType,
            age = age
        )
        val updated = current.copy(name = name, email = email, bloodType = bloodType, age = age)
        persistUser(updated)
        _currentUser.value = updated
    }

    private fun persistUser(profile: UserProfile) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_id", profile.id)
            .putString("user_name", profile.name)
            .putString("user_email", profile.email)
            .putString("photo_url", profile.photoUrl)
            .putBoolean("is_google_auth", profile.isGoogleAuthenticated)
            .putString("blood_type", profile.bloodType)
            .putInt("age", profile.age)
            .apply()
    }
}
