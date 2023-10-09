package com.github.panpf.zoomimage.core.test.subsampling.internal

import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.core.test.internal.cornerA
import com.github.panpf.zoomimage.core.test.internal.cornerB
import com.github.panpf.zoomimage.core.test.internal.cornerC
import com.github.panpf.zoomimage.core.test.internal.cornerD
import com.github.panpf.zoomimage.core.test.internal.corners
import com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper
import com.github.panpf.zoomimage.subsampling.internal.exifOrientationName
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import org.junit.Assert
import org.junit.Test

class ExifOrientationHelperTest {

    @Test
    fun testConstructor() {
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_UNDEFINED)
            .apply {
            Assert.assertEquals(ExifInterface.ORIENTATION_UNDEFINED, exifOrientation)
        }

        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_270)
            .apply {
            Assert.assertEquals(ExifInterface.ORIENTATION_ROTATE_270, exifOrientation)
        }
    }

    @Test
    fun testIsFlipped() {
        Assert.assertFalse(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(
                ExifInterface.ORIENTATION_ROTATE_90
            ).isFlipped)
        Assert.assertTrue(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(
                ExifInterface.ORIENTATION_TRANSPOSE
            ).isFlipped)
        Assert.assertFalse(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(
                ExifInterface.ORIENTATION_ROTATE_180
            ).isFlipped)
        Assert.assertTrue(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(
                ExifInterface.ORIENTATION_FLIP_VERTICAL
            ).isFlipped)
        Assert.assertFalse(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(
                ExifInterface.ORIENTATION_ROTATE_270
            ).isFlipped)
        Assert.assertTrue(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(
                ExifInterface.ORIENTATION_TRANSVERSE
            ).isFlipped)
        Assert.assertTrue(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL
            ).isFlipped)
        Assert.assertFalse(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(
                ExifInterface.ORIENTATION_UNDEFINED
            ).isFlipped)
        Assert.assertFalse(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(
                ExifInterface.ORIENTATION_NORMAL
            ).isFlipped)
        Assert.assertFalse(com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(-1).isFlipped)
        Assert.assertFalse(com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(100).isFlipped)
    }

    @Test
    fun testRotationDegrees() {
        Assert.assertEquals(
            90,
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_90).rotationDegrees
        )
        Assert.assertEquals(
            270,
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_TRANSPOSE).rotationDegrees
        )
        Assert.assertEquals(
            180,
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_180).rotationDegrees
        )
        Assert.assertEquals(
            180,
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_FLIP_VERTICAL).rotationDegrees
        )
        Assert.assertEquals(
            270,
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_270).rotationDegrees
        )
        Assert.assertEquals(
            90,
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_TRANSVERSE).rotationDegrees
        )
        Assert.assertEquals(
            0,
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_FLIP_HORIZONTAL).rotationDegrees
        )
        Assert.assertEquals(
            0,
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_UNDEFINED).rotationDegrees
        )
        Assert.assertEquals(
            0,
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_NORMAL).rotationDegrees
        )
        Assert.assertEquals(0, com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(
            -1
        ).rotationDegrees)
        Assert.assertEquals(0, com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(
            100
        ).rotationDegrees)
    }

    @Test
    fun testApplyToBitmap() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val inBitmap = context.assets.open("sample_dog.jpg").use {
            BitmapFactory.decodeStream(it)
        }
        Assert.assertTrue(
            inBitmap.cornerA != inBitmap.cornerB
                    && inBitmap.cornerA != inBitmap.cornerC
                    && inBitmap.cornerA != inBitmap.cornerD
        )

        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_90)
            .applyToBitmap(inBitmap)!!.let { outBitmap ->
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerD, cornerA, cornerB, cornerC) }.toString(),
                )
            }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_TRANSVERSE)
            .applyToBitmap(inBitmap)!!.let { outBitmap ->
                // Flip horizontally and apply ORIENTATION_ROTATE_90
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerC, cornerB, cornerA, cornerD) }.toString(),
                )
            }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_180)
            .applyToBitmap(inBitmap)!!.let { outBitmap ->
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerC, cornerD, cornerA, cornerB) }.toString(),
                )
            }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_FLIP_VERTICAL)
            .applyToBitmap(inBitmap)!!.let { outBitmap ->
                // Flip horizontally and apply ORIENTATION_ROTATE_180
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerD, cornerC, cornerB, cornerA) }.toString(),
                )
            }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_270)
            .applyToBitmap(inBitmap)!!.let { outBitmap ->
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerB, cornerC, cornerD, cornerA) }.toString(),
                )
            }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_TRANSPOSE)
            .applyToBitmap(inBitmap)!!.let { outBitmap ->
                // Flip horizontally and apply ORIENTATION_ROTATE_270
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerA, cornerD, cornerC, cornerB) }.toString(),
                )
            }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_FLIP_HORIZONTAL)
            .applyToBitmap(inBitmap)!!.let { outBitmap ->
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerB, cornerA, cornerD, cornerC) }.toString(),
                )
            }
        Assert.assertNull(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_UNDEFINED)
                .applyToBitmap(inBitmap)
        )
        Assert.assertNull(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_NORMAL)
                .applyToBitmap(inBitmap)
        )
        Assert.assertNull(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(-1).applyToBitmap(inBitmap)
        )
        Assert.assertNull(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(100).applyToBitmap(inBitmap)
        )
    }

    @Test
    fun testAddToBitmap() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val inBitmap = context.assets.open("sample_dog.jpg").use {
            BitmapFactory.decodeStream(it)
        }
        Assert.assertTrue(
            inBitmap.cornerA != inBitmap.cornerB
                    && inBitmap.cornerA != inBitmap.cornerC
                    && inBitmap.cornerA != inBitmap.cornerD
        )

        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_90)
            .addToBitmap(inBitmap)!!.let { outBitmap ->
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerB, cornerC, cornerD, cornerA) }.toString(),
                )
            }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_TRANSVERSE)
            .addToBitmap(inBitmap)!!.let { outBitmap ->
                // Flip horizontally based on ORIENTATION_ROTATE_90
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerC, cornerB, cornerA, cornerD) }.toString(),
                )
            }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_180)
            .addToBitmap(inBitmap)!!.let { outBitmap ->
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerC, cornerD, cornerA, cornerB) }.toString(),
                )
            }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_FLIP_VERTICAL)
            .addToBitmap(inBitmap)!!.let { outBitmap ->
                // Flip horizontally based on ORIENTATION_ROTATE_180
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerD, cornerC, cornerB, cornerA) }.toString(),
                )
            }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_270)
            .addToBitmap(inBitmap)!!.let { outBitmap ->
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerD, cornerA, cornerB, cornerC) }.toString(),
                )
            }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_TRANSPOSE)
            .addToBitmap(inBitmap)!!.let { outBitmap ->
                // Flip horizontally based on ORIENTATION_ROTATE_270
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerA, cornerD, cornerC, cornerB) }.toString(),
                )
            }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_FLIP_HORIZONTAL)
            .addToBitmap(inBitmap)!!.let { outBitmap ->
                Assert.assertEquals(
                    inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                    inBitmap.corners { listOf(cornerB, cornerA, cornerD, cornerC) }.toString(),
                )
            }
        Assert.assertNull(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_UNDEFINED)
                .addToBitmap(inBitmap)
        )
        Assert.assertNull(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_NORMAL)
                .addToBitmap(inBitmap)
        )
        Assert.assertNull(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(-1).addToBitmap(inBitmap)
        )
        Assert.assertNull(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(100).addToBitmap(inBitmap)
        )
    }

    @Test
    fun testAddAndApplyToBitmap() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val inBitmap = context.assets.open("sample_dog.jpg").use {
            BitmapFactory.decodeStream(it)
        }
        Assert.assertTrue(
            inBitmap.cornerA != inBitmap.cornerB
                    && inBitmap.cornerA != inBitmap.cornerC
                    && inBitmap.cornerA != inBitmap.cornerD
        )

        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_90)
            .applyToBitmap(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_90)
                .addToBitmap(inBitmap)!!,
        )!!.let { outBitmap ->
            Assert.assertEquals(
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
            )
        }

        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_TRANSVERSE)
            .applyToBitmap(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_TRANSVERSE)
                .addToBitmap(inBitmap)!!
        )!!.let { outBitmap ->
            Assert.assertEquals(
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
            )
        }

        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_180)
            .applyToBitmap(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_180)
                .addToBitmap(inBitmap)!!
        )!!.let { outBitmap ->
            Assert.assertEquals(
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
            )
        }

        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_FLIP_VERTICAL)
            .applyToBitmap(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_FLIP_VERTICAL)
                .addToBitmap(inBitmap)!!
        )!!.let { outBitmap ->
            Assert.assertEquals(
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
            )
        }

        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_270)
            .applyToBitmap(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_270)
                .addToBitmap(inBitmap)!!
        )!!.let { outBitmap ->
            Assert.assertEquals(
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
            )
        }

        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_TRANSPOSE)
            .applyToBitmap(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_TRANSPOSE)
                .addToBitmap(inBitmap)!!
        )!!.let { outBitmap ->
            Assert.assertEquals(
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
            )
        }

        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_FLIP_HORIZONTAL)
            .applyToBitmap(
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_FLIP_HORIZONTAL)
                .addToBitmap(inBitmap)!!
        )!!.let { outBitmap ->
            Assert.assertEquals(
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                outBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
                inBitmap.corners { listOf(cornerA, cornerB, cornerC, cornerD) }.toString(),
            )
        }
    }

    @Test
    fun testApplyToSize() {
        Assert.assertEquals(
            IntSizeCompat(50, 100),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_90)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(50, 100),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_TRANSVERSE)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(100, 50),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_180)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(100, 50),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_FLIP_VERTICAL)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(50, 100),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_270)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(50, 100),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_TRANSPOSE)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(100, 50),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_UNDEFINED)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(100, 50),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_NORMAL)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(100, 50),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_FLIP_HORIZONTAL)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(100, 50),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(-1)
                .applyToSize(IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntSizeCompat(100, 50),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(100)
                .applyToSize(IntSizeCompat(100, 50))
        )
    }

    @Test
    fun testAddToSize() {
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_90)
            .apply {
            Assert.assertEquals(IntSizeCompat(50, 100), addToSize(IntSizeCompat(100, 50)))
        }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_TRANSVERSE)
            .apply {
            Assert.assertEquals(IntSizeCompat(50, 100), addToSize(IntSizeCompat(100, 50)))
        }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_180)
            .apply {
            Assert.assertEquals(IntSizeCompat(100, 50), addToSize(IntSizeCompat(100, 50)))
        }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_FLIP_VERTICAL)
            .apply {
            Assert.assertEquals(IntSizeCompat(100, 50), addToSize(IntSizeCompat(100, 50)))
        }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_270)
            .apply {
            Assert.assertEquals(IntSizeCompat(50, 100), addToSize(IntSizeCompat(100, 50)))
        }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_TRANSPOSE)
            .apply {
            Assert.assertEquals(IntSizeCompat(50, 100), addToSize(IntSizeCompat(100, 50)))
        }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_UNDEFINED)
            .apply {
            Assert.assertEquals(IntSizeCompat(100, 50), addToSize(IntSizeCompat(100, 50)))
        }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_NORMAL)
            .apply {
            Assert.assertEquals(IntSizeCompat(100, 50), addToSize(IntSizeCompat(100, 50)))
        }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_FLIP_HORIZONTAL)
            .apply {
            Assert.assertEquals(IntSizeCompat(100, 50), addToSize(IntSizeCompat(100, 50)))
        }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(-1).apply {
            Assert.assertEquals(IntSizeCompat(100, 50), addToSize(IntSizeCompat(100, 50)))
        }
        com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(100).apply {
            Assert.assertEquals(IntSizeCompat(100, 50), addToSize(IntSizeCompat(100, 50)))
        }
    }
    
    @Test
    fun testAddToRect() {
        Assert.assertEquals(
            IntRectCompat(10, 50, 30, 60),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_90)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(20, 50, 40, 60),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_TRANSVERSE)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(50, 20, 60, 40),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_180)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(40, 20, 50, 40),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_FLIP_VERTICAL)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(20, 40, 40, 50),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_ROTATE_270)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(10, 40, 30, 50),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_TRANSPOSE)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(50, 10, 60, 30),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_FLIP_HORIZONTAL)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(40, 10, 50, 30),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_UNDEFINED)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(40, 10, 50, 30),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(ExifInterface.ORIENTATION_NORMAL)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(40, 10, 50, 30),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(-1)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
        Assert.assertEquals(
            IntRectCompat(40, 10, 50, 30),
            com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper(100)
                .addToRect(IntRectCompat(40, 10, 50, 30), IntSizeCompat(100, 50))
        )
    }

    @Test
    fun testExifOrientationName() {
        Assert.assertEquals("ROTATE_90",
            com.github.panpf.zoomimage.subsampling.internal.exifOrientationName(ExifInterface.ORIENTATION_ROTATE_90)
        )
        Assert.assertEquals("TRANSPOSE",
            com.github.panpf.zoomimage.subsampling.internal.exifOrientationName(ExifInterface.ORIENTATION_TRANSPOSE)
        )
        Assert.assertEquals("ROTATE_180",
            com.github.panpf.zoomimage.subsampling.internal.exifOrientationName(ExifInterface.ORIENTATION_ROTATE_180)
        )
        Assert.assertEquals(
            "FLIP_VERTICAL",
            com.github.panpf.zoomimage.subsampling.internal.exifOrientationName(ExifInterface.ORIENTATION_FLIP_VERTICAL)
        )
        Assert.assertEquals("ROTATE_270",
            com.github.panpf.zoomimage.subsampling.internal.exifOrientationName(ExifInterface.ORIENTATION_ROTATE_270)
        )
        Assert.assertEquals("TRANSVERSE",
            com.github.panpf.zoomimage.subsampling.internal.exifOrientationName(ExifInterface.ORIENTATION_TRANSVERSE)
        )
        Assert.assertEquals(
            "FLIP_HORIZONTAL",
            com.github.panpf.zoomimage.subsampling.internal.exifOrientationName(ExifInterface.ORIENTATION_FLIP_HORIZONTAL)
        )
        Assert.assertEquals("UNDEFINED",
            com.github.panpf.zoomimage.subsampling.internal.exifOrientationName(ExifInterface.ORIENTATION_UNDEFINED)
        )
        Assert.assertEquals("NORMAL",
            com.github.panpf.zoomimage.subsampling.internal.exifOrientationName(ExifInterface.ORIENTATION_NORMAL)
        )
        Assert.assertEquals("-1",
            com.github.panpf.zoomimage.subsampling.internal.exifOrientationName(-1)
        )
        Assert.assertEquals("100",
            com.github.panpf.zoomimage.subsampling.internal.exifOrientationName(100)
        )
    }
}