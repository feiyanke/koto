package io.koto.example

import io.koto.reactive.core.asStream
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

    (0..10).asStream()
            .groupBy({it<5}){
                if (it) {
                    take(2)
                    .forEach { println(it) }
                    .finish { println("finish:$it") }

                } else {
                    take(3)
                    .forEach { println("no:$it") }
                    .finish{println("finish:$it")}
                }
            }
            .finish { println("finish") }

}