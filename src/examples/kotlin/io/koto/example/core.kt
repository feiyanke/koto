package io.koto.example

import io.koto.common.JParser
import io.koto.common.be
import io.koto.common.delayRun
import io.koto.common.then
import io.koto.reactive.core.Stream
import io.koto.reactive.core.asStream
import io.koto.reactive.core.forEach
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

    val a = JParser("""   [] """).parse()


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