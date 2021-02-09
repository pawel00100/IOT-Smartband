import generator.ConfigReader
import generator.Generator
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime
import java.time.ZoneOffset

fun main() = runBlocking {
    val aws = AWS()

    val topic = "/smartband"
    val humanData = ConfigReader.getHumanData()
    val generator = Generator(humanData!!)
    val measurement = generator.measurement
    var generatorJob: Job
    var printlnJob: Job
    var alarmJob: Job


    measurement.uid = "user2"

    coroutineScope {
        alarmJob = launch {
            delay(2000)
            val alarm = ConfigReader.alarmFromMeasurement(measurement)
            val msg = ConfigReader.serialize(alarm)
            aws.publish(topic, msg)
            println("published alarm")
        }
        generatorJob = launch { generator.start() }
        printlnJob = launch {
            while (true) {
                delay(1000)
                measurement.mutex.withLock {
                    println(measurement)
                    ConfigReader.saveMeasurement(measurement)
                    val msg = ConfigReader.serialize(measurement)
                    measurement.time = LocalDateTime.now(ZoneOffset.UTC).toString()
                    aws.publish(topic, msg)
                }
            }
        }
        delay(30000)
        alarmJob.cancelAndJoin()
        printlnJob.cancelAndJoin()
        generatorJob.cancelAndJoin()
    }
    println("Done")
}