package generator

val cardiacArrest = SensorData(frequency = 0.1, mean = 70.0, std = 50.0)

val ovulationTemp = SensorData(frequency = 100.0, mean = 0.5, std = 0.05)

suspend fun loop(delay: Long, func: (suspend () -> Unit)) {
    while (true) {
        kotlinx.coroutines.delay(delay)
        func()
    }
}

operator fun SensorData.plus(other: SensorData): SensorData = SensorData(
    frequency + other.frequency,
    mean + other.mean,
    std + other.std
)