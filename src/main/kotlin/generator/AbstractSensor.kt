package generator

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class AbstractSensor<SD>(
    protected val data: SD,
    private val frequency: Double,
    private val setter: (Double) -> Unit,
    private val mutex: Mutex
) {

    protected abstract fun generateValue(): Double

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
