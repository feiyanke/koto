package io.koto.reactive.core

import io.koto.common.ValueLatch
import java.util.concurrent.*

class Stream<T, R> (
        var signal: IMethod<T, R>,
        var error: IEasyMethod<Throwable>,
        var finish: IUnitMethod,

        var start: IUnitMethod,
        var cancel: IUnitMethod,
        var report: IUnitMethod
) {

    fun filter(predicate:(R)->Boolean): Stream<T, R> = make(FilterOperator(predicate))
    fun distinct(): Stream<T, R> = make(DistinctOperator())
    fun apply(block: R.() -> Unit) = make(ApplyOperator(block))
    fun also(block: (R) -> Unit) = make(AlsoOperator(block))
    fun <E> map(tranform:(R)->E): Stream<T, E> = make(MapOperator(tranform))
    fun <E> map(method: IMethod<R, E>): Stream<T, E> = make(method)
    fun <E> mapCallback(callback:(R, (E)->Unit)->Unit): Stream<T, E> = make(MapCallbackOperator(callback))
    fun <E> mapFuture(method:(R)->Future<E>): Stream<T, E> = make(MapFutureOperator(method))
    fun scan(init:R? = null, method:(R,R)->R): Stream<T, R> = make(ScanOperator(init, method))
    fun reduce(init:R? = null, method:(R,R)->R) = scan(init, method).last()?:init
    fun multiScan(vararg init:R, m:(List<R>,R)->R): Stream<T, R> = make(MultiScanOperator(*init, method = m))
    fun forEach(block:(R)->Unit): Stream<T, R> = make(ForEachOperator(block))
    fun error(block: (e:Throwable) -> Unit): Stream<T, R> = make(ErrorOperator(block))
    fun take(n:Int): Stream<T, R> = make(TakeOperator(n))
    fun takeLast(n:Int): Stream<T, R> = make(TakeLastOperator(n))
    fun log(block: (R) -> String): Stream<T, R> = make(LogOperator(block))
    fun print() = forEach(::println).finish()
    fun on(executor: Executor): Stream<T, R> = make(ScheduleOperator(executor))
    fun parallel(): Stream<T, R> = on(Executors.newCachedThreadPool())
    fun pack(n:Int): Stream<T, R> = make(PackOperator(n))
    fun serialze(): Stream<T, R> = pack(1)
    fun buffer(count:Int) = make(BufferOperator(count))
    fun debounce(ms:Long) = make(DebounceOperator(ms))
    fun throtle(ms:Long) = debounce(ms)
    fun sample(ms:Long) = make(SampleOperator(ms))
    fun <E> flatMap(transform:(R)-> Stream<*, E>): Stream<T, E> = make(FlatMapOperator(transform))
    fun elementAt(index:Int): Stream<T, R> = make(ElementAtOperator(index))
    fun first(): Stream<T, R> = elementAt(0)
    fun skip(count: Int): Stream<T, R> = make(SkipOperator(count))
    fun skipLast(count: Int): Stream<T, R> = make(SkipLastOperator(count))
    fun startWith(stream: Stream<*, R>): Stream<*, R> = Companion.concat(stream, this)
    fun startWith(vararg v:R): Stream<*, R> = Companion.concat(just(*v), this)
    fun startWith(list:List<R>): Stream<*, R> = Companion.concat(from(list), this)
    fun merge(vararg stream: Stream<*, R>, sync: Boolean = false): Stream<*, R> = Companion.merge(this, *stream, sync = sync)
    fun concat(vararg stream: Stream<*, R>): Stream<*, R> = Companion.concat(this, *stream)
    fun zip(vararg stream: Stream<*, R>): Stream<*, List<R>> = Companion.zip(this, *stream)
    fun timeInterval(): Stream<T, Long> = make(TimeIntervalOperator())
    fun timeStamp(): Stream<T, TimeStamp<R>> = map { TimeStamp(it) }
    fun indexStamp(): Stream<T, IndexStamp<R>> = make(IndexedOperator())
    fun takeUntil(predicate: (R) -> Boolean): Stream<T, R> = make(TakeUntilOperator(predicate))
    fun takeWhile(predicate: (R) -> Boolean): Stream<T, R> = make(TakeWhileOperator(predicate))
    fun skipUntil(predicate: (R) -> Boolean): Stream<T, R> = make(SkipUntilOperator(predicate))
    fun skipWhile(predicate: (R) -> Boolean): Stream<T, R> = make(SkipWhileOperator(predicate))
    fun finish(block: () -> Unit) = make(FinishOperator(block)).start()
    fun finish() {
        val latch = CountDownLatch(1)
        finish { latch.countDown() }
        latch.await()
    }
    fun last(block: (R?) -> Unit) = make(LastOperator()).forEach(block).finish{}
    fun last() : R? {
        val latch = ValueLatch<R?>()
        last {
            latch.set(it)
        }
        return latch.get()
    }
    fun all(predicate: (R) -> Boolean) = map(predicate).takeUntil { !it }.last()!!
    fun contains(v:R) = takeUntil { it == v }.last() == v
    fun any(predicate: (R) -> Boolean) = map(predicate).takeUntil { it }.last()!!
    fun count() = (indexStamp().last()?.index?:-1) + 1

    companion object {
        fun <T> create(block: Source<T>.()->Unit): Stream<T, T> = make(BlockSource(block))
        fun fromRunable(block:()->Unit): Stream<Unit, Unit> = make(RunableSource(block))
        fun from(runnable: Runnable): Stream<Unit, Unit> = fromRunable(runnable::run)
        fun <T> fromCallable(callable:()->T): Stream<T, T> = make(CallableSource(callable))
        fun <T> from(callable: Callable<T>): Stream<T, T> = fromCallable(callable::call)
        fun <T> from(future: Future<T>): Stream<T, T> = fromCallable(future::get)
        fun <T> from(iterable: Iterable<T>): Stream<T, T> = make(IterableSource(iterable))
        fun <T> from(array: Array<T>): Stream<T, T> = make(IterableSource(array.asIterable()))
        fun <T> just(vararg values:T): Stream<T, T> = from(values.asIterable())
        fun range(n:Int, m:Int): Stream<Int, Int> = from(n until m)
        fun interval(ms: Long): Stream<Int, Int> = make(IntervalSource(ms))
        fun <T> merge(vararg stream: Stream<*, T>, sync:Boolean = false): Stream<*, T> = make(MergeSource(sync, stream.asList()))
        fun <T> concat(vararg stream: Stream<*, T>): Stream<*, T> = merge(*stream, sync = true)
        fun <T> zip(vararg stream: Stream<*, T>): Stream<*, List<T>> = make(ZipSource(stream.asList()))

        private fun <T> make(o: Source<T>): Stream<T, T> = o.make()
    }

    fun <E> make(m: Operator<R, E>) = make(m.signal, m.error, m.finish, m.start, m.cancel, m.report)
    fun make(m: EasyOperator<R>) = make(m.signal, m.error, m.finish, m.start, m.cancel, m.report)

    fun <E> make(next : IMethod<R, E>,
                 error : IEasyMethod<Throwable>? = null,
                 finish : IUnitMethod? = null,
                 start : IUnitMethod? = null,
                 cancel : IUnitMethod? = null,
                 report : IUnitMethod? = null
    ) : Stream<T, E> = chainNext(next).apply {
        chainError(error)
        chainFinish(finish)
        chainStart(start)
        chainCancel(cancel)
        chainReport(report)
    }

    fun make(next : IEasyMethod<R>? = null,
             error : IEasyMethod<Throwable>? = null,
             finish : IUnitMethod? = null,
             start : IUnitMethod? = null,
             cancel : IUnitMethod? = null,
             report : IUnitMethod? = null
    ) : Stream<T, R> = apply {
        chainNext(next)
        chainError(error)
        chainFinish(finish)
        chainStart(start)
        chainCancel(cancel)
        chainReport(report)
    }

    private fun <E> chainNext(m: IMethod<R, E>) : Stream<T, E> = Stream(signal.chain(m), error, finish, start, cancel, report)
    private fun chainNext(m: IEasyMethod<R>?) = m?.let { signal = signal.chain(m) }
    private fun chainError(m: IEasyMethod<Throwable>?) = m?.let { error = error.chain(m) }
    private fun chainFinish(m: IUnitMethod?) = m?.let { finish = finish.chain(m) }
    private fun chainStart(m: IUnitMethod?) = m?.let { start = it.chain(start) }
    private fun chainCancel(m: IUnitMethod?) = m?.let { cancel = it.chain(cancel) }
    private fun chainReport(m: IUnitMethod?) = m?.let { report = it.chain(report) }
}

