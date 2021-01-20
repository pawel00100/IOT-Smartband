package generator

import com.beust.klaxon.Json

data class Activity(
    @Json(name = "activity")
    val name: String,
    val temp: SensorData,
    val pulse: SensorData,
    val accelX: AccelSensorData,
    val accelY: AccelSensorData,
    val accelZ: AccelSensorData
)

data class SensorData(
    val frequency: Double,
    val mean: Double,
    val std: Double
)

data class AccelSensorData(
    val frequency: Double,
    val mean: Double,
    val std: Double,
    val decMean: Double,
    val decStd: Double,
    val incMean: Double,
    val incStd: Double
)


