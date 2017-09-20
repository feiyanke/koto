package io.koto.example

import io.koto.reactive.core.Context
import io.koto.reactive.core.asStream
import io.koto.reactive.core.average
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

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
    Context.interval(500).sample(600).print()
    print("finish")
}