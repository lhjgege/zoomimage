/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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

package com.github.panpf.zoomimage

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.compose.AsyncImage
import com.github.panpf.sketch.compose.AsyncImagePainter
import com.github.panpf.sketch.compose.AsyncImagePainter.State
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.compose.sketch.internal.onStateOf
import com.github.panpf.zoomimage.compose.sketch.internal.transformOf
import com.github.panpf.zoomimage.compose.subsampling.subsampling
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.compose.zoom.zoom
import com.github.panpf.zoomimage.compose.zoom.zoomScrollBar
import com.github.panpf.zoomimage.sketch.SketchImageSource
import com.github.panpf.zoomimage.sketch.SketchTileBitmapCache
import com.github.panpf.zoomimage.sketch.SketchTileBitmapPool
import kotlin.math.roundToInt


/**
 * An image component that integrates the Sketch image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * SketchZoomAsyncImage(
 *     imageUri = "http://sample.com/sample.jpg",
 *     contentDescription = "view image",
 *     sketch = context.sketch,
 *     modifier = Modifier.fillMaxSize(),
 * )
 * ```
 *
 * @param imageUri [DisplayRequest.uriString] value.
 * @param contentDescription Text used by accessibility services to describe what this image
 *  represents. This should always be provided unless this image is used for decorative purposes,
 *  and does not represent a meaningful action that a user can take.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content.
 * @param placeholder A [Painter] that is displayed while the image is loading.
 * @param error A [Painter] that is displayed when the image request is unsuccessful.
 * @param uriEmpty A [Painter] that is displayed when the request's [DisplayRequest.uriString] is empty.
 * @param onLoading Called when the image request begins loading.
 * @param onSuccess Called when the image request completes successfully.
 * @param onError Called when the image request completes unsuccessfully.
 * @param alignment Optional alignment parameter used to place the [AsyncImagePainter] in the given
 *  bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 *  used if the bounds are a different size from the intrinsic size of the [AsyncImagePainter].
 * @param alpha Optional opacity to be applied to the [AsyncImagePainter] when it is rendered
 *  onscreen.
 * @param colorFilter Optional [ColorFilter] to apply for the [AsyncImagePainter] when it is
 *  rendered onscreen.
 * @param filterQuality Sampling algorithm applied to a bitmap when it is scaled and drawn into the
 *  destination.
 * @param state The state to control zoom
 * @param scrollBar Controls whether scroll bars are displayed and their style
 * @param onLongPress Called when the user long presses the image
 * @param onTap Called when the user taps the image
 */
@Composable
@NonRestartableComposable
fun SketchZoomAsyncImage(
    imageUri: String?,
    contentDescription: String?,
    sketch: Sketch,
    modifier: Modifier = Modifier,
    placeholder: Painter? = null,
    error: Painter? = null,
    uriEmpty: Painter? = error,
    onLoading: ((State.Loading) -> Unit)? = null,
    onSuccess: ((State.Success) -> Unit)? = null,
    onError: ((State.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    state: ZoomState = rememberZoomState(),
    scrollBar: ScrollBarSpec? = ScrollBarSpec.Default,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) = SketchZoomAsyncImage(
    request = DisplayRequest(LocalContext.current, imageUri),
    contentDescription = contentDescription,
    sketch = sketch,
    modifier = modifier,
    transform = transformOf(placeholder, error, uriEmpty),
    onState = onStateOf(onLoading, onSuccess, onError),
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
    filterQuality = filterQuality,
    state = state,
    scrollBar = scrollBar,
    onLongPress = onLongPress,
    onTap = onTap,
)

/**
 * An image component that integrates the Sketch image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * SketchZoomAsyncImage(
 *     request = DisplayRequest(LocalContext.current, "http://sample.com/sample.jpg") {
 *         placeholder(R.drawable.placeholder)
 *         crossfade()
 *     },
 *     contentDescription = "view image",
 *     sketch = context.sketch,
 *     modifier = Modifier.fillMaxSize(),
 * )
 * ```
 *
 * @param request [DisplayRequest].
 * @param contentDescription Text used by accessibility services to describe what this image
 *  represents. This should always be provided unless this image is used for decorative purposes,
 *  and does not represent a meaningful action that a user can take.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content.
 * @param placeholder A [Painter] that is displayed while the image is loading.
 * @param error A [Painter] that is displayed when the image request is unsuccessful.
 * @param uriEmpty A [Painter] that is displayed when the request's [DisplayRequest.uriString] is null.
 * @param onLoading Called when the image request begins loading.
 * @param onSuccess Called when the image request completes successfully.
 * @param onError Called when the image request completes unsuccessfully.
 * @param alignment Optional alignment parameter used to place the [AsyncImagePainter] in the given
 *  bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 *  used if the bounds are a different size from the intrinsic size of the [AsyncImagePainter].
 * @param alpha Optional opacity to be applied to the [AsyncImagePainter] when it is rendered
 *  onscreen.
 * @param colorFilter Optional [ColorFilter] to apply for the [AsyncImagePainter] when it is
 *  rendered onscreen.
 * @param filterQuality Sampling algorithm applied to a bitmap when it is scaled and drawn into the
 *  destination.
 * @param state The state to control zoom
 * @param scrollBar Controls whether scroll bars are displayed and their style
 * @param onLongPress Called when the user long presses the image
 * @param onTap Called when the user taps the image
 */
@Composable
@NonRestartableComposable
fun SketchZoomAsyncImage(
    request: DisplayRequest,
    contentDescription: String?,
    sketch: Sketch,
    modifier: Modifier = Modifier,
    placeholder: Painter? = null,
    error: Painter? = null,
    uriEmpty: Painter? = error,
    onLoading: ((State.Loading) -> Unit)? = null,
    onSuccess: ((State.Success) -> Unit)? = null,
    onError: ((State.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    state: ZoomState = rememberZoomState(),
    scrollBar: ScrollBarSpec? = ScrollBarSpec.Default,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) = SketchZoomAsyncImage(
    request = request,
    contentDescription = contentDescription,
    sketch = sketch,
    modifier = modifier,
    transform = transformOf(placeholder, error, uriEmpty),
    onState = onStateOf(onLoading, onSuccess, onError),
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
    filterQuality = filterQuality,
    state = state,
    scrollBar = scrollBar,
    onLongPress = onLongPress,
    onTap = onTap,
)

/**
 * An image component that integrates the Sketch image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * SketchZoomAsyncImage(
 *     imageUri = "http://sample.com/sample.jpg",
 *     contentDescription = "view image",
 *     sketch = context.sketch,
 *     modifier = Modifier.fillMaxSize(),
 * )
 * ```
 *
 * @param imageUri [DisplayRequest.uriString] value.
 * @param contentDescription Text used by accessibility services to describe what this image
 *  represents. This should always be provided unless this image is used for decorative purposes,
 *  and does not represent a meaningful action that a user can take.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content.
 * @param transform A callback to transform a new [State] before it's applied to the
 *  [AsyncImagePainter]. Typically this is used to modify the state's [Painter].
 * @param onState Called when the state of this painter changes.
 * @param alignment Optional alignment parameter used to place the [AsyncImagePainter] in the given
 *  bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 *  used if the bounds are a different size from the intrinsic size of the [AsyncImagePainter].
 * @param alpha Optional opacity to be applied to the [AsyncImagePainter] when it is rendered
 *  onscreen.
 * @param colorFilter Optional [ColorFilter] to apply for the [AsyncImagePainter] when it is
 *  rendered onscreen.
 * @param filterQuality Sampling algorithm applied to a bitmap when it is scaled and drawn into the
 *  destination.
 * @param state The state to control zoom
 * @param scrollBar Controls whether scroll bars are displayed and their style
 * @param onLongPress Called when the user long presses the image
 * @param onTap Called when the user taps the image
 */
@Composable
@NonRestartableComposable
fun SketchZoomAsyncImage(
    imageUri: String?,
    contentDescription: String?,
    sketch: Sketch,
    modifier: Modifier = Modifier,
    transform: (State) -> State = AsyncImagePainter.DefaultTransform,
    onState: ((State) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    state: ZoomState = rememberZoomState(),
    scrollBar: ScrollBarSpec? = ScrollBarSpec.Default,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) = SketchZoomAsyncImage(
    request = DisplayRequest(LocalContext.current, imageUri),
    contentDescription = contentDescription,
    sketch = sketch,
    modifier = modifier,
    transform = transform,
    onState = onState,
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
    filterQuality = filterQuality,
    state = state,
    scrollBar = scrollBar,
    onLongPress = onLongPress,
    onTap = onTap,
)


/**
 * An image component that integrates the Sketch image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * SketchZoomAsyncImage(
 *     request = DisplayRequest(LocalContext.current, "http://sample.com/sample.jpg") {
 *         placeholder(R.drawable.placeholder)
 *         crossfade()
 *     },
 *     contentDescription = "view image",
 *     sketch = context.sketch,
 *     modifier = Modifier.fillMaxSize(),
 * )
 * ```
 *
 * @param request [DisplayRequest].
 * @param contentDescription Text used by accessibility services to describe what this image
 *  represents. This should always be provided unless this image is used for decorative purposes,
 *  and does not represent a meaningful action that a user can take.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content.
 * @param transform A callback to transform a new [State] before it's applied to the
 *  [AsyncImagePainter]. Typically this is used to modify the state's [Painter].
 * @param onState Called when the state of this painter changes.
 * @param alignment Optional alignment parameter used to place the [AsyncImagePainter] in the given
 *  bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 *  used if the bounds are a different size from the intrinsic size of the [AsyncImagePainter].
 * @param alpha Optional opacity to be applied to the [AsyncImagePainter] when it is rendered
 *  onscreen.
 * @param colorFilter Optional [ColorFilter] to apply for the [AsyncImagePainter] when it is
 *  rendered onscreen.
 * @param filterQuality Sampling algorithm applied to a bitmap when it is scaled and drawn into the
 *  destination.
 * @param state The state to control zoom
 * @param scrollBar Controls whether scroll bars are displayed and their style
 * @param onLongPress Called when the user long presses the image
 * @param onTap Called when the user taps the image
 */
@Composable
fun SketchZoomAsyncImage(
    request: DisplayRequest,
    contentDescription: String?,
    sketch: Sketch,
    modifier: Modifier = Modifier,
    transform: (State) -> State = AsyncImagePainter.DefaultTransform,
    onState: ((State) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    state: ZoomState = rememberZoomState(),
    scrollBar: ScrollBarSpec? = ScrollBarSpec.Default,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) {
    state.zoomable.contentScale = contentScale
    state.zoomable.alignment = alignment

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        state.subsampling.tileBitmapPool = SketchTileBitmapPool(sketch, "SketchZoomAsyncImage")
        state.subsampling.tileBitmapCache = SketchTileBitmapCache(sketch, "SketchZoomAsyncImage")
    }

    AsyncImage(
        request = request,
        contentDescription = contentDescription,
        sketch = sketch,
        transform = transform,
        onState = { loadState ->
            onState(context, sketch, state, request, loadState)
            onState?.invoke(loadState)
        },
        alignment = Alignment.TopStart,
        contentScale = ContentScale.None,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality,
        clipToBounds = false,
        modifier = modifier
            .zoomScrollBar(state.zoomable, scrollBar)
            .zoom(state.zoomable, onLongPress = onLongPress, onTap = onTap)
            .subsampling(state.zoomable, state.subsampling),
    )
}

private fun onState(
    context: Context,
    sketch: Sketch,
    state: ZoomState,
    request: DisplayRequest,
    loadState: State,
) {
    state.zoomable.logger.d { "SketchZoomAsyncImage. onState. state=${loadState.name}. uri='${request.uriString}'" }
    val zoomableState = state.zoomable
    val subsamplingState = state.subsampling
    val painterSize = loadState.painter?.intrinsicSize?.roundToIntSize()?.takeIf { it.isNotEmpty() }
    zoomableState.contentSize = painterSize ?: IntSize.Zero

    when (loadState) {
        is State.Success -> {
            subsamplingState.ignoreExifOrientation = request.ignoreExifOrientation
            subsamplingState.disabledTileBitmapCache =
                request.memoryCachePolicy != CachePolicy.ENABLED
            subsamplingState.disabledTileBitmapReuse = request.disallowReuseBitmap
            val imageSource = SketchImageSource(context, sketch, request.uriString)
            subsamplingState.setImageSource(imageSource)
        }

        else -> {
            subsamplingState.setImageSource(null)
        }
    }
}

private val State.name: String
    get() = when (this) {
        is State.Loading -> "Loading"
        is State.Success -> "Success"
        is State.Error -> "Error"
        is State.Empty -> "Empty"
    }

private fun Size.roundToIntSize(): IntSize {
    return IntSize(width.roundToInt(), height.roundToInt())
}

private fun IntSize.isNotEmpty(): Boolean = width > 0 && height > 0