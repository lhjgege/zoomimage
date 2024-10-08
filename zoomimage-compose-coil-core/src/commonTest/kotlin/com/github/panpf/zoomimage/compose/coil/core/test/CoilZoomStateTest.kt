package com.github.panpf.zoomimage.compose.coil.core.test

import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.github.panpf.zoomimage.coil.CoilModelToImageSourceImpl
import com.github.panpf.zoomimage.images.coil.TestCoilModelToImageSource
import com.github.panpf.zoomimage.rememberCoilZoomState
import com.github.panpf.zoomimage.test.TestLifecycle
import com.github.panpf.zoomimage.util.Logger
import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CoilZoomStateTest {

    @Test
    fun testRememberCoilZoomState() = runComposeUiTest {
        setContent {
            TestLifecycle {
                val zoomState1 = rememberCoilZoomState()
                assertEquals(
                    expected = "CoilZoomAsyncImage",
                    actual = zoomState1.logger.tag
                )
                assertEquals(
                    expected = listOf(CoilModelToImageSourceImpl()).joinToString { it::class.toString() },
                    actual = zoomState1.modelToImageSources.joinToString { it::class.toString() }
                )

                val modelToImageSources = remember {
                    listOf(TestCoilModelToImageSource()).toImmutableList()
                }
                val zoomState2 = rememberCoilZoomState(
                    modelToImageSources = modelToImageSources,
                )
                assertEquals(
                    expected = listOf(
                        TestCoilModelToImageSource(),
                        CoilModelToImageSourceImpl()
                    ).joinToString { it::class.toString() },
                    actual = zoomState2.modelToImageSources.joinToString { it::class.toString() }
                )

                assertEquals(
                    expected = Logger.Level.Info,
                    actual = zoomState1.logger.level
                )
                val zoomState3 = rememberCoilZoomState(logLevel = Logger.Level.Debug)
                assertEquals(
                    expected = Logger.Level.Debug,
                    actual = zoomState3.logger.level
                )
            }
        }
    }
}