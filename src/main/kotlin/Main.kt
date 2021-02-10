import generator.GenConfigProvider
import generator.Generator
import generator.delayLoop
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

object Main {
    private val aws = AWS()

    private const val topic = "/smartband"
    private val humanData = GenConfigProvider.getHumanData()
    private val genState = GenConfigProvider.getGenState()
    private val generator = Generator(genState, humanData)
    private val measurement = generator.measurement.apply { uid = "user2" }
    private var input = ""

    private val stateString =
        """
        activity: ${genState.lastActivityName}
        sex: ${genState.sex}
        """.trimIndent()

    private val helpString =
        """
        alarm - send alarm
        save - save measurement to .json
        display - show measurements each 2 s
        stop - stop display
        state -  show generator state
        """.trimIndent()

    private fun alarm() {
        val alarm = GenConfigProvider.alarmFromMeasurement(measurement)
        val msg = GenConfigProvider.serialize(alarm)
        aws.publish(topic, msg)
        println("published alarm")
    }

    private suspend fun display() = delayLoop(2000, { input != "stop" }) {
        measurement.mutex.withLock {
            print("$measurement\n> ")
        }
    }

    private suspend fun publish() = delayLoop(5000) {
        measurement.mutex.withLock {
            GenConfigProvider.saveMeasurement(measurement)
            val msg = GenConfigProvider.serialize(measurement)
            aws.publish(topic, msg)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {

        val generatorJob: Job = launch { generator.start() }
        val publishJob: Job = launch { publish() }
        var displayJob: Job? = null

        while (input != "exit") {
            when (input) {
                "alarm" -> alarm()
                "save" -> GenConfigProvider.saveMeasurement(measurement)
                "display" -> displayJob = launch { display() }
                "state" -> println(stateString)
                "help" -> println(helpString)
            }

            withContext(Dispatchers.IO) {
                print("> ")
                input = readLine() ?: ""
            }
        }

        displayJob?.cancelAndJoin()
        publishJob.cancelAndJoin()
        generatorJob.cancelAndJoin()

        println("Done")
    }
}