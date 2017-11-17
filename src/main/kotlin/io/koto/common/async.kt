package io.koto.common

import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledThreadPoolExecutor
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

val executor = ScheduledThreadPoolExecutor(10)
fun delayRun(delay:Long, block:()->Unit)
        = executor.schedule(block, delay, TimeUnit.MILLISECONDS)
fun fixRateRun(delay: Long, peroid:Long, block:()->Unit)
        = executor.scheduleAtFixedRate(block, delay, peroid, TimeUnit.MILLISECONDS)
fun fixDelayRun(delay: Long, peroid: Long, block: () -> Unit)
        = executor.scheduleWithFixedDelay(block, delay, peroid, TimeUnit.MILLISECONDS)

