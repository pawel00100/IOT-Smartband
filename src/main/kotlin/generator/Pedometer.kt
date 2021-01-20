package generator

import java.util.*
import kotlin.math.abs

class Pedometer(
    private val measurement: Measurement,
    private val stepsMod: Double
) {

    private val maxSize = 10
    private val accelD = LinkedList<Double>()

    var accelX: Double = 0.0
        set(value) {
            val delta = abs(value - field)
            if (delta > stepsMod * abs(field)) updateAccel()
            field = value
        }

    var accelY: Double = 0.0
        set(value) {
            val delta = abs(value - field)
            if (delta > stepsMod * abs(field)) updateAccel()
            field = value
        }

    var accelZ: Double = 0.0
        set(value) {
            val delta = abs(value - field)
            if (delta > stepsMod * abs(field)) updateAccel()
            field = value
        }

    private fun updateAccel() {
        if (accelX != 0.0 && accelY != 0.0 && accelZ != 0.0)
            accelD.offer(abs(accelX + accelY + accelZ))
        if (accelD.size == maxSize)
            updateSteps()
    }

    private fun updateSteps() {
        var steps = 0
        //finding peaks in cached measurements
        val peaks = Array(maxSize) { false }
        for (i in 1 until maxSize - 1) if (accelD[i] > accelD[i - 1] && accelD[i] > accelD[i + 1]) peaks[i] = true
        //check whether peak are separated by at least two measurements
        //only these will be counted as steps
        var k = 0
        var d: Int
        for (i in peaks.indices) {
            if (peaks[i]) {
                if (k != 0) {
                    d = i - k - 1
                    if (d > 2) steps++
                }
                k = i
            }
        }
        //if last step was more than two measures before end,
        //compensate for possible "missed" step
        if (maxSize - k > 2) steps++
        accelD.clear()
        //mutex is already acquired in AbstractSensor calling accel* setter
        measurement.steps += steps
    }
}