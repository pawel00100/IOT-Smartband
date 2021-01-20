import generator.ConfigReader
import generator.Generator
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock

fun main() = runBlocking {
    val humanData = ConfigReader.getHumanData()
    val generator = Generator(humanData!!)
    coroutineScope {
        launch { generator.start() }
        launch {
            while (true) {
                delay(1000)
                generator.mutex.withLock {
                    println(generator.measurement)
                }
            }
        }
    }

    println("Done")
}