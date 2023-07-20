package com.github.panpf.zoomimage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.LocalImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.NullRequestDataException
import com.github.panpf.zoomimage.coil.internal.CoilImageSource
import com.github.panpf.zoomimage.coil.internal.CoilTileMemoryCache
import com.github.panpf.zoomimage.compose.ScrollBarSpec
import com.github.panpf.zoomimage.compose.internal.isNotEmpty
import com.github.panpf.zoomimage.subsampling.BindZoomableStateAndSubsamplingState
import com.github.panpf.zoomimage.subsampling.SubsamplingState
import com.github.panpf.zoomimage.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.subsampling.subsampling
import kotlin.math.roundToInt


@Composable
fun rememberCoilZoomAsyncImageLogger(tag: String = "CoilZoomAsyncImage", level: Int = Logger.INFO): Logger =
    remember {
        Logger(tag = tag).apply { this.level = level }
    }

@Composable
@NonRestartableComposable
fun CoilZoomAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: Painter? = null,
    error: Painter? = null,
    fallback: Painter? = error,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    imageLoader: ImageLoader = LocalImageLoader.current,
    logger: Logger = rememberCoilZoomAsyncImageLogger(),
    zoomableState: ZoomableState = rememberZoomableState(logger),
    subsamplingState: SubsamplingState = rememberSubsamplingState(logger),
    scrollBarSpec: ScrollBarSpec? = ScrollBarSpec.Default,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) = CoilZoomAsyncImage(
    model = model,
    contentDescription = contentDescription,
    modifier = modifier,
    transform = transformOf(placeholder, error, fallback),
    onState = onStateOf(onLoading, onSuccess, onError),
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
    filterQuality = filterQuality,
    imageLoader = imageLoader,
    logger = logger,
    zoomableState = zoomableState,
    subsamplingState = subsamplingState,
    scrollBarSpec = scrollBarSpec,
    onLongPress = onLongPress,
    onTap = onTap,
)

@Composable
fun CoilZoomAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    transform: (AsyncImagePainter.State) -> AsyncImagePainter.State = AsyncImagePainter.DefaultTransform,
    onState: ((AsyncImagePainter.State) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    imageLoader: ImageLoader = LocalImageLoader.current,
    logger: Logger = rememberCoilZoomAsyncImageLogger(),
    zoomableState: ZoomableState = rememberZoomableState(logger),
    subsamplingState: SubsamplingState = rememberSubsamplingState(logger),
    scrollBarSpec: ScrollBarSpec? = ScrollBarSpec.Default,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) {
    if (zoomableState.contentAlignment != alignment) {
        zoomableState.contentAlignment = alignment
    }
    if (zoomableState.contentScale != contentScale) {
        zoomableState.contentScale = contentScale
    }

    BindZoomableStateAndSubsamplingState(zoomableState, subsamplingState)

    LaunchedEffect(Unit) {
        subsamplingState.tileMemoryCache = CoilTileMemoryCache(imageLoader)
    }

    val modifier1 = modifier
        .clipToBounds()
        .let { if (scrollBarSpec != null) it.zoomScrollBar(zoomableState, scrollBarSpec) else it }
        .zoomable(state = zoomableState, onLongPress = onLongPress, onTap = onTap)
        .graphicsLayer {
            val transform1 = zoomableState.userTransform
            scaleX = transform1.scaleX
            scaleY = transform1.scaleY
            rotationZ = transform1.rotation
            translationX = transform1.offsetX
            translationY = transform1.offsetY
            transformOrigin = transform1.origin
        }
        .subsampling(zoomableState = zoomableState, subsamplingState = subsamplingState)

    val request = requestOf(model)
    AsyncImage(
        model = request,
        imageLoader = imageLoader,
        contentDescription = contentDescription,
        modifier = modifier1,
        transform = transform,
        onState = { state ->
            onState(logger, state, imageLoader, zoomableState, subsamplingState, request)
            onState?.invoke(state)
        },
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality
    )
}

private fun onState(
    logger: Logger,
    state: AsyncImagePainter.State,
    imageLoader: ImageLoader,
    zoomableState: ZoomableState,
    subsamplingState: SubsamplingState,
    request: ImageRequest
) {
    logger.d("onState. state=${state.name}. data: ${request.data}")
    val painterSize = state.painter?.intrinsicSize?.roundToIntSize()
    val containerSize = zoomableState.containerSize
    val newContentSize = when {
        painterSize != null -> painterSize
        containerSize.isNotEmpty() -> containerSize
        else -> IntSize.Zero
    }
    if (zoomableState.contentSize != newContentSize) {
        zoomableState.contentSize = newContentSize
    }

    when (state) {
        is AsyncImagePainter.State.Success -> {
            subsamplingState.disableMemoryCache =
                request.memoryCachePolicy != CachePolicy.ENABLED
            subsamplingState.setImageSource(CoilImageSource(imageLoader, request))
        }

        else -> {
            subsamplingState.setImageSource(null)
        }
    }
}

val AsyncImagePainter.State.name: String
    get() = when (this) {
        is AsyncImagePainter.State.Loading -> "Loading"
        is AsyncImagePainter.State.Success -> "Success"
        is AsyncImagePainter.State.Error -> "Error"
        is AsyncImagePainter.State.Empty -> "Empty"
    }


/** Create an [ImageRequest] from the [model]. */
@Composable
@ReadOnlyComposable
internal fun requestOf(model: Any?): ImageRequest {
    if (model is ImageRequest) {
        return model
    } else {
        return ImageRequest.Builder(LocalContext.current).data(model).build()
    }
}

@Stable
private fun transformOf(
    placeholder: Painter?,
    error: Painter?,
    uriEmpty: Painter?,
): (AsyncImagePainter.State) -> AsyncImagePainter.State {
    return if (placeholder != null || error != null || uriEmpty != null) {
        { state ->
            when (state) {
                is AsyncImagePainter.State.Loading -> {
                    if (placeholder != null) state.copy(painter = placeholder) else state
                }

                is AsyncImagePainter.State.Error -> if (state.result.throwable is NullRequestDataException) {
                    if (uriEmpty != null) state.copy(painter = uriEmpty) else state
                } else {
                    if (error != null) state.copy(painter = error) else state
                }

                else -> state
            }
        }
    } else {
        AsyncImagePainter.DefaultTransform
    }
}

@Stable
private fun onStateOf(
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)?,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)?,
    onError: ((AsyncImagePainter.State.Error) -> Unit)?,
): ((AsyncImagePainter.State) -> Unit)? {
    return if (onLoading != null || onSuccess != null || onError != null) {
        { state ->
            when (state) {
                is AsyncImagePainter.State.Loading -> onLoading?.invoke(state)
                is AsyncImagePainter.State.Success -> onSuccess?.invoke(state)
                is AsyncImagePainter.State.Error -> onError?.invoke(state)
                is AsyncImagePainter.State.Empty -> {}
            }
        }
    } else {
        null
    }
}

private fun Size.roundToIntSize(): IntSize {
    return IntSize(width.roundToInt(), height.roundToInt())
}