package com.github.panpf.zoomimage.subsampling

import android.graphics.Bitmap

/**
 * A pool of [Bitmap]s that can be reused to avoid triggering the garbage collector.
 */
interface TileBitmapPool {

    /**
     * Puts the specified [bitmap] into the pool.
     *
     * @return If true, it was successfully placed, otherwise call the [bitmap. recycle] method to recycle the [Bitmap].
     * @see android.graphics.Bitmap.isMutable
     * @see android.graphics.Bitmap.recycle
     */
    fun put(bitmap: Bitmap): Boolean

    /**
     * Get a reusable [Bitmap]. Note that all colors are erased before returning
     */
    fun get(width: Int, height: Int, config: Bitmap.Config): Bitmap?
}