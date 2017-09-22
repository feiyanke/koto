package io.koto.example

import io.koto.common.delayRun
import io.koto.reactive.core.Stream
import java.util.*
import kotlin.concurrent.thread
import kotlin.concurrent.timerTask

fun testMap(n:Int) : String {
    Thread.sleep(1000)
    return n.toString()
}

fun testMapAsync(n:Any, cb:(String)->Unit){
    thread {
        Thread.sleep(1000)
        cb(n.toString())
    }
}

fun main(args: Array<String>) {
    val key1 = KeyNotifier {
        println(it.name)
    }

    val time = System.currentTimeMillis()
    key1.down(time + 100)
    key1.up(time + 6000)
//    key1.up(time + 500)
//    key1.up(time + 600)
    //key1.down(time + 800)
    //key1.up(time + 900)
    while (true) {

    }

}

class KeyNotifier(val cb:(Notification)->Unit) {

    enum class Notification {
        CLICK, DOUBLE_CLICK, PRESS_START, PRESS_END
    }

    val PRESS_START_TIME : Long = 2000      //ms
    val DOUBLE_CLICK_INTERVAL : Long = 500  //ms

    @Volatile var downTime : Long = 0
    @Volatile var clickTime : Long = 0
    var task1 : TimerTask = timerTask {  }
    var task2 : TimerTask = timerTask {  }

    fun click(ms: Long) {
        if (clickTime != 0L) {
            val interval = ms - clickTime
            if (interval < DOUBLE_CLICK_INTERVAL) {
                clickTime = 0
                task2.cancel()
                cb(Notification.DOUBLE_CLICK)
                return
            }
        }
        clickTime = ms
        delayRun(DOUBLE_CLICK_INTERVAL) {
            cb(Notification.CLICK)
        }

    }


    fun down(ms:Long){
        downTime = ms
        task1.cancel()
        task1 = delayRun(PRESS_START_TIME) {
            cb(Notification.PRESS_START)
        }
    }

    fun down() = down(System.currentTimeMillis())

    fun up(ms:Long) {
        if (downTime > 0L) {
            val interval = ms - downTime
            if (interval > PRESS_START_TIME) {
                cb(Notification.PRESS_END)
            } else if (interval>0){
                task1.cancel()
                click(ms)
            }
            downTime = 0
        }
    }

    fun up() = up(System.currentTimeMillis())

}