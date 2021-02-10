package generator

import com.beust.klaxon.Json
import kotlinx.coroutines.sync.Mutex
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.random.Random

data class Activity(
    @Json(name = "activity")
    val name: String,
    val stepsMod: Double,
    var temp: SensorData,
    var pulse: SensorData,
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


data class Measurement(
    var uid: String = "not set",
    var time: String = LocalDateTime.now().toString(),
    var temp: Double = 0.0,
    var pulse: Double = 0.0,
    var steps: Int = 0
) {
    private val _mutex = Mutex()
    @Json(ignored = true)
    val mutex: Mutex
        get() {
            time = LocalDateTime.now(ZoneOffset.UTC).toString()
            return _mutex
        }
}

data class Alarm(
    var uid: String = "not set",
    var time: String = LocalDateTime.now().toString(),
    var temp: Double = 0.0,
    var pulse: Double = 0.0,
    val alarm: Boolean = true
)
