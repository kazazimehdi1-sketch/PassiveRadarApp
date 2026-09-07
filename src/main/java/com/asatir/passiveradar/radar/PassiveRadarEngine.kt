package com.asatir.passiveradar.radar

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.asatir.passiveradar.data.model.RadarResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.sqrt

class PassiveRadarEngine {
    private var audioRecord: AudioRecord? = null
    private var isRunning = false
    
    private val sampleRate = 192000
    private val channelConfig = AudioFormat.CHANNEL_IN_STEREO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 4

    private val _radarResult = MutableStateFlow<RadarResult?>(null)
    val radarResult: StateFlow<RadarResult?> = _radarResult

    fun startEngine() {
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                return
            }
            
            audioRecord?.startRecording()
            isRunning = true

            val buffer = ByteArray(bufferSize)
            Thread {
                while (isRunning) {
                    try {
                        val readBytes = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                        if (readBytes > 0) {
                            val floats = convertToFloat(buffer, readBytes)
                            
                            // تحلیل‌های پیشرفته
                            val entropy = computeEntropy(floats)
                            val dielectric = computeDielectric(floats)
                            val fractal = computeFractal(floats)
                            val rms = computeRMS(floats)

                            // ارزیابی شرایط تشخیص
                            val isDetected = entropy > 0.04f && 
                                           dielectric > 0.08f && 
                                           fractal < 0.9f && 
                                           rms > 0.02f

                            val confidence = calculateConfidence(entropy, dielectric, fractal, rms)
                            
                            val materialType = determineMaterialType(
                                entropy, 
                                dielectric, 
                                fractal
                            )

                            val result = RadarResult(
                                entropy = entropy,
                                dielectric = dielectric,
                                fractal = fractal,
                                rms = rms,
                                isDetected = isDetected,
                                materialType = materialType,
                                confidence = confidence
                            )
                            
                            _radarResult.value = result
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun convertToFloat(byteArray: ByteArray, size: Int): FloatArray {
        val shorts = ByteBuffer.wrap(byteArray, 0, size)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer()
        val floats = FloatArray(shorts.remaining())
        for (i in floats.indices) {
            floats[i] = shorts.get(i).toFloat() / 32768.0f
        }
        return floats
    }

    private fun computeEntropy(data: FloatArray): Float {
        if (data.isEmpty()) return 0f
        var sum = 0f
        for (v in data) {
            val p = abs(v)
            if (p > 0.001f) {
                sum -= p * ln(p.toDouble()).toFloat()
            }
        }
        return sum / data.size
    }

    private fun computeDielectric(data: FloatArray): Float {
        if (data.isEmpty()) return 0f
        var sum = 0f
        for (v in data) {
            sum += abs(v)
        }
        return sum / data.size
    }

    private fun computeFractal(data: FloatArray): Float {
        if (data.size < 2) return 0f
        var diff = 0f
        for (i in 0 until data.size - 1) {
            diff += abs(data[i + 1] - data[i])
        }
        return diff / data.size
    }

    private fun computeRMS(data: FloatArray): Float {
        if (data.isEmpty()) return 0f
        var sum = 0f
        for (v in data) {
            sum += v * v
        }
        return sqrt(sum / data.size)
    }

    private fun calculateConfidence(
        entropy: Float,
        dielectric: Float,
        fractal: Float,
        rms: Float
    ): Float {
        val entropyScore = (entropy / 0.1f).coerceIn(0f, 1f)
        val dielectricScore = (dielectric / 0.15f).coerceIn(0f, 1f)
        val fractalScore = (1f - fractal / 1.5f).coerceIn(0f, 1f)
        val rmsScore = (rms / 0.1f).coerceIn(0f, 1f)
        
        return (entropyScore + dielectricScore + fractalScore + rmsScore) / 4f
    }

    private fun determineMaterialType(
        entropy: Float,
        dielectric: Float,
        fractal: Float
    ): String {
        return when {
            dielectric > 0.15f && entropy > 0.06f -> "ساروج/بتن"
            dielectric > 0.12f -> "متریال متراکم باستانی"
            dielectric > 0.08f -> "خاک معدنی"
            fractal > 1.2f -> "فلز خالص"
            else -> "خاک عادی"
        }
    }

    fun stopEngine() {
        isRunning = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioRecord = null
    }
}
