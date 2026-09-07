package com.asatir.passiveradar.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asatir.passiveradar.data.model.RadarResult
import com.asatir.passiveradar.data.model.SensorData
import java.text.DecimalFormat

@Composable
fun RadarScreen(
    radarResult: RadarResult?,
    sensorData: SensorData,
    latitude: Double?,
    longitude: Double?,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onCaptureClick: () -> Unit
) {
    val decimalFormat = DecimalFormat("#.####")
    var isScanning by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // هدر
        Text(
            "📡 فلزیاب راداری منفعل",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // دکمه‌های کنترلی
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    onStartClick()
                    isScanning = true
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("▶ شروع")
            }

            Button(
                onClick = {
                    onStopClick()
                    isScanning = false
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("⏹ توقف")
            }

            Button(
                onClick = { onCaptureClick() },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("📸")
            }
        }

        if (isScanning) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            )
        }

        // نتایج راداری
        radarResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📊 نتایج تحلیل", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    SensorReadingRow(
                        label = "Entropy",
                        value = decimalFormat.format(result.entropy),
                        status = if (result.entropy > 0.04f) "✅" else "⚠️"
                    )

                    SensorReadingRow(
                        label = "Dielectric",
                        value = decimalFormat.format(result.dielectric),
                        status = if (result.dielectric > 0.08f) "✅" else "⚠️"
                    )

                    SensorReadingRow(
                        label = "Fractal",
                        value = decimalFormat.format(result.fractal),
                        status = if (result.fractal < 0.9f) "✅" else "⚠️"
                    )

                    SensorReadingRow(
                        label = "RMS",
                        value = decimalFormat.format(result.rms),
                        status = if (result.rms > 0.02f) "✅" else "⚠️"
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    SensorReadingRow(
                        label = "تشخیص",
                        value = if (result.isDetected) "فلز یافت شد" else "فلز یافت نشد",
                        status = if (result.isDetected) "🎯" else "❌"
                    )

                    SensorReadingRow(
                        label = "نوع مادی",
                        value = result.materialType,
                        status = ""
                    )

                    SensorReadingRow(
                        label = "دقت",
                        value = "${(result.confidence * 100).toInt()}%",
                        status = ""
                    )
                }
            }
        }

        // داده‌های سنسور
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📍 سنسورهای دستگاه", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                SensorReadingRow(
                    label = "شتاب X",
                    value = decimalFormat.format(sensorData.accelerometerX),
                    status = ""
                )

                SensorReadingRow(
                    label = "شتاب Y",
                    value = decimalFormat.format(sensorData.accelerometerY),
                    status = ""
                )

                SensorReadingRow(
                    label = "شتاب Z",
                    value = decimalFormat.format(sensorData.accelerometerZ),
                    status = ""
                )

                SensorReadingRow(
                    label = "دما",
                    value = "${decimalFormat.format(sensorData.temperature)}°C",
                    status = ""
                )

                SensorReadingRow(
                    label = "فشار",
                    value = "${decimalFormat.format(sensorData.pressure)} hPa",
                    status = ""
                )

                SensorReadingRow(
                    label = "رطوبت",
                    value = "${decimalFormat.format(sensorData.humidity)}%",
                    status = ""
                )
            }
        }

        // موقعیت جغرافیایی
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🗺️ موقعیت جغرافیایی", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                SensorReadingRow(
                    label = "عرض جغرافیایی",
                    value = latitude?.let { decimalFormat.format(it) } ?: "بدست می‌آید...",
                    status = if (latitude != null) "✅" else "⏳"
                )

                SensorReadingRow(
                    label = "طول جغرافیایی",
                    value = longitude?.let { decimalFormat.format(it) } ?: "بدست می‌آید...",
                    status = if (longitude != null) "✅" else "⏳"
                )
            }
        }
    }
}

@Composable
fun SensorReadingRow(label: String, value: String, status: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(value, style = MaterialTheme.typography.bodyMedium)
            Text(status)
        }
    }
}
