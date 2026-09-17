package com.example.data.remote

import com.example.BuildConfig
import com.example.data.local.entity.VitalRecord
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GeminiChatRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiChatResponse(
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content: GeminiContent?
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiChatRequest
    ): GeminiChatResponse
}

class GeminiHealthService {
    private val api: GeminiApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    suspend fun getHealthAdvice(
        userMessage: String,
        conversationHistory: List<Pair<String, Boolean>>, // text to isUser
        recentVitals: List<VitalRecord> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val vitalsSummary = if (recentVitals.isNotEmpty()) {
            val vitalsList = recentVitals.take(5).joinToString(", ") {
                val sec = if (it.valueSecondary != null) "/${it.valueSecondary.toInt()}" else ""
                "${it.type.displayName}: ${it.valuePrimary.toInt()}$sec ${it.unit} (${it.severity.levelName})"
            }
            "Current patient vitals context: $vitalsList."
        } else {
            "No recent vital signs recorded yet."
        }

        val systemPrompt = "You are HealthPulse Assistant, a warm, authentic, caring, and naturally conversational personal health companion. " +
                "You speak in a genuinely human, thoughtful, and friendly tone, just like a caring doctor or knowledgeable health advisor chatting with a friend. " +
                "CRITICAL FORMATTING RULES: " +
                "1. NEVER use any asterisks (* or **), markdown bolding, markdown bullet points with asterisks, headers (#), or robotic AI markdown lists in your output. " +
                "2. Write in smooth, natural, human paragraphs and full conversational sentences. " +
                "3. If giving tips or steps, present them conversationally (such as: 'First...', 'Next...', 'Also...') without bullet asterisks. " +
                "4. Be empathetic, practical, and easy to understand. Never diagnose conditions or prescribe medications; gently advise consulting a healthcare professional for persistent or critical symptoms. " +
                "$vitalsSummary"

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val contentsList = mutableListOf<GeminiContent>()

                // Add past 4 conversation messages for context
                val recentHistory = conversationHistory.takeLast(4)
                for ((msg, isUser) in recentHistory) {
                    contentsList.add(
                        GeminiContent(
                            role = if (isUser) "user" else "model",
                            parts = listOf(GeminiPart(text = msg))
                        )
                    )
                }

                // Add current message
                contentsList.add(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = userMessage))
                    )
                )

                val request = GeminiChatRequest(
                    contents = contentsList,
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = systemPrompt))
                    )
                )

                val response = api.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!responseText.isNullOrBlank()) {
                    return@withContext cleanAndHumanizeResponse(responseText)
                }
            } catch (e: Exception) {
                // Fallback to offline rule-based knowledge engine
            }
        }

        // Offline conversational health intelligence engine
        return@withContext cleanAndHumanizeResponse(generateOfflineWarmHealthResponse(userMessage, recentVitals))
    }

    private fun cleanAndHumanizeResponse(rawText: String): String {
        return rawText
            // Remove bold markdown (**text** or __text__)
            .replace(Regex("""\*\*(.*?)\*\*"""), "$1")
            .replace(Regex("""__(.*?)__"""), "$1")
            // Remove italic markdown (*text* or _text_)
            .replace(Regex("""\*(.*?)\*"""), "$1")
            // Remove any remaining stray asterisks
            .replace("*", "")
            // Clean markdown headers like ### Header
            .replace(Regex("""(?m)^#{1,6}\s*"""), "")
            // Remove markdown blockquotes
            .replace(Regex("""(?m)^>\s*"""), "")
            // Remove bullet asterisks or dashes at line starts and keep clean conversational flow
            .replace(Regex("""(?m)^\s*[\*\-]\s+"""), "• ")
            // Clean up extra blank lines
            .replace(Regex("""\n{3,}"""), "\n\n")
            .trim()
    }

    private fun generateOfflineWarmHealthResponse(message: String, recentVitals: List<VitalRecord>): String {
        val lower = message.lowercase()

        return when {
            lower.contains("heart") || lower.contains("pulse") || lower.contains("bpm") -> {
                val hrRecord = recentVitals.firstOrNull { it.type == com.example.data.model.VitalType.HEART_RATE }
                val hrDetail = if (hrRecord != null) "Your latest pulse was ${hrRecord.valuePrimary.toInt()} bpm (${hrRecord.severity.levelName})." else "A normal resting heart rate for adults is typically 60 to 100 beats per minute."
                "I'm glad you're keeping an eye on your heart. $hrDetail When you measure with our camera scan, remember to sit comfortably, keep your finger still over the lens, and breathe gently. Factors like caffeine, hydration, and stress can cause temporary variations."
            }
            lower.contains("blood pressure") || lower.contains("bp") || lower.contains("hypertension") -> {
                val bpRecord = recentVitals.firstOrNull { it.type == com.example.data.model.VitalType.BLOOD_PRESSURE }
                val bpDetail = if (bpRecord != null) "Your last blood pressure check recorded ${bpRecord.valuePrimary.toInt()}/${bpRecord.valueSecondary?.toInt() ?: 80} mmHg." else "Ideal resting blood pressure is generally under 120/80 mmHg."
                "Blood pressure is such a vital indicator of your vascular health. $bpDetail Regular checks at the same time each day give the clearest picture. Make sure to rest for five minutes before measuring and avoid caffeine right beforehand."
            }
            lower.contains("oxygen") || lower.contains("spo2") -> {
                "Oxygen saturation measures how efficiently your red blood cells carry oxygen. A healthy reading is 95% or higher. If you ever feel short of breath or notice your reading dipping below 92%, it's important to rest and speak with a healthcare provider right away."
            }
            lower.contains("fever") || lower.contains("temp") || lower.contains("temperature") -> {
                "Standard body temperature is around 36.5°C to 37.2°C (97.7°F to 99°F). If you're running a temperature above 38°C (100.4°F), prioritizing rest, drinking cool electrolyte fluids, and staying in a comfortable environment will help your body recover."
            }
            lower.contains("summary") || lower.contains("how am i") || lower.contains("trends") || lower.contains("report") -> {
                if (recentVitals.isEmpty()) {
                    "You don't have any vital readings logged yet today. Taking a quick 15-second camera pulse check or logging your blood pressure is a wonderful place to start!"
                } else {
                    val count = recentVitals.size
                    val abnormals = recentVitals.count { it.severity != com.example.data.model.AlertSeverity.NORMAL }
                    if (abnormals == 0) {
                        "Looking over your $count recent readings, everything is in a stable, healthy range! Your cardiovascular and vital markers look balanced. Keep up your routine, daily hydration, and movement."
                    } else {
                        "Reviewing your $count recent checks, I noticed $abnormals reading flagged for attention. Take a look at your Alerts tab for quick guidance, take some calming deep breaths, and feel free to share your history with your care team."
                    }
                }
            }
            lower.contains("medication") || lower.contains("pill") || lower.contains("reminder") -> {
                "Staying consistent with your schedule is one of the best things you can do for your health. You can check off your daily doses directly in the Reminders tab so you never miss a beat."
            }
            lower.contains("caregiver") || lower.contains("emergency") || lower.contains("doctor") -> {
                "Your care circle is here to support you. You can set up direct emergency contacts in the Caregivers tab, where critical alert notifications can be easily forwarded to your family or physician."
            }
            lower.contains("hi") || lower.contains("hello") || lower.contains("hey") -> {
                "Hello there! I'm here to support your daily wellness journey. Whether you want to explore your recent vital trends, understand a pulse reading, or chat about your health routine, how are you feeling today?"
            }
            else -> {
                "I'm here with you every step of the way. You can ask me about your heart rate, blood pressure trends, oxygen levels, or daily reminders. How can I help you feel your best today?"
            }
        }
    }
}
