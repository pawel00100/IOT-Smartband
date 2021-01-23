import generator.ConfigReader
import generator.Generator
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

fun main() = runBlocking {
    val humanData = ConfigReader.getHumanData()
    val generator = Generator(humanData!!)
    val measurement = generator.measurement
    var generatorJob: Job
    var printlnJob: Job
    coroutineScope {
        generatorJob = launch { generator.start() }
        printlnJob = launch {
            while (true) {
                delay(1000)
                measurement.mutex.withLock {
                    println(measurement)
                    ConfigReader.saveMeasurement(measurement)
                }
            }
        }
        delay(60000)
        printlnJob.cancelAndJoin()
        generatorJob.cancelAndJoin()
    }
    println("Done")
}