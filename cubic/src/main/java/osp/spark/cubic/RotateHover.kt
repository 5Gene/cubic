package osp.spark.cubic

import android.graphics.Camera
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import osp.spark.cartoon.curves.interval
import osp.spark.cartoon.wings.transForm

/**
 * @author yun.
 * @date 2023/3/19
 * @des [一句话描述]
 * @since [https://github.com/5hmlA]
 * <p><a href="https://github.com/5hmlA">github</a>
 */


@Composable
fun RotateHover(hover: Float = 26F, time: Int = 3111, content: @Composable () -> Unit) {
    val animation = remember {
        Animatable(0F)
    }
    val scope = rememberCoroutineScope()
    val tapModifier = Modifier.pointerInput(Unit) {
        detectTapGestures {
            scope.launch {
                animation.animateTo(
                    (if (animation.value < .5F) 1F else 0F),
                    animationSpec = tween(durationMillis = time)
                )
            }
        }
    }

    Box(
        modifier = tapModifier
            .rotateHover(animation.value, hover)
    ) {
        content()
    }
}

fun Modifier.rotateHover(progress: Float, hover: Float = 26F) =
    composed {
        val camera = remember { Camera() }
        drawWithContent {
            if (progress > 0) {
                val width = size.width
                val height = size.height
                with(drawContext.canvas.nativeCanvas) {
                    val hovertime = 0.2F
                    val hoverEnd = 1 - hovertime
                    val hovering = if (progress <= hovertime) {
                        hover * progress.interval(0F, .2F)
                    } else if (progress >= hoverEnd) {
                        hover - hover * progress.interval(hoverEnd, 1F)
                    } else {
                        hover
                    }
                    val rotateing = 360F * progress.interval(hovertime, hoverEnd)

                    transForm(
                        rotate = rotateing,
                        clip = { _, _ ->
                            clipRect(-width, -height, width, 0F)
                        }
                    ) {
                        drawContent()
                    }

                    transForm(
                        camera = camera,
                        rotate = rotateing,
                        rotateX = hovering,
                        clip = { _, _ ->
                            clipRect(-width, 0F, width, height)
                        }
                    ) {
                        drawContent()
                    }
                }
            } else {
                drawContent()
            }
        }
    }
