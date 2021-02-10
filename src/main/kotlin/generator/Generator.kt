package generator

import generator.sensor.AccelSensor
import generator.sensor.NormalSensor
import generator.sensor.Sensor
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.*

/**
 * Class in control of coroutines imitating sensors
 *
 * @param data list of activities containing parameters for sensors
 * @property measurement object containing generated values
 */
class Generator(private val genState: GenState, private val data: List<Activity>) {
    private var time: Long = 0
    private lateinit var activity: Activity
    private lateinit var pedometer: Pedometer
    private lateinit var sensors: Array<Job>

    val measurement = Measurement()

    private fun activityTime(name: String): Long = when (name) {
        "lying" -> //~5-9 hours
            (Random().nextGaussian() * 120 + 7 * 60) * 60 * 1e3
        "walking", "cycling", "nordic_walking" -> //~40-100 min
            (Random().nextGaussian() * 20 + 60) * 60 * 1e3
        else -> // ~15-45 min
            (Random().nextGaussian() * 15 + 30) * 60 * 1e3
    }.toLong()

    private fun selectActivity(activityId: Int?) {
        activity = if (LocalDateTime.now().hour in 5..23) {
            val id = activityId ?: Random().nextInt(data.size - 1)
            data.filterNot { it.name == genState.lastActivityName }[id]
        } else {
            data.first { it.name == "lying" }
        }

        if (genState.checkOvulation()) activity.temp += ovulationTemp
        if (Random().nextInt(1000) == 999) activity.pulse = cardiacArrest

        pedometer = Pedometer(
            measurement,
            activity.stepsMod
        )

        time = activityTime(activity.name)
        genState.lastActivityName = activity.name
        GenConfigProvider.saveGenState(genState)
    }

    /**
     * Chooses activity from [data] and starts coroutines
     * imitating temperature, pulse and accelerometer sensors
     * @param activityId optional index of activity to chose
     */
    suspend fun start(activityId: Int? = null) {
        println("Starting generator")
        val mutex = measurement.mutex
        while (true) {
            selectActivity(activityId)
            coroutineScope {
                sensors = arrayOf(
                    launch { Sensor(NormalSensor(activity.temp), measurement::temp.setter, mutex).start() },
                    launch { Sensor(NormalSensor(activity.pulse), measurement::pulse.setter, mutex).start() },
                    launch { Sensor(AccelSensor(activity.accelX), pedometer::accelX.setter, mutex).start() },
                    launch { Sensor(AccelSensor(activity.accelY), pedometer::accelY.setter, mutex).start() },
                    launch { Sensor(AccelSensor(activity.accelZ), pedometer::accelZ.setter, mutex).start() }
                )
                delay(time)
                sensors.forEach { it.cancel() }
            }
        }
    }
}