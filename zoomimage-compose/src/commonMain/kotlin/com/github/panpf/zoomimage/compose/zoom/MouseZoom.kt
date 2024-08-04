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

package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import com.github.panpf.zoomimage.zoom.GestureType
import kotlinx.coroutines.launch


/**
 * Add mouse zoom support
 */
fun Modifier.mouseZoom(
    zoomable: ZoomableState,
): Modifier = this.then(MouseZoomElement(zoomable))


internal data class MouseZoomElement(
    val zoomable: ZoomableState,
) : ModifierNodeElement<MouseZoomNode>() {

    override fun create(): MouseZoomNode {
        return MouseZoomNode(zoomable)
    }

    override fun update(node: MouseZoomNode) {
        node.update(zoomable)
    }
}

internal class MouseZoomNode(
    var zoomable: ZoomableState,
) : DelegatingNode(), CompositionLocalConsumerModifierNode {

    private var pointerPosition = Offset(0f, 0f)

    private val positionDelegate = delegate(SuspendingPointerInputModifierNode {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                if (event.type == PointerEventType.Move) {
                    if (zoomable.disabledGestureTypes and GestureType.MOUSE_SCROLL_SCALE == 0) {
                        val position = event.changes.first().position
                        pointerPosition = position
                    }
                }
            }
        }
    })
    private val scrollDelegate = delegate(SuspendingPointerInputModifierNode {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                if (event.type == PointerEventType.Scroll) {
                    if (zoomable.disabledGestureTypes and GestureType.MOUSE_SCROLL_SCALE == 0) {
                        coroutineScope.launch {
                            val newScale = if (!zoomable.reverseMouseWheelScale) {
                                zoomable.transform.scaleX - (event.changes.first().scrollDelta.y * 0.33f)
                            } else {
                                zoomable.transform.scaleX + (event.changes.first().scrollDelta.y * 0.33f)
                            }
                            val contentPosition = zoomable.touchPointToContentPoint(pointerPosition)
                            zoomable.scale(
                                targetScale = newScale,
                                animated = true,
                                centroidContentPoint = contentPosition
                            )
                        }
                    }
                }
            }
        }
    })

    fun update(zoomable: ZoomableState) {
        this.zoomable = zoomable
        positionDelegate.resetPointerInputHandler()
        scrollDelegate.resetPointerInputHandler()
    }
}