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

    override fun generateValue(params: AccelSensorData): Double {
        val mean: Double
        val std: Double
        if (previousValue == 0.0) {
            mean = params.mean
            std = params.std
        } else when {
            previousValue < lowerEnd -> {
                mean = params.incMean
                std = params.incStd
            }
            previousValue > upperEnd -> {
                mean = params.decMean
                std = params.decStd
            }
            else -> if (Random.Default.nextBoolean()) {
                mean = params.incMean
                std = params.incStd
            } else {
                mean = params.decMean
                std = params.decStd
            }
        }

        previousValue += Random.Default.nextDouble(mean - std, mean + std)
        return previousValue
    }
}