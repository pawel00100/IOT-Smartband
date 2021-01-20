package generator

import kotlinx.coroutines.sync.Mutex
import kotlin.random.Random

class AccelSensor(
    data: AccelSensorData,
    setter: (Double) -> Unit,
    mutex: Mutex
) : AbstractSensor<AccelSensorData>(data, data.frequency, setter, mutex) {

    private var previousValue: Double = 0.0
    private val upperEnd: Double = data.mean + data.std
    private val lowerEnd: Double = data.mean - data.std

    override fun generateValue(): Double {
        val mean: Double
        val std: Double
        if (previousValue == 0.0) {
            mean = data.mean
            std = data.std
        } else when {
            previousValue < lowerEnd -> {
                mean = data.incMean
                std = data.incStd
            }
            previousValue > upperEnd -> {
                mean = data.decMean
                std = data.decStd
            }
            else -> if (Random.Default.nextBoolean()) {
                mean = data.incMean
                std = data.incStd
            } else {
                mean = data.decMean
                std = data.decStd
            }
        }

        previousValue += Random.Default.nextDouble(mean - std, mean + std)
        return previousValue
    }
}