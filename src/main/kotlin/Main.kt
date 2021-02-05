import generator.ConfigReader
import generator.Generator
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime
import java.time.ZoneOffset

fun main() = runBlocking {
    val aws = AWS()

    val humanData = ConfigReader.getHumanData()
    val generator = Generator(humanData!!)
    val measurement = generator.measurement
    var generatorJob: Job
    var printlnJob: Job

//    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    measurement.uid = "user2"

    coroutineScope {
        generatorJob = launch { generator.start() }
        printlnJob = launch {
            while (true) {
                delay(1000)
                measurement.mutex.withLock {
                    println(measurement)
                    ConfigReader.saveMeasurement(measurement)
                    val msg = ConfigReader.serialize(measurement)
//                    measurement.time = LocalDateTime.now(ZoneOffset.UTC).format(formatter)
                    measurement.time = LocalDateTime.now(ZoneOffset.UTC).toString()
                    aws.publish("/smartband", msg)
                }
            }
        }
        delay(30000)
        printlnJob.cancelAndJoin()
        generatorJob.cancelAndJoin()
    }
    println("Done")
}