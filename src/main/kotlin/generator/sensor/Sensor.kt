package generator.sensor

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Sensor(
    gen: ISensor,
    private val setter: (Double) -> Unit,
    private val mutex: Mutex
) : ISensor by gen {

    private suspend fun takeMeasurement() {
        val measurement = generateValue()

        mutex.withLock {
            setter(measurement)
        }

        delay((1000.0 / frequency).toLong())
    }

    suspend fun start() {
        while (true) takeMeasurement()
    }
}
