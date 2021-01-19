package generator

import com.beust.klaxon.Json

data class Activity(
    @Json(name = "activity")
    val name: String,
    val temp: SensorData,
    val pulse: SensorData,
    val accelX: SensorData,
    val accelY: SensorData,
    val accelZ: SensorData
)

data class SensorData(
    val frequency: Double,
    val mean: Double,
    val std: Double
)

