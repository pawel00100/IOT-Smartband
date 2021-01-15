package generator

import com.beust.klaxon.Json

data class HumanData(
    val frequency: Int,
    val activities: List<Activity>
)

data class Activity(
    @Json(name = "activity")
    val name: String,
    @Json(name = "activity_id")
    val id: Int,
    val hand: Map<String, SensorData>,
    val chest: Map<String, SensorData>,
    val ankle: Map<String, SensorData>
    )

data class SensorData(
    val mean: Double,
    val std: Double,
    val mad: Double,
    val diffMean: Double,
    val incMean: Double,
    val incStd: Double,
    val incMad: Double,
    val decMean: Double,
    val decStd: Double,
    val decMad: Double,
    val deriativeChanges: Int,
    val signChanges: Int,
    val total: Int
)