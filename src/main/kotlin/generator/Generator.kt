package generator

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.properties.Delegates

class Generator(private val data: HumanData) {
    private var startTime by Delegates.notNull<Long>()
    private var endTime by Delegates.notNull<Long>()
    private var currentActivity by Delegates.notNull<Activity>()

    val measurementMutex = Mutex()
    var measurement by Delegates.notNull<Measurement>()

    private fun selectActivity() {
        val id = Random().nextInt(data.activities.size)
        currentActivity = data.activities[id]
        println(currentActivity.name)

        startTime = System.nanoTime()
        //activities will last for ~15-45 min
        endTime = startTime + ((Random().nextGaussian() * 15 + 30) * 1e9).toLong()
    }

    private fun generateValue(params: SensorData): Double {
        val mean = params.mean
        val std = params.std
        return Random().nextGaussian() * std + mean
    }

    private suspend fun takeMeasure() {
        if (System.nanoTime() > endTime) selectActivity()

        val params = currentActivity.hand["temp"]!!
        val temp = generateValue(params)

        measurementMutex.withLock {
            measurement = Measurement(temp)
        }

        delay((1000.0 / data.frequency).toLong())
    }

    suspend fun start() {
        selectActivity()
        while (true) takeMeasure()
    }
}