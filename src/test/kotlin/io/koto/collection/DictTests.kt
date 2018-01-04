package io.koto.collection

import org.junit.Test
import kotlin.test.assertEquals


class DictTests {
    @Test
    fun test1() {
        val dict = Dictionary.Builder()
                .add("ab")
                .add("abc")
                .add("bcd")
                .add("abcd")
                .add("bcde")
                .add("cde")
                .add("defg")
                .build()
        val r = dict.find("123abcdefg123", exclusive = false)
        assertEquals(setOf("ab","abc", "bcd", "abcd", "bcde", "cde", "defg"),r)
    }

    @Test
    fun test2() {
        val dict = Dictionary.Builder()
                .add("ab")
                .add("abc")
                .add("bcd")
                .add("abcd")
                .add("bcde")
                .add("cde")
                .add("defg")
                .build()
        val r = dict.find("123abcdefg123")
        assertEquals(setOf("abcd", "bcde", "defg"),r)
    }

    @Test
    fun test3() {
        val dict = Dictionary.Builder()
                .add("ab")
                .add("abc")
                .add("bcd")
                .add("abcd")
                .add("bcde")
                .add("cde")
                .add("defg")
                .build()
        val r = dict.find("123abcadefg123")
        assertEquals(setOf("abc", "defg"),r)
    }

    @Test
    fun test4() {
        val dict = Dictionary.Builder()
                .add("ab")
                .add("abc")
                .add("bcd")
                .add("abcd")
                .add("bcde")
                .add("cde")
                .add("defg")
                .build()
        val r = dict.find("ababcadefg123")
        assertEquals(setOf("ab", "abc", "defg"),r)
    }

    @Test
    fun test5() {
        val dict = Dictionary.Builder()
                .add("ab")
                .add("abc")
                .add("bcd")
                .add("abcd")
                .add("bcde")
                .add("cde")
                .add("defg")
                .add("ababcadefg123")
                .build()
        val r = dict.find("ababcadefg123")
        assertEquals(setOf("ababcadefg123"),r)
    }

    @Test
    fun test6() {
        val dict = Dictionary.Builder()
                .add("ab")
                .add("abc")
                .add("bcd")
                .add("abcd")
                .add("bcde")
                .add("cde")
                .add("defg")
                .add("ababcbcdefg123")
                .build()
        val r = dict.find("ababcbcdefg123", false)
        assertEquals(setOf("ababcbcdefg123", "ab","abc","bcd", "bcde", "cde", "defg"),r)
    }
}