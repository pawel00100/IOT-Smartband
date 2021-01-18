package generator

import kotlinx.coroutines.delay
import java.util.*

class Sensor(
    private val data: SensorData,
    private val setter: (Double) -> Unit
) {

    private fun generateValue(params: SensorData): Double {
        val mean = params.mean
        val std = params.std
        return Random().nextGaussian() * std + mean
    }

    private suspend fun takeMeasurement() {
        val measurement = generateValue(data)

        setter(measurement)

        delay((1000.0 / data.frequency).toLong())
    }

    suspend fun start() {
        while (true) takeMeasurement()
    }
}