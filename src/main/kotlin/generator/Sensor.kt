package generator

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class Sensor(
    private val data: SensorData,
    private val setter: (Double) -> Unit,
    private val mutex: Mutex
) {

    private fun generateValue(params: SensorData): Double {
        val mean = params.mean
        val std = params.std
        return Random().nextGaussian() * std + mean
    }

    private suspend fun takeMeasurement() {
        val measurement = generateValue(data)

        mutex.withLock {
            setter(measurement)
        }

        delay((1000.0 / data.frequency).toLong())
    }

    suspend fun start() {
        while (true) takeMeasurement()
    }
}