import generator.ConfigReader
import generator.Generator
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

fun main() = runBlocking {
    val humanData = ConfigReader.getHumanData()
    val generator = Generator(humanData!!)
    var generatorJob: Job
    var printlnJob: Job
    coroutineScope {
        generatorJob = launch { generator.start(4) }
        printlnJob = launch {
            while (true) {
                delay(1000)
                generator.measurement.mutex.withLock {
                    println(generator.measurement)
                }
            }
        }
        delay(20000)
        printlnJob.cancelAndJoin()
        generatorJob.cancelAndJoin()
    }
    println("Done")
}