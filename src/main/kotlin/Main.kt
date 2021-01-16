import generator.ConfigReader
import generator.Generator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val humanData = ConfigReader.getHumanData()
    val generator = Generator(humanData!!)
    val measurements = produce { while (true) send(generator.takeMeasure()) }
    repeat(10) {
        println(measurements.receive())
    }
    coroutineContext.cancelChildren()
}