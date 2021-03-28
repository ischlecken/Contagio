package de.contagio.core

import kotlin.test.Test
import kotlin.test.assertEquals

class HashTest {

    @Test
    fun emptyStringWithEmptySalt_isExpected() {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", "".toSHA256(""))
    }

    @Test
    fun blaWithEmptySalt_isExpected() {
        assertEquals("4df3c3f68fcc83b27e9d42c90431a72499f17875c81a599b566c9889b9696703", "bla".toSHA256(""))
    }

    @Test
    fun blaWithFaselSalt_isExpected() {
        assertEquals("60e25d01aeb5a68d8247d00ebbc966b12b507d46c24d34515b4e629e5bde9ea8", "bla".toSHA256("fasel"))
    }
}
