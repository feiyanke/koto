package io.koto.reactive.core

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

abstract class Source<R> {
    open val signal: IEasyMethod<R> = empty<R>()
    open val error : IEasyMethod<Throwable> = empty<Throwable>()
    open val finish : IUnitMethod = empty()
    open protected val start : IUnitMethod = empty()
    open protected val cancel : IUnitMethod = empty()
    open protected val report : IUnitMethod = empty()
    fun make(): Stream<R, R> = Stream(signal, error, finish, start, cancel, report)
}

abstract class BaseSource<R>: Source<R>(){

    private var count = AtomicInteger(0)

    override val signal = method<R> {
        count.incrementAndGet()
        output(it)
    }

    override val error = empty<Throwable>()

    override val finish = method {
        if (count.decrementAndGet() == -1) {
            output()
        }
    }

    override val cancel = method {
        signal.output = {}
        error.output = {}
        finish.output = {}
    }

    override val report = method {
        if (count.decrementAndGet() == -1) {
            finish.output()
        }
    }
}

class BlockSource<R>(block: BaseSource<R>.()->Unit) : BaseSource<R>() {
    override val start = method {
        try {
            block()
        } catch (e: Throwable) {
            error(e)
        } finally {
            finish()
        }
    }
}

class RunableSource(block:()->Unit) : BaseSource<Unit>() {
    override val start = method {
        try {
            block()
        } catch (e: Throwable) {
            error(e)
        } finally {
            finish()
        }
    }
}

class CallableSource<R>(callable:()->R) : BaseSource<R>() {
    override val start = method {
        try {
            signal(callable())
        } catch (e: Throwable) {
            error(e)
        } finally {
            finish()
        }
    }
}

class IntervalSource(ms:Long): BaseSource<Int>(){
    var count = AtomicInteger(0)
    override val start = method {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            signal(count.getAndIncrement())
        }, 0, ms, TimeUnit.MILLISECONDS)
    }
}

class IterableSource<T>(iterable: Iterable<T>): BaseSource<T>() {
    val iter = iterable.iterator()
    override val start = method {
        iterable.forEach { signal(it) }
        finish()
    }
}

class MergeSource<T>(sync:Boolean, list:List<Stream<*, T>>): BaseSource<T>() {
    private val count = AtomicInteger(list.size)
    override val start = method {
        list.forEach {
            if (sync) {
                it.forEach {
                    signal(it)
                }.finish {
                    if (count.decrementAndGet() == 0) {
                        finish()
                    }
                }
            } else {
                thread {
                    it.forEach {
                        signal(it)
                    }.finish {
                        if (count.decrementAndGet() == 0) {
                            finish()
                        }
                    }
                }
            }
        }
    }
}

class ZipSource<T>(list: List<Stream<*, T>>): BaseSource<List<T>>() {

    private val buffer : List<MutableList<T>> = list.map { mutableListOf<T>() }
    private val count = AtomicInteger(list.size)
    override val start = method {
        for (i in 0 until list.size) {
            list[i].forEach {
                buffer[i].add(it)
                check()?.let { signal(it) }
            }.finish {
                if (count.decrementAndGet() == 0) {
                    finish()
                }
            }
        }
    }

    @Synchronized private fun check() : List<T>? {
        return if(buffer.all { it.isNotEmpty() }) {
            buffer.map { it.removeAt(0) }
        } else null
    }
}
