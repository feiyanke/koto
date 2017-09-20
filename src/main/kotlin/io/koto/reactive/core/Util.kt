package io.koto.reactive.core

import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

fun <T:Any> Iterable<T>.asStream(): Context<T, T> = Context.from(this)
fun <T:Any> Array<T>.asStream(): Context<T, T> = Context.from(this)
fun Runnable.asStream(): Context<Unit, Unit> = Context.from(this)
fun <T:Any> Callable<T>.asStream(): Context<T, T> = Context.from(this)
fun <T:Any> Future<T>.asStream(): Context<T, T> = Context.from(this)

fun Context<*, Int>.sum() = reduce(0) { x, y->x+y}
fun <T:Comparable<T>> Context<*, T>.min() = reduce { x, y->if (x<y) x else y }
fun <T:Comparable<T>> Context<*, T>.max() = reduce { x, y->if (x>y) x else y }
fun Context<*, Int>.average() = scan(0) { x, y->x+y}.indexStamp().last()!!.let { it.value.toDouble() / (it.index+1) }

class TimeStamp<out T>(val value:T) {
    val time = System.currentTimeMillis()
}

class IndexStamp<out T>(val value: T, val index:Int)


