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

data class Measurement(
    var temp: Double = 0.0,
    var pulse: Double = 0.0,
    var accelX: Double = 0.0,
    var accelY: Double = 0.0,
    var accelZ: Double = 0.0
)