/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.panpf.zoomimage.view.zoom.internal

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView.ScaleType
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.ScrollEdge
import com.github.panpf.zoomimage.core.IntSizeCompat
import com.github.panpf.zoomimage.core.OffsetCompat
import com.github.panpf.zoomimage.core.ScaleFactorCompat
import com.github.panpf.zoomimage.core.TransformCompat
import com.github.panpf.zoomimage.core.div
import com.github.panpf.zoomimage.core.internal.DEFAULT_MEDIUM_SCALE_MULTIPLE
import com.github.panpf.zoomimage.core.internal.calculateNextStepScale
import com.github.panpf.zoomimage.core.internal.computeUserScales
import com.github.panpf.zoomimage.core.isEmpty
import com.github.panpf.zoomimage.core.rotate
import com.github.panpf.zoomimage.view.zoom.OnDragFlingListener
import com.github.panpf.zoomimage.view.zoom.OnDrawableSizeChangeListener
import com.github.panpf.zoomimage.view.zoom.OnMatrixChangeListener
import com.github.panpf.zoomimage.view.zoom.OnRotateChangeListener
import com.github.panpf.zoomimage.view.zoom.OnScaleChangeListener
import com.github.panpf.zoomimage.view.zoom.OnViewDragListener
import com.github.panpf.zoomimage.view.zoom.OnViewLongPressListener
import com.github.panpf.zoomimage.view.zoom.OnViewSizeChangeListener
import com.github.panpf.zoomimage.view.zoom.OnViewTapListener
import com.github.panpf.zoomimage.view.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.view.zoom.ZoomAnimationSpec

/**
 * Based https://github.com/Baseflow/PhotoView git 565505d5 20210120
 */
class ZoomEngine constructor(logger: Logger, val view: View) {

    private val logger: Logger = logger.newLogger(module = "ZoomEngine")
    private val tapHelper = TapHelper(view.context, this)
    private val scaleDragHelper = ScaleDragHelper(
        context = view.context,
        logger = logger,
        engine = this,
        onUpdateMatrix = {
            scrollBarHelper?.onMatrixChanged()
            onMatrixChangeListenerList?.forEach { listener ->
                listener.onMatrixChanged()
            }
        },
        onViewDrag = { dx: Float, dy: Float ->
            onViewDragListenerList?.forEach {
                it.onDrag(dx, dy)
            }
        },
        onDragFling = { velocityX: Float, velocityY: Float ->
            onDragFlingListenerList?.forEach {
                it.onFling(velocityX, velocityY)
            }
        },
        onScaleChanged = { scaleFactor: Float, focusX: Float, focusY: Float ->
            onScaleChangeListenerList?.forEach {
                it.onScaleChanged(scaleFactor, focusX, focusY)
            }
        }
    )
    private var scrollBarHelper: ScrollBarHelper? = null
    private var _rotateDegrees = 0

    private var onMatrixChangeListenerList: MutableSet<OnMatrixChangeListener>? = null
    private var onRotateChangeListenerList: MutableSet<OnRotateChangeListener>? = null
    private var onDragFlingListenerList: MutableSet<OnDragFlingListener>? = null
    private var onViewDragListenerList: MutableSet<OnViewDragListener>? = null
    private var onScaleChangeListenerList: MutableSet<OnScaleChangeListener>? = null
    private var onViewSizeChangeListenerList: MutableSet<OnViewSizeChangeListener>? = null
    private var onDrawableSizeChangeListenerList: MutableSet<OnDrawableSizeChangeListener>? = null

    /** Allows the parent ViewGroup to intercept events while sliding to an edge */
    var allowParentInterceptOnEdge: Boolean = true
    var onViewLongPressListener: OnViewLongPressListener? = null
    var onViewTapListener: OnViewTapListener? = null
    var viewSize = IntSizeCompat.Zero
        internal set(value) {
            if (field != value) {
                field = value
                reset()
                onViewSizeChangeListenerList?.forEach {
                    it.onSizeChanged()
                }
            }
        }

    /**
     * Dimensions of the original image, which is used to calculate the scale of double-click scaling
     */
    var imageSize = IntSizeCompat.Zero
        internal set(value) {
            if (field != value) {
                field = value
                reset()
            }
        }
    var drawableSize = IntSizeCompat.Zero
        internal set(value) {
            if (field != value) {
                field = value
                reset()
                onDrawableSizeChangeListenerList?.forEach {
                    it.onSizeChanged()
                }
            }
        }
    var scaleType: ScaleType = ScaleType.FIT_CENTER
        internal set(value) {
            if (field != value) {
                field = value
                reset()
            }
        }
    var readMode: ReadMode? = null
        internal set(value) {
            if (field != value) {
                field = value
                reset()
            }
        }
    var defaultMediumScaleMultiple: Float = DEFAULT_MEDIUM_SCALE_MULTIPLE
        internal set(value) {
            if (field != value) {
                field = value
                reset()
            }
        }
    var scrollBarSpec: ScrollBarSpec? = ScrollBarSpec.Default
        internal set(value) {
            if (field != value) {
                field = value
                resetScrollBarHelper()
            }
        }
    var animationSpec: ZoomAnimationSpec = ZoomAnimationSpec.Default
    var threeStepScale: Boolean = false
        internal set
    var rubberBandScale: Boolean = true
        internal set

    var minUserScale: Float = 1.0f
        private set

    var mediumUserScale: Float = 1.0f
        private set

    var maxUserScale: Float = 1.0f
        private set

    /**
     * Initial scale and translate for base matrix
     */
    var baseInitialTransform: TransformCompat = TransformCompat.Origin
        private set

    /**
     * Initial scale and translate for user matrix
     */
    var userInitialTransform: TransformCompat = TransformCompat.Origin
        private set

//    var displayTransform: TransformCompat = TransformCompat.Origin
//        private set


    /**************************************** Internal ********************************************/

    init {
        reset()
        resetScrollBarHelper()
    }

    private fun reset() {
        val drawableSize = drawableSize
        val imageSize = imageSize
        val viewSize = viewSize
        if (drawableSize.isEmpty() || viewSize.isEmpty()) {
            minUserScale = 1.0f
            mediumUserScale = 1.0f
            maxUserScale = 1.0f
            baseInitialTransform = TransformCompat.Origin
            userInitialTransform = TransformCompat.Origin
        } else {
            val rotatedDrawableSize = drawableSize.rotate(rotateDegrees)
            val rotatedImageSize = imageSize.rotate(rotateDegrees)
            val userStepScales = computeUserScales(
                contentSize = rotatedDrawableSize,
                contentOriginSize = rotatedImageSize,
                containerSize = viewSize,
                scaleMode = scaleType.toScaleMode(),
                baseScale = scaleType.computeScaleFactor(
                    srcSize = rotatedDrawableSize,
                    dstSize = viewSize
                ),
                defaultMediumScaleMultiple = defaultMediumScaleMultiple
            )
            minUserScale = userStepScales[0]
            mediumUserScale = userStepScales[1]
            maxUserScale = userStepScales[2]
            baseInitialTransform = scaleType
                .computeTransform(srcSize = rotatedDrawableSize, dstSize = viewSize)
            val readMode = scaleType.supportReadMode()
                    && readMode?.should(srcSize = rotatedDrawableSize, dstSize = viewSize) == true
            userInitialTransform = if (readMode) {
                val readModeTransform = computeReadModeTransform(
                    srcSize = rotatedDrawableSize,
                    dstSize = viewSize,
                    scaleType = scaleType,
                )
                readModeTransform.div(baseInitialTransform.scale)
            } else {
                TransformCompat.Origin
            }
        }
        scaleDragHelper.reset()
        logger.d {
            "reset. viewSize=$viewSize, " +
                    "imageSize=$imageSize, " +
                    "drawableSize=$drawableSize, " +
                    "rotateDegrees=$rotateDegrees, " +
                    "scaleType=$scaleType, " +
                    "readMode=$readMode, " +
                    "minUserScale=$minUserScale, " +
                    "mediumUserScale=$mediumUserScale, " +
                    "maxUserScale=$maxUserScale, " +
                    "baseInitialTransform=$baseInitialTransform, " +
                    "userInitialTransform=$userInitialTransform"
        }
    }

    private fun resetScrollBarHelper() {
        scrollBarHelper?.cancel()
        scrollBarHelper = null
        val scrollBar = scrollBarSpec
        if (scrollBar != null) {
            scrollBarHelper = ScrollBarHelper(view.context, this@ZoomEngine, scrollBar)
        }
    }

    internal fun clean() {
        scaleDragHelper.clean()
    }

    internal fun onDraw(canvas: Canvas) {
        scrollBarHelper?.onDraw(canvas)
    }

    internal fun onTouchEvent(event: MotionEvent): Boolean {
        if (drawableSize.isEmpty()) return false
        val scaleAndDragConsumed = scaleDragHelper.onTouchEvent(event)
        val tapConsumed = tapHelper.onTouchEvent(event)
        return scaleAndDragConsumed || tapConsumed
    }


    /*************************************** Interaction ******************************************/

    /**
     * Locate to the location specified on the drawable image. You don't have to worry about scaling and rotation
     *
     * @param x Drawable the x coordinate on the diagram
     * @param y Drawable the y-coordinate on the diagram
     */
    fun location(x: Float, y: Float, animate: Boolean = false) {
        scaleDragHelper.location(x, y, animate)
    }

    /**
     * Scale to the specified scale. You don't have to worry about rotation degrees
     *
     * @param focalX  Scale the x coordinate of the center point on the view
     * @param focalY  Scale the y coordinate of the center point on the view
     */
    fun scale(newScale: Float, focalX: Float, focalY: Float, animate: Boolean) {
        val currentScale = userScale
        if (newScale > currentScale) {
            scaleDragHelper.scale(
                newUserScale = newScale.coerceIn(minUserScale, maxUserScale),
                focalX = focalX,
                focalY = focalY,
                animate = animate
            )
        } else {
            scaleDragHelper.scale(
                newUserScale = newScale.coerceIn(minUserScale, maxUserScale),
                focalX = (view.right / 2).toFloat(),
                focalY = (view.bottom / 2).toFloat(),
                animate = animate
            )
        }
    }

    /**
     * Scale to the specified scale. You don't have to worry about rotation degrees
     */
    fun scale(scale: Float, animate: Boolean = false) {
        scale(scale, (view.right / 2).toFloat(), (view.bottom / 2).toFloat(), animate)
    }

    /**
     * Rotate the image to the specified degrees
     *
     * @param degrees Rotation degrees, can only be 90°, 180°, 270°, 360°
     */
    fun rotateTo(degrees: Int) {
        require(degrees % 90 == 0) { "degrees must be in multiples of 90: $degrees" }
        if (_rotateDegrees == degrees) return

        var newDegrees = degrees % 360
        if (newDegrees <= 0) {
            newDegrees = 360 - newDegrees
        }
        _rotateDegrees = newDegrees
        reset()
        onRotateChangeListenerList?.forEach {
            it.onRotateChanged(newDegrees)
        }
    }

    /**
     * Rotate an degrees based on the current rotation degrees
     *
     * @param addDegrees Rotation degrees, can only be 90°, 180°, 270°, 360°
     */
    fun rotateBy(addDegrees: Int) {
        return rotateTo(_rotateDegrees + addDegrees)
    }

    fun getNextStepScale(): Float {
        val stepScales = if (threeStepScale) {
            floatArrayOf(minUserScale, mediumUserScale, maxUserScale)
        } else {
            floatArrayOf(minUserScale, mediumUserScale)
        }
        return calculateNextStepScale(stepScales, userScale)
    }


    /***************************************** Information ****************************************/

    val rotateDegrees: Int
        get() = _rotateDegrees

    fun canScroll(horizontal: Boolean, direction: Int): Boolean =
        scaleDragHelper.canScroll(horizontal, direction)

    val scrollEdge: ScrollEdge
        get() = scaleDragHelper.scrollEdge

    val isScaling: Boolean
        get() = scaleDragHelper.isScaling

    val userScale: Float
        get() = scaleDragHelper.userScale
    val userOffset: OffsetCompat
        get() = scaleDragHelper.userOffset

    val baseScale: ScaleFactorCompat
        get() = scaleDragHelper.baseScale
    val baseOffset: OffsetCompat
        get() = scaleDragHelper.baseOffset

    val displayScale: ScaleFactorCompat
        get() = scaleDragHelper.displayScale
    val displayOffset: OffsetCompat
        get() = scaleDragHelper.displayOffset

    fun getDisplayMatrix(matrix: Matrix) = scaleDragHelper.getDisplayMatrix(matrix)

    fun getDisplayRect(rectF: RectF) = scaleDragHelper.getDisplayRect(rectF)

    fun getDisplayRect(): RectF = scaleDragHelper.getDisplayRect()

    /** Gets the area that the user can see on the drawable (not affected by rotation) */
    fun getVisibleRect(rect: Rect) = scaleDragHelper.getVisibleRect(rect)

    /** Gets the area that the user can see on the drawable (not affected by rotation) */
    fun getVisibleRect(): Rect = scaleDragHelper.getVisibleRect()

    fun touchPointToDrawablePoint(touchPoint: PointF): Point? {
        return scaleDragHelper.touchPointToDrawablePoint(touchPoint)
    }

    fun addOnMatrixChangeListener(listener: OnMatrixChangeListener) {
        this.onMatrixChangeListenerList = (onMatrixChangeListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
    }

    fun removeOnMatrixChangeListener(listener: OnMatrixChangeListener): Boolean {
        return onMatrixChangeListenerList?.remove(listener) == true
    }

    fun addOnRotateChangeListener(listener: OnRotateChangeListener) {
        this.onRotateChangeListenerList = (onRotateChangeListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
    }

    fun removeOnRotateChangeListener(listener: OnRotateChangeListener): Boolean {
        return onRotateChangeListenerList?.remove(listener) == true
    }

    fun addOnDragFlingListener(listener: OnDragFlingListener) {
        this.onDragFlingListenerList = (onDragFlingListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
    }

    fun removeOnDragFlingListener(listener: OnDragFlingListener): Boolean {
        return onDragFlingListenerList?.remove(listener) == true
    }

    fun addOnViewDragListener(listener: OnViewDragListener) {
        this.onViewDragListenerList = (onViewDragListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
    }

    fun removeOnViewDragListener(listener: OnViewDragListener): Boolean {
        return onViewDragListenerList?.remove(listener) == true
    }

    fun addOnScaleChangeListener(listener: OnScaleChangeListener) {
        this.onScaleChangeListenerList = (onScaleChangeListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
    }

    fun removeOnScaleChangeListener(listener: OnScaleChangeListener): Boolean {
        return onScaleChangeListenerList?.remove(listener) == true
    }

    fun addOnViewSizeChangeListener(listener: OnViewSizeChangeListener) {
        this.onViewSizeChangeListenerList =
            (onViewSizeChangeListenerList ?: LinkedHashSet()).apply {
                add(listener)
            }
    }

    fun removeOnViewSizeChangeListener(listener: OnViewSizeChangeListener): Boolean {
        return onViewSizeChangeListenerList?.remove(listener) == true
    }

    fun addOnDrawableSizeChangeListener(listener: OnDrawableSizeChangeListener) {
        this.onDrawableSizeChangeListenerList =
            (onDrawableSizeChangeListenerList ?: LinkedHashSet()).apply {
                add(listener)
            }
    }

    fun removeOnDrawableSizeChangeListener(listener: OnDrawableSizeChangeListener): Boolean {
        return onDrawableSizeChangeListenerList?.remove(listener) == true
    }
}