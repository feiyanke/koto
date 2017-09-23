package io.koto.reactive.core

import java.util.concurrent.Callable
import java.util.concurrent.Future

fun <T:Any> Iterable<T>.asStream(): Stream<T, T> = Stream.from(this)
fun <T:Any> Array<T>.asStream(): Stream<T, T> = Stream.from(this)
fun Runnable.asStream(): Stream<Unit, Unit> = Stream.from(this)
fun <T:Any> Callable<T>.asStream(): Stream<T, T> = Stream.from(this)
fun <T:Any> Future<T>.asStream(): Stream<T, T> = Stream.from(this)

fun Stream<*, Int>.sum() = reduce(0) { x, y->x+y}
fun <T:Comparable<T>> Stream<*, T>.min() = reduce { x, y->if (x<y) x else y }
fun <T:Comparable<T>> Stream<*, T>.max() = reduce { x, y->if (x>y) x else y }
fun Stream<*, Int>.average() = scan(0) { x, y->x+y}.indexStamp().last()!!.let { it.value.toDouble() / (it.index+1) }

fun <K, T> Stream<*, Pair<K, T>>.forEach(key:K, block:(T)->Unit) = make(GroupForEachOperator(key, block))

class TimeStamp<out T>(val value:T) {
    val time = System.currentTimeMillis()
}

class IndexStamp<out T>(val value: T, val index:Int)


