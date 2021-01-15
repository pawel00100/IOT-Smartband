package generator

import kotlin.properties.Delegates
import kotlin.random.Random

class Generator(var data: HumanData) {
    private var startTime by Delegates.notNull<Long>()
    private lateinit var currentActivity: Activity

    init {
        selectActivity()
        startTime = System.nanoTime()
    }

    private fun selectActivity(){
        val id = Random.Default.nextInt(data.activities.size)
        currentActivity = data.activities[id]
    }


}