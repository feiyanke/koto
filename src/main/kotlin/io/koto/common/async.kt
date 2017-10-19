package io.koto.common

import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.timerTask

class ValueLatch<T> : CountDownLatch(1) {
    val value = AtomicReference<T>()
    fun set(v:T) {
        value.set(v)
        countDown()
    }
    fun get() : T {
        await()
        return value.get()
    }
    fun get(ms: Long) : T? {
        return if(await(ms, TimeUnit.MILLISECONDS)) {
            value.get()
        } else null
    }
}

class Latch {
    var latch = CountDownLatch(1)

    @Synchronized fun await() {
        latch = CountDownLatch(1)
        latch.await()
    }

    @Synchronized fun await(ms:Long) : Boolean {
        latch = CountDownLatch(1)
        return latch.await(ms, TimeUnit.MILLISECONDS)
    }
    fun async() {
        latch.countDown()
    }
}

inline fun delayRun(delay:Long, crossinline block:TimerTask.()->Unit):TimerTask
        = timerTask(block).apply { Timer().schedule(this, delay) }
fun delayRun(delay:Long, task:TimerTask) = Timer().schedule(task, delay)
inline fun fixRateRun(delay: Long, peroid:Long, crossinline block:TimerTask.()->Unit):TimerTask
        = timerTask(block).apply { Timer().scheduleAtFixedRate(this, delay, peroid) }
inline fun fixDelayRun(delay: Long, peroid: Long, crossinline block: TimerTask.() -> Unit):TimerTask
        = timerTask(block).apply { Timer().schedule(this, delay, peroid) }

