package generator

import kotlinx.coroutines.sync.Mutex
import java.util.*

class Sensor(
    data: SensorData,
    setter: (Double) -> Unit,
    mutex: Mutex
) : AbstractSensor<SensorData>(data, data.frequency, setter, mutex) {

    override fun generateValue(): Double {
        val mean = data.mean
        val std = data.std
        return Random().nextGaussian() * std + mean
    }
}