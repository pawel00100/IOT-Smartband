import generator.GenConfigProvider
import generator.GenState
import generator.Generator
import generator.delayLoop
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

object Main {
    private val aws = AWS()

    private const val topic = "/smartband"
    private val humanData = GenConfigProvider.getHumanData()
    private val genState = GenConfigProvider.getGenState() ?: GenState()
    private val generator = Generator(genState, humanData!!)
    private val measurement = generator.measurement.apply { uid = "user2" }
    private var input = ""

    private fun alarm() {
        val alarm = GenConfigProvider.alarmFromMeasurement(measurement)
        val msg = GenConfigProvider.serialize(alarm)
        aws.publish(topic, msg)
        println("published alarm")
    }

    private fun save() {
        GenConfigProvider.saveMeasurement(measurement)
    }

    private suspend fun display() = delayLoop(2000, { input != "stop" }) {
        measurement.mutex.withLock {
            println(measurement)
        }
        print("> ")
    }

    private suspend fun publish() = delayLoop(5000, { input != "stop" }) {
        measurement.mutex.withLock {
            GenConfigProvider.saveMeasurement(measurement)
            val msg = GenConfigProvider.serialize(measurement)
            aws.publish(topic, msg)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {

        val generatorJob: Job = launch { generator.start() }
        var saveJob: Job? = null
        var publishJob: Job? = null
        var displayJob: Job? = null

        while (input != "exit") {
            when (input) {
                "alarm" -> alarm()
                "save" -> saveJob = launch { save() }
                "publish" -> publishJob = launch { publish() }
                "display" -> displayJob = launch { display() }
            }

            withContext(Dispatchers.IO) {
                print("> ")
                input = readLine() ?: ""
            }
        }

        displayJob?.cancelAndJoin()
        saveJob?.cancelAndJoin()
        publishJob?.cancelAndJoin()
        generatorJob.cancelAndJoin()

        println("Done")
    }
}