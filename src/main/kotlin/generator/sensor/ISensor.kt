package generator.sensor

interface ISensor {
    val frequency: Double
    fun generateValue(): Double
}