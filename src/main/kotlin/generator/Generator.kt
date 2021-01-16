package generator

import kotlinx.coroutines.delay
import java.util.*
import kotlin.properties.Delegates

class Generator(private val data: HumanData) {
    private var startTime by Delegates.notNull<Long>()
    private var endTime by Delegates.notNull<Long>()
    private var currentActivity by Delegates.notNull<Activity>()

    init {
        selectActivity()
    }

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

    suspend fun takeMeasure(): Measurement {
        if (System.nanoTime() > endTime) selectActivity()
        delay((1000.0 / data.frequency).toLong())

        val params = currentActivity.hand["temp"]!!
        val temp = generateValue(params)
        return Measurement(temp)
    }
}