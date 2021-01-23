package generator.sensor

import generator.SensorData
import java.util.*

class NormalSensor(
    private val data: SensorData
) : ISensor {

    override val frequency: Double
        get() = data.frequency

    override fun generateValue(): Double {
        val mean = data.mean
        val std = data.std
        return Random().nextGaussian() * std + mean
    }
}