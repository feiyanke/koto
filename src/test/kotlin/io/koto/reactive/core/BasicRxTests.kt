/**
 * Copyright 2013 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.koto.reactive.core

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.*

/**
 * This class use plain Kotlin without extensions from the language adaptor
 */
class BasicRxTests : KotlinTests() {

    @Test
    fun testCreate() {
        Stream.create<String> {
            signal("Hello")
            finish()
        }.forEach { a.received(it) }.finish()
        verify(a, times(1)).received("Hello")
    }

    @Test
    fun testFilter() {
        Stream.from(listOf(1, 2, 3))
                .filter { it >= 2 }
                .forEach(received()).finish()
        verify(a, times(0)).received(1)
        verify(a, times(1)).received(2)
        verify(a, times(1)).received(3)
    }

    @Test
    fun testLast() {
        assertEquals("three", Stream.from(listOf("one", "two", "three")).last())
    }

    @Test
    fun testMap1() {
        Stream.just(1)
                .map { v -> "hello_$v" }
                .forEach(received()).finish()
        verify(a, times(1)).received("hello_1")
    }

    @Test
    fun testMap2() {
        Stream.from(listOf(1, 2, 3)).map { v -> "hello_$v" }
                .forEach(received())
                .finish()
        verify(a, times(1)).received("hello_1")
        verify(a, times(1)).received("hello_2")
        verify(a, times(1)).received("hello_3")
    }


    @Test
    fun testMerge() {
        Stream.merge(
                Stream.from(listOf(1, 2, 3)),
                Stream.merge(
                        Stream.just(6),
                        Stream.just(7)
                ),
                Stream.from(listOf(4, 5))
        ).forEach(received()).finish()
        verify(a, times(1)).received(1)
        verify(a, times(1)).received(2)
        verify(a, times(1)).received(3)
        verify(a, times(1)).received(4)
        verify(a, times(1)).received(5)
        verify(a, times(1)).received(6)
        verify(a, times(1)).received(7)
    }

    @Test
    fun testFromWithIterable() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(5, Stream.from(list).count())
    }

    @Test
    fun testFromWithObjects() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(2, Stream.from(listOf(list, 6)).count())
    }

    @Test
    fun testStartWith() {
        val list = listOf(10, 11, 12, 13, 14)
        val startList = listOf(1, 2, 3, 4, 5)
        assertEquals(6, Stream.from(list).startWith(0).count())
        assertEquals(10, Stream.from(list).startWith(startList).count())
    }

    @Test
    fun testSkipTake() {
        Stream.from(listOf(1, 2, 3)).skip(1).take(1)
                .forEach(received())
                .finish()
        verify(a, times(0)).received(1)
        verify(a, times(1)).received(2)
        verify(a, times(0)).received(3)
    }

    @Test
    fun testSkip() {
        Stream.from(listOf(1, 2, 3)).skip(2)
                .forEach(received())
                .finish()
        verify(a, times(0)).received(1)
        verify(a, times(0)).received(2)
        verify(a, times(1)).received(3)
    }

    @Test
    fun testTake() {
        Stream.from(listOf(1, 2, 3)).take(2)
                .forEach(received())
                .finish()
        verify(a, times(1)).received(1)
        verify(a, times(1)).received(2)
        verify(a, times(0)).received(3)
    }

    @Test
    fun testTakeLast() {
        Stream.just(1, 3, 2, 5, 4).takeLast(2)
                .forEach(received())
                .finish()
        verify(a, times(0)).received(1)
        verify(a, times(0)).received(2)
        verify(a, times(0)).received(3)
        verify(a, times(1)).received(4)
        verify(a, times(1)).received(5)
    }

    @Test
    fun testTakeWhile() {
        Stream.from(listOf(1, 2, 3)).takeWhile { x -> x < 3 }
                .forEach(received())
                .finish()
        verify(a, times(1)).received(1)
        verify(a, times(1)).received(2)
        verify(a, times(0)).received(3)
    }

    @Test
    fun testTakeWhileWithIndex() {
        Stream.from(listOf(1, 2, 3))
                .takeWhile { x -> x < 3 }
                .indexStamp()
                .forEach { a.received(it.index) }
                .finish()
        verify(a, times(1)).received(0)
        verify(a, times(1)).received(1)
        verify(a, times(0)).received(2)
    }

    @Test
    fun testLastOrDefault() {
        assertEquals("two", Stream.from(listOf("one", "two")).last())
        assertEquals("default", Stream.from(listOf("one", "two")).filter { it.length > 3 }.last() ?: "default")
    }

    @Test
    fun testAll() {
        a.received(Stream.from(listOf(1, 2, 3)).all { x -> x > 0 })
        verify(a, times(1)).received(true)
    }

    @Test
    fun testZip() {
        val o1 = Stream.from(listOf(1, 2, 3))
        val o2 = Stream.from(listOf(4, 5, 6))
        val o3 = Stream.from(listOf(7, 8, 9))

        Stream.zip(o1, o2, o3).forEach(received()).finish()
        verify(a, times(1)).received(listOf(1, 4, 7))
        verify(a, times(1)).received(listOf(2, 5, 8))
        verify(a, times(1)).received(listOf(3, 6, 9))
    }
}