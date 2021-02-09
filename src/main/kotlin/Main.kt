import generator.GenConfigProvider
import generator.GenState
import generator.Generator
import generator.loop
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime
import java.time.ZoneOffset

fun main() = runBlocking {
    val aws = AWS()

    val topic = "/smartband"
    val humanData = GenConfigProvider.getHumanData()
    val genState = GenConfigProvider.getGenState() ?: GenState()
    val generator = Generator(genState, humanData!!)
    val measurement = generator.measurement


    measurement.uid = "user2"

    val generatorJob: Job = launch { generator.start() }
    val alarmJob: Job = launch {
        delay(2000)
        val alarm = GenConfigProvider.alarmFromMeasurement(measurement)
        val msg = GenConfigProvider.serialize(alarm)
        aws.publish(topic, msg)
        println("published alarm")
    }
    val printlnJob: Job = launch {
        loop(1000) {
            measurement.mutex.withLock {
                println(measurement)
            }
        }
    }
    val publishJob: Job = launch {
        loop(1000) {
            measurement.mutex.withLock {
                GenConfigProvider.saveMeasurement(measurement)
                val msg = GenConfigProvider.serialize(measurement)
                measurement.time = LocalDateTime.now(ZoneOffset.UTC).toString()
                aws.publish(topic, msg)
            }
        }
    }

    delay(30000)
    alarmJob.cancelAndJoin()
    printlnJob.cancelAndJoin()
    publishJob.cancelAndJoin()
    generatorJob.cancelAndJoin()

    println("Done")
}