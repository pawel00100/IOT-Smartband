package generator.sensor

import generator.delayLoop
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Sensor(
    gen: ISensor,
    private val setter: (Double) -> Unit,
    private val mutex: Mutex
) : ISensor by gen {

    suspend fun start() = delayLoop((1000.0 / frequency).toLong()) {
        mutex.withLock {
            setter(generateValue())
        }
    }
}
