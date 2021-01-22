package generator

import java.util.*
import kotlin.math.abs
import kotlin.reflect.KProperty


/**
 * Class implementing step counting algorithm. Uses data from accelerometer
 * do estimate number of steps.
 *
 * @param measurement object collecting results
 * @param stepsMod parameter controlling sensitivity of filter
 *
 * @property accelX value of acceleration on axis X
 * @property accelY value of acceleration on axis Y
 * @property accelZ value of acceleration on axis Z
 */
class Pedometer(
    private val measurement: Measurement,
    private val stepsMod: Double
) {

    private val maxSize = 10
    private val accelD = LinkedList<Double>()

    var accelX: Double by AxisFilter()
    var accelY: Double by AxisFilter()
    var accelZ: Double by AxisFilter()

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

    private class AxisFilter(private var accel: Double = 0.0) {
        operator fun getValue(thisRef: Pedometer, property: KProperty<*>): Double = accel
        operator fun setValue(thisRef: Pedometer, property: KProperty<*>, value: Double) {
            val delta = abs(value - accel)
            if (delta > thisRef.stepsMod * abs(accel)) thisRef.updateAccel()
            accel = value
        }
    }
}