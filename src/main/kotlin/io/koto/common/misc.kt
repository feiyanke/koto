package io.koto.common

import java.nio.ByteBuffer
import kotlin.experimental.and

fun ByteArray.asBuffer() = ByteBuffer.wrap(this)

fun Byte.asUnsigned() = toShort() and 0xFF
fun Short.asUnsigned() = toInt() and 0xFFFF
fun Int.asUnsigned() = toLong() and 0xFFFFFFFFL
fun ubyte(v:Int) = v and 0xFF
fun ushort(v:Int) = v and 0xFFFF
fun uint(v:Long) = v and 0xFFFFFFFFL

fun <T> List<T>.orNull(): List<T>? = if (isEmpty()) null else this
fun Boolean.be() : Boolean? = if (this) this else null
infix fun <T> Boolean.be(v:T) : T? = if (this) v else null
inline fun <R> Boolean.be(expression: () -> R) : R? = if (this) expression() else null

inline fun <reified R> Iterable<*>.find(predicate: (R) -> Boolean): List<R> {
    val list : MutableList<R> = mutableListOf()
    for (i in this) {
        if ((i is R) && predicate(i)) list.add(i)
    }
    return list
}

inline fun <reified R> tryOr(default:R, expression:()->R) : R {
    return try {expression()}catch (e:Throwable) {e.printStackTrace();default}
}

inline fun <reified R> tryOrNull(expression:()->R) : R?{
    return try {expression()}catch (e:Throwable) {e.printStackTrace();null}
}