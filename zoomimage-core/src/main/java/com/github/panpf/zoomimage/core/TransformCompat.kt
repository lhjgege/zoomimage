package com.github.panpf.zoomimage.core

import com.github.panpf.zoomimage.core.internal.format

data class TransformCompat(
    val scale: ScaleFactorCompat,
    val offset: OffsetCompat,
    val rotation: Float = 0f,
    val origin: Origin = com.github.panpf.zoomimage.core.Origin.TopStart,
) {

    constructor(
        scaleX: Float,
        scaleY: Float,
        offsetX: Float,
        offsetY: Float,
        rotation: Float = 0f,
        originX: Float = 0f,
        originY: Float = 0f,
    ) : this(
        scale = ScaleFactorCompat(scaleX = scaleX, scaleY = scaleY),
        offset = OffsetCompat(x = offsetX, y = offsetY),
        rotation = rotation,
        origin = Origin(pivotFractionX = originX, pivotFractionY = originY)
    )

    val scaleX: Float
        get() = scale.scaleX
    val scaleY: Float
        get() = scale.scaleY
    val offsetX: Float
        get() = offset.x
    val offsetY: Float
        get() = offset.y
    val originX: Float
        get() = origin.pivotFractionX
    val originY: Float
        get() = origin.pivotFractionY

    companion object {
        val Origin = TransformCompat(
            scale = ScaleFactorCompat(1f, 1f),
            offset = OffsetCompat.Zero,
            rotation = 0f,
            origin = com.github.panpf.zoomimage.core.Origin.TopStart,
        )
    }

    override fun toString(): String {
        return "TransformCompat(" +
                "scale=${scale.toShortString()}, " +
                "offset=${offset.toShortString()}, " +
                "rotation=$rotation, " +
                "origin=${originX.format(2)}x${originY.format(2)}" +
                ")"
    }
}

/**
 * Linearly interpolate between two TransformCompat.
 *
 * The [fraction] argument represents position on the timeline, with 0.0 meaning
 * that the interpolation has not started, returning [start] (or something
 * equivalent to [start]), 1.0 meaning that the interpolation has finished,
 * returning [stop] (or something equivalent to [stop]), and values in between
 * meaning that the interpolation is at the relevant point on the timeline
 * between [start] and [stop]. The interpolation can be extrapolated beyond 0.0 and
 * 1.0, so negative values and values greater than 1.0 are valid (and can
 * easily be generated by curves).
 *
 * Values for [fraction] are usually obtained from an [Animation<Float>], such as
 * an `AnimationController`.
 */
fun lerp(start: TransformCompat, stop: TransformCompat, fraction: Float): TransformCompat {
    require(start.origin == stop.origin) {
        "Transform origin must be the same: start.origin=${start.origin}, stop.origin=${stop.origin}"
    }
    return start.copy(
        scale = lerp(start.scale, stop.scale, fraction),
        offset = lerp(start.offset, stop.offset, fraction),
        rotation = com.github.panpf.zoomimage.core.internal
            .lerp(start.rotation, stop.rotation, fraction),
    )
}

fun TransformCompat.toShortString(): String =
    "(${scale.toShortString()},${offset.toShortString()}," +
            "$rotation,${originX.format(2)}x${originY.format(2)})"

fun TransformCompat.times(scaleFactor: ScaleFactorCompat): TransformCompat {
    return this.copy(
        scale = ScaleFactorCompat(
            scaleX = scale.scaleX * scaleFactor.scaleX,
            scaleY = scale.scaleY * scaleFactor.scaleY,
        ),
        offset = OffsetCompat(
            x = offset.x * scaleFactor.scaleX,
            y = offset.y * scaleFactor.scaleY,
        ),
    )
}

fun TransformCompat.div(scaleFactor: ScaleFactorCompat): TransformCompat {
    return this.copy(
        scale = ScaleFactorCompat(
            scaleX = scale.scaleX / scaleFactor.scaleX,
            scaleY = scale.scaleY / scaleFactor.scaleY,
        ),
        offset = OffsetCompat(
            x = offset.x / scaleFactor.scaleX,
            y = offset.y / scaleFactor.scaleY,
        ),
    )
}

fun TransformCompat.concat(other: TransformCompat): TransformCompat {
    require(this.origin == other.origin) {
        "Transform origin must be the same: this.origin=${this.origin}, other.origin=${other.origin}"
    }
    return this.copy(
        scale = scale.times(other.scale),
        offset = offset + other.offset,
        rotation = rotation + other.rotation,
    )
}