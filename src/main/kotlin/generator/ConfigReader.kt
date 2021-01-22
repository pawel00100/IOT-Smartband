package generator

import com.beust.klaxon.FieldRenamer
import com.beust.klaxon.Klaxon
import java.io.File
import kotlin.random.Random

object ConfigReader {

    private const val configPath = "../generator"
    private const val savePath = "measurement.json"

    private val renamer = object : FieldRenamer {
        override fun toJson(fieldName: String) = FieldRenamer.camelToUnderscores(fieldName)
        override fun fromJson(fieldName: String) = FieldRenamer.underscoreToCamel(fieldName)
    }

    private val klaxon = Klaxon().fieldRenamer(renamer)

    private fun drawConfigFile(id: Int): String = when (id) {
        0 -> "human1"
        else -> "human2"
    }

    fun getHumanData(id: Int = Random.Default.nextInt(2)): List<Activity>? {
        val resource = "$configPath/${drawConfigFile(id)}.json"
        val configFile = try {
            javaClass.getResource(resource).readText(Charsets.UTF_8)
        } catch (all: Exception) {
            throw RuntimeException("Failed to load resource=$resource!", all)
        }
        return klaxon.parseArray(configFile)
    }

    fun saveMeasurement(measurement: Measurement) {
        val jsonString = klaxon.toJsonString(measurement)
        File(savePath).apply {
            createNewFile()
        }.also {
            it.writeText(jsonString)
        }
    }
}