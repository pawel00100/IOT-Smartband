import generator.ConfigReader
import generator.Generator
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

fun main() = runBlocking {
    val humanData = ConfigReader.getHumanData()
    val generator = Generator(humanData!!)
    coroutineScope {
        launch { generator.start() }
        launch {
            while (true) {
                delay(100)
                generator.measurementMutex.withLock {
                    println(generator.measurement)
                }
            }
        }
    }
    println("Done")
}