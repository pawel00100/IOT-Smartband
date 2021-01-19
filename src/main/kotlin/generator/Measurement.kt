package generator

import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt


class Measurement(
    var temp: Double = 0.0,
    var pulse: Double = 0.0,
    var steps: Int = 0
) {

    private val maxSize = 10
    private val accelD = LinkedList<Double>()

    var accelX: Double = 0.0
        set(value) {
            field = value
            updateAccel()
        }
    var accelY: Double = 0.0
        set(value) {
            field = value
            updateAccel()
        }
    var accelZ: Double = 0.0
        set(value) {
            field = value
            updateAccel()
        }

    private fun updateAccel() {
        accelD.offer(sqrt(accelX.pow(2) + accelY.pow(2) + accelZ.pow(2)))
        if (accelD.size == maxSize) updateSteps()
    }

    private fun updateSteps() {
        val p = Array(maxSize) { 0 }
        for (i in 1 until maxSize - 1) if (accelD[i] > accelD[i - 1] && accelD[i] > accelD[i + 1]) p[i] = 1

        var k = 0
        var d: Int
        for (i in p.indices) {
            if (p[i] == 1) {
                if (k != 0) {
                    d = i - k - 1
                    if (d > 2) steps++
                }
                k = i
            }
        }
        if (maxSize - k > 2) steps++
        accelD.clear()
    }

    override fun toString(): String = "Measurement(temp=$temp, pulse=$pulse, steps=$steps)"

}