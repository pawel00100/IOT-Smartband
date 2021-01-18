package generator

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.util.*

class Generator(private val data: List<Activity>) {
    private var time: Long = 0
    private lateinit var currentActivity: Activity
    private lateinit var sensors: Array<Job>

    val measurement = Measurement()
    val mutex = Mutex()

    private fun selectActivity() {
        val id = Random().nextInt(data.size)
        currentActivity = data[id]
        println(currentActivity.name)

        //activities will last for ~15-45 min
        time = ((Random().nextGaussian() * 15 + 30) * 60 * 1e3).toLong()
    }

    suspend fun start() {
        println("starting generator")
        while (true) {
            selectActivity()
            coroutineScope {
                sensors = arrayOf(
                    launch { Sensor(currentActivity.temp, measurement::temp.setter).start() },
                    launch { Sensor(currentActivity.pulse, measurement::pulse.setter).start() },
                    launch { Sensor(currentActivity.accelX, measurement::accelX.setter).start() },
                    launch { Sensor(currentActivity.accelY, measurement::accelY.setter).start() },
                    launch { Sensor(currentActivity.accelZ, measurement::accelZ.setter).start() }
                )
            }
            delay(time)
            sensors.forEach { it.cancel() }
        }
    }
}