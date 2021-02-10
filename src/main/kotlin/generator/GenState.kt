package generator

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.KlaxonException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.random.Random

@Target(AnnotationTarget.FIELD)
annotation class KlaxonDate

val dateConverter = object : Converter {
    override fun canConvert(cls: Class<*>) = cls == LocalDate::class.java

    override fun fromJson(jv: JsonValue) =
        if (jv.string != null) {
            LocalDate.parse(jv.string, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } else {
            throw KlaxonException("Couldn't parse date: ${jv.string}")
        }

    override fun toJson(value: Any) = """ "$value" """
}

data class GenState @JvmOverloads constructor(
    var lastActivityName: String = "",
    @KlaxonDate
    var lastOvulationDate: LocalDate = LocalDate.now(),
    val sex: String = if (Random.Default.nextBoolean()) "male" else "female"
) {
    fun isOvulating(): Boolean =
        if (sex == "female" && ChronoUnit.DAYS.between(lastOvulationDate, LocalDate.now()) == 28L) {
            lastOvulationDate = LocalDate.now()
            true
        } else false
}