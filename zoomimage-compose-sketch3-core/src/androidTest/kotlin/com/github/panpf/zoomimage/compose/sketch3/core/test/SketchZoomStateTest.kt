package com.github.panpf.zoomimage.compose.sketch3.core.test

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.zoomimage.compose.sketch.SketchComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.compose.sketch.internal.AnimatableSketchComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.compose.sketch.internal.EngineSketchComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.rememberSketchZoomState
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.test.TestLifecycle
import com.github.panpf.zoomimage.util.Logger
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class SketchZoomStateTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun testRememberSketchZoomState() {
        rule.setContent {
            TestLifecycle {
                val zoomState1 = rememberSketchZoomState()
                assertEquals(
                    expected = "SketchZoomAsyncImage",
                    actual = zoomState1.logger.tag
                )

                assertEquals(
                    expected = listOf(
                        AnimatableSketchComposeSubsamplingImageGenerator,
                        EngineSketchComposeSubsamplingImageGenerator
                    ).joinToString { it::class.toString() },
                    actual = zoomState1.subsamplingImageGenerators.joinToString { it::class.toString() }
                )
                val subsamplingImageGenerators = remember {
                    listOf(TestSketchComposeSubsamplingImageGenerator).toImmutableList()
                }
                val zoomState2 = rememberSketchZoomState(
                    subsamplingImageGenerators = subsamplingImageGenerators,
                )
                assertEquals(
                    expected = listOf(
                        TestSketchComposeSubsamplingImageGenerator,
                        AnimatableSketchComposeSubsamplingImageGenerator,
                        EngineSketchComposeSubsamplingImageGenerator
                    ).joinToString { it::class.toString() },
                    actual = zoomState2.subsamplingImageGenerators.joinToString { it::class.toString() }
                )

                assertEquals(
                    expected = Logger.Level.Info,
                    actual = zoomState1.logger.level
                )
                val zoomState3 = rememberSketchZoomState(logLevel = Logger.Level.Debug)
                assertEquals(
                    expected = Logger.Level.Debug,
                    actual = zoomState3.logger.level
                )
            }
        }
    }

    data object TestSketchComposeSubsamplingImageGenerator :
        SketchComposeSubsamplingImageGenerator {

        override suspend fun generateImage(
            sketch: Sketch,
            request: DisplayRequest,
            result: DisplayResult.Success,
            painter: Painter
        ): SubsamplingImageGenerateResult? {
            return null
        }
    }
}