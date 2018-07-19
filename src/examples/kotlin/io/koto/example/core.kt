package io.koto.example

import io.koto.reactive.core.asStream
import java.io.Closeable
import java.util.regex.Pattern
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

    val a = TestCloseable()
    val b = TestCloseable()
    val c = TestCloseable()
    try {
        use(a,b,c) {
            println("123")
            throw Exception("!234")
        }
    } catch (e:Exception) {
        println("345")
        e.printStackTrace()
    }


//    for (i in 0 until 11 step 2 ) {
//        println(i)
//    }
    

//    (0..10).asStream()
//            .groupBy({it<5}){
//                if (it) {
//                    take(2)
//                    .forEach { println(it) }
//                    .finish { println("finish:$it") }
//
//                } else {
//                    take(3)
//                    .forEach { println("no:$it") }
//                    .finish{println("finish:$it")}
//                }
//            }
//            .finish { println("finish") }

}

class TestCloseable : Closeable {
    override fun close() {
        println("close:$this")
    }

}

public inline fun <R> use(vararg closeble: Closeable, block: () -> R): R {
    try {
        return block()
    } finally {
        for (c in closeble) {
            try {
                c.close()
            } catch (e:Exception) {

            }
        }
    }
}