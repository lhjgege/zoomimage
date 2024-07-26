package com.github.panpf.zoomimage.core.common.test.util

import com.github.panpf.zoomimage.util.format
import com.github.panpf.zoomimage.util.quietClose
import com.github.panpf.zoomimage.util.toHexString
import okio.Closeable
import okio.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class CoreUtilsTest {

    @Test
    fun testFormat() {
        assertEquals(1.2f, 1.234f.format(1), 0f)
        assertEquals(1.23f, 1.234f.format(2), 0f)
        assertEquals(1.24f, 1.235f.format(2), 0f)
    }

    @Test
    fun testToHexString() {
        val any1 = Any()
        val any2 = Any()
        assertEquals(
            expected = any1.hashCode().toString(16),
            actual = any1.toHexString()
        )
        assertEquals(
            expected = any2.hashCode().toString(16),
            actual = any2.toHexString()
        )
        assertNotEquals(
            illegal = any1.toHexString(),
            actual = any2.toHexString()
        )
    }

    @Test
    fun testQuietClose() {
        val myCloseable = MyCloseable()

        assertFailsWith(IOException::class) {
            myCloseable.close()
        }

        myCloseable.quietClose()
    }

    private class MyCloseable : Closeable {

        override fun close() {
            throw IOException("Closed")
        }
    }
}