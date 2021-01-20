package generator

import kotlinx.coroutines.sync.Mutex
import java.util.*

class Sensor(
    data: SensorData,
    setter: (Double) -> Unit,
    mutex: Mutex
) : AbstractSensor<SensorData>(data, data.frequency, setter, mutex) {

    override fun generateValue(params: SensorData): Double {
        val mean = params.mean
        val std = params.std
        return Random().nextGaussian() * std + mean
    }
}