package com.example.data.ppg

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

data class PpgFrameResult(
    val redIntensity: Float,
    val isFingerCoveringLens: Boolean,
    val estimatedBpm: Int?,
    val signalQualityPercentage: Int,
    val rawTimestamp: Long
)

class PpgPulseAnalyzer(
    private val onFrameProcessed: (PpgFrameResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val sampleHistory = ArrayDeque<Pair<Long, Float>>() // timestamp to intensity
    private val peakTimestamps = ArrayDeque<Long>()
    private val maxHistoryDurationMs = 12000L // 12 seconds buffer
    private var lastCalculatedBpm: Int? = null

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val now = System.currentTimeMillis()
        try {
            val planes = image.planes
            if (planes.isEmpty()) {
                image.close()
                return
            }

            val yBuffer: ByteBuffer = planes[0].buffer
            val uBuffer: ByteBuffer? = if (planes.size > 1) planes[1].buffer else null
            val vBuffer: ByteBuffer? = if (planes.size > 2) planes[2].buffer else null

            // Sample center 25% region of frame
            val width = image.width
            val height = image.height
            val startX = (width * 0.35).toInt()
            val endX = (width * 0.65).toInt()
            val startY = (height * 0.35).toInt()
            val endY = (height * 0.65).toInt()

            var totalRed = 0.0
            var pixelCount = 0
            val yRowStride = planes[0].rowStride
            val yPixelStride = planes[0].pixelStride

            val vRowStride = if (vBuffer != null && planes.size > 2) planes[2].rowStride else yRowStride
            val vPixelStride = if (vBuffer != null && planes.size > 2) planes[2].pixelStride else 1

            // Sample pixels in steps of 4 for speed
            for (y in startY until endY step 4) {
                for (x in startX until endX step 4) {
                    val yIndex = y * yRowStride + x * yPixelStride
                    if (yIndex < yBuffer.remaining()) {
                        val yVal = (yBuffer.get(yIndex).toInt() and 0xFF)
                        var vVal = 128
                        if (vBuffer != null && planes.size > 2) {
                            val vIndex = (y / 2) * vRowStride + (x / 2) * vPixelStride
                            if (vIndex < vBuffer.remaining()) {
                                vVal = (vBuffer.get(vIndex).toInt() and 0xFF)
                            }
                        }
                        // Approximate Red in YUV: R = Y + 1.402 * (V - 128)
                        val r = (yVal + 1.402 * (vVal - 128)).coerceIn(0.0, 255.0)
                        totalRed += r
                        pixelCount++
                    }
                }
            }

            val avgRed = if (pixelCount > 0) (totalRed / pixelCount).toFloat() else 0f

            // Finger detection: red intensity usually high (> 110) under torch
            val isFingerCovering = avgRed > 60f

            if (isFingerCovering) {
                sampleHistory.addLast(Pair(now, avgRed))

                // Purge old samples
                while (sampleHistory.isNotEmpty() && now - sampleHistory.first().first > maxHistoryDurationMs) {
                    sampleHistory.removeFirst()
                }

                if (sampleHistory.size > 30) {
                    val smoothedBpm = calculateBpmFromHistory()
                    if (smoothedBpm != null && smoothedBpm in 40..210) {
                        lastCalculatedBpm = smoothedBpm
                    }
                }
            } else {
                sampleHistory.clear()
                peakTimestamps.clear()
            }

            val quality = if (isFingerCovering) {
                val sampleCount = sampleHistory.size
                min(100, max(20, (sampleCount * 2.5).toInt()))
            } else {
                0
            }

            onFrameProcessed(
                PpgFrameResult(
                    redIntensity = avgRed,
                    isFingerCoveringLens = isFingerCovering,
                    estimatedBpm = if (quality > 40) lastCalculatedBpm else null,
                    signalQualityPercentage = quality,
                    rawTimestamp = now
                )
            )
        } catch (e: Exception) {
            // Graceful fallback
        } finally {
            image.close()
        }
    }

    private fun calculateBpmFromHistory(): Int? {
        if (sampleHistory.size < 25) return null

        val values = sampleHistory.map { it.second }
        val timestamps = sampleHistory.map { it.first }

        // Compute local moving average
        val windowSize = 5
        val smoothed = FloatArray(values.size)
        for (i in values.indices) {
            val start = max(0, i - windowSize / 2)
            val end = min(values.size, i + windowSize / 2 + 1)
            var sum = 0f
            for (j in start until end) sum += values[j]
            smoothed[i] = sum / (end - start)
        }

        // Peak detection with minimum distance (250ms -> 240 bpm max)
        val detectedPeaks = mutableListOf<Long>()
        val minIntervalMs = 280L

        for (i in 2 until smoothed.size - 2) {
            if (smoothed[i] > smoothed[i - 1] && smoothed[i] > smoothed[i - 2] &&
                smoothed[i] > smoothed[i + 1] && smoothed[i] > smoothed[i + 2]
            ) {
                val peakTime = timestamps[i]
                if (detectedPeaks.isEmpty() || peakTime - detectedPeaks.last() >= minIntervalMs) {
                    detectedPeaks.add(peakTime)
                }
            }
        }

        if (detectedPeaks.size < 3) return null

        // Compute intervals between consecutive peaks
        val intervals = mutableListOf<Long>()
        for (i in 1 until detectedPeaks.size) {
            val interval = detectedPeaks[i] - detectedPeaks[i - 1]
            if (interval in 300..1500) { // between 40 bpm and 200 bpm
                intervals.add(interval)
            }
        }

        if (intervals.isEmpty()) return null

        val averageIntervalMs = intervals.average()
        if (averageIntervalMs <= 0) return null

        val rawBpm = (60000.0 / averageIntervalMs).toInt()
        return rawBpm.coerceIn(40, 200)
    }
}
