package generator

import java.util.*
import kotlin.math.abs


class Measurement(
    var temp: Double = 0.0,
    var pulse: Double = 0.0,
    var steps: Int = 0
) {

    private val maxSize = 10
    private val accelD = LinkedList<Double>()

    var accelX: Double = 0.0
        set(value) {
            if (abs(value - field) > 5 * abs(field) / 4) updateAccel()
            field = value
        }
    var accelY: Double = 0.0
        set(value) {
            if (abs(value - field) > 5 * abs(field) / 4) updateAccel()
            field = value
        }
    var accelZ: Double = 0.0
        set(value) {
            if (abs(value - field) > 5 * abs(field) / 4 ) updateAccel()
            field = value
        }

    private fun updateAccel() {
        if (accelX != 0.0 && accelY != 0.0 && accelZ != 0.0)
            accelD.offer(abs(accelX + accelY + accelZ))
        if (accelD.size == maxSize)
            updateSteps()
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