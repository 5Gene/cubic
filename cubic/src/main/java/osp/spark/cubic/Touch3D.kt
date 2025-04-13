package osp.spark.cubic

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * @author yun.
 * @date 2023/3/18
 * @des [一句话描述]
 * @since [https://github.com/5hmlA]
 * <p><a href="https://github.com/5hmlA">github</a>
 */

@Preview
@Composable
fun PreviewTouch3D() {
    Box {
        Box(
            modifier = Modifier
                .size(300.dp, 300.dp)
                .background(color = Color.Blue)
        ) {
            Touch3D {
                Box(
                    modifier = Modifier
                        .size(300.dp, 300.dp)
                        .background(Color.Red)
                )
            }
        }
    }
}

fun Modifier.touch3D(deep: Float = 20F) = composed {
    val animateMove = remember { Animatable(Offset.Zero, typeConverter = Offset.VectorConverter) }
    val animate = remember { Animatable(0F) }
    val coroutineScope = rememberCoroutineScope()
    pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            //按下后
            if (!animate.isRunning) {
                coroutineScope.launch {
                    animateMove.snapTo(Offset(down.position.x, down.position.y))
                    animate.animateTo(1F)
                }
            }
            //拖动
            drag(down.id) {
                it.consume()
                coroutineScope.launch {
                    animateMove.snapTo(
                        Offset(
                            it.position.x.coerceIn(0F, size.width.toFloat()),
                            it.position.y.coerceIn(0F, size.height.toFloat())
                        )
                    )
                }
            }
            //抬起
            coroutineScope.launch { animate.animateTo(0F) }
        }
    }.graphicsLayer {
        val width = size.width.toFloat()
        val height = size.height.toFloat()
        rotationY = (animateMove.value.x - width / 2) / (width / 2) * deep * animate.value
        rotationX = (height / 2 - animateMove.value.y) / (height / 2) * deep * animate.value
    }
}

@Composable
private fun touchModifier(): Modifier {
    val animateMove = remember { Animatable(Offset.Zero, typeConverter = Offset.VectorConverter) }
    val animate = remember { Animatable(0F) }
    val coroutineScope = rememberCoroutineScope()
    return Modifier.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            //按下后
            if (!animate.isRunning) {
                coroutineScope.launch {
                    animateMove.snapTo(Offset(down.position.x, down.position.y))
                    animate.animateTo(1F)
                }
            }
            //拖动
            drag(down.id) {
                it.consume()
                coroutineScope.launch {
                    animateMove.snapTo(
                        Offset(
                            it.position.x.coerceIn(0F, size.width.toFloat()),
                            it.position.y.coerceIn(0F, size.height.toFloat())
                        )
                    )
                }
            }
            //抬起
            coroutineScope.launch { animate.animateTo(0F) }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Touch3D(deep: Float = 20F, content: @Composable () -> Unit) {
    val animateMove = remember { Animatable(Offset.Zero, typeConverter = Offset.VectorConverter) }
    val animate = remember { Animatable(0F) }
    val scope = rememberCoroutineScope()
    val modifier = Modifier.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            //按下后
            if (!animate.isRunning) {
                scope.launch {
                    animateMove.snapTo(Offset(down.position.x, down.position.y))
                    animate.animateTo(1F)
                }
            }
            //拖动
            drag(down.id) {
                it.consume()
                scope.launch {
                    animateMove.snapTo(
                        Offset(
                            it.position.x.coerceIn(0F, size.width.toFloat()),
                            it.position.y.coerceIn(0F, size.height.toFloat())
                        )
                    )
                }
            }
            //抬起
            scope.launch { animate.animateTo(0F) }
        }
    }

    SideEffect {
        println("Touch3D_")
    }
    Box(modifier = modifier.graphicsLayer {
        val width = size.width.toFloat()
        val height = size.height.toFloat()
        rotationY = (animateMove.value.x - width / 2) / (width / 2) * deep * animate.value
        rotationX = (height / 2 - animateMove.value.y) / (height / 2) * deep * animate.value
    }) {
        SideEffect {
            println("Touch3D_BoxWithConstraints_Box_content")
        }
        content()
    }
}