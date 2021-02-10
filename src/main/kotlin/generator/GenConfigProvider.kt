package generator

import com.beust.klaxon.FieldRenamer
import com.beust.klaxon.Klaxon
import java.io.File
import kotlin.random.Random

/**
 * Object reading and writing .json config files. Uses [Klaxon] for parsing to
 * and from .json
 */
object GenConfigProvider {

    private const val CONFIG_PATH = "../generator"
    private const val SAVE_PATH = "measurement.json"
    private const val GEN_STATE_PATH = "gen_state.json"

    private val renamer = object : FieldRenamer {
        override fun toJson(fieldName: String) = FieldRenamer.camelToUnderscores(fieldName)
        override fun fromJson(fieldName: String) = FieldRenamer.underscoreToCamel(fieldName)
    }

    private val klaxon = Klaxon().fieldRenamer(renamer)

    private fun drawConfigFile(id: Int): String = when (id) {
        0 -> "human1.json"
        else -> "human2.json"
    }

    /**
     * Tries to read list of [Activity] objects from .json file
     * @param id index of config file to load, default random
     */
    fun getHumanData(id: Int = Random.Default.nextInt(2)): List<Activity> {
        val resource = "$CONFIG_PATH/${drawConfigFile(id)}"
        val configFile = try {
            javaClass.getResource(resource).readText(Charsets.UTF_8)
        } catch (all: Exception) {
            throw RuntimeException("Failed to load resource=$resource!", all)
        }
        return klaxon.parseArray(configFile) ?: emptyList()
    }

    fun getGenState(): GenState {
        val configFile = try {
            File(GEN_STATE_PATH).readText(Charsets.UTF_8)
        } catch (all: Exception) {
            return GenState()
        }
        return klaxon
            .fieldConverter(KlaxonDate::class, dateConverter)
            .parse(configFile) ?: GenState()
    }

    /**
     * Overwrites file "measurement.json" with values from [measurement]
     * @param measurement values to save
     */
    fun saveMeasurement(measurement: Measurement) {
        val jsonString = klaxon.toJsonString(measurement)
        File(SAVE_PATH).apply {
            createNewFile()
        }.also {
            it.writeText(jsonString)
        }
    }

    /**
     * Overwrites file "gen_state.json" with values from [state]
     * @param state values to save
     */
    fun saveGenState(state: GenState) {
        val jsonString = klaxon.toJsonString(state)
        File(GEN_STATE_PATH).apply {
            createNewFile()
        }.also {
            it.writeText(jsonString)
        }
    }

    fun alarmFromMeasurement(measurement: Measurement): Alarm {
        return Alarm(uid = measurement.uid, time = measurement.time, temp = measurement.temp, pulse = measurement.pulse)
    }

    fun serialize(measurement: Measurement): String {
        return klaxon.toJsonString(measurement)
    }

    fun serialize(alarm: Alarm): String {
        return klaxon.toJsonString(alarm)
    }
}