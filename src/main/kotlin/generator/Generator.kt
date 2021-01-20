package generator

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class Generator(private val data: List<Activity>) {
    private var time: Long = 0
    private lateinit var activity: Activity
    private lateinit var pedometer: Pedometer
    private lateinit var sensors: Array<Job>

    val measurement = Measurement()

    private fun selectActivity(activityId: Int?) {
        val id = activityId ?: Random().nextInt(data.size)
        activity = data[id]
        println(activity.name)

        pedometer = Pedometer(
            measurement,
            activity.stepsMod
        )
        //activities will last for ~15-45 min
        time = ((Random().nextGaussian() * 15 + 30) * 60 * 1e3).toLong()
    }

    suspend fun start(activityId: Int? = null) {
        println("starting generator")
        val mutex = measurement.mutex
        while (true) {
            selectActivity(activityId)
            coroutineScope {
                sensors = arrayOf(
                    launch { Sensor(activity.temp, measurement::temp.setter, mutex).start() },
                    launch { Sensor(activity.pulse, measurement::pulse.setter, mutex).start() },
                    launch { AccelSensor(activity.accelX, pedometer::accelX.setter, mutex).start() },
                    launch { AccelSensor(activity.accelY, pedometer::accelY.setter, mutex).start() },
                    launch { AccelSensor(activity.accelZ, pedometer::accelZ.setter, mutex).start() }
                )
            }
            delay(time)
            sensors.forEach { it.cancel() }
        }
    }
}