package osp.sparkj.cubic

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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

    val animate = remember { Animatable(0F) }
    val scope = rememberCoroutineScope()

    var touchPointX by remember { mutableFloatStateOf(0F) }
    var touchPointY by remember { mutableFloatStateOf(0F) }

    pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            //按下后
            if (!animate.isRunning) {
                touchPointX = (down.position.x - size.width / 2F) / (size.width / 2F)
                touchPointY = (size.height / 2F - down.position.y) / (size.height / 2F)
                scope.launch { animate.animateTo(1F) }
            }
            //拖动
            drag(down.id) {
                it.consume()
                val x = it.position.x.coerceIn(0F, size.width.toFloat())
                touchPointX = (x - size.width / 2F) / (size.width / 2F)
                val y = it.position.y.coerceIn(0F, size.height.toFloat())
                touchPointY = (size.height / 2F - y) / (size.height / 2F)
            }
            //抬起
            scope.launch { animate.animateTo(0F) }
        }
    }
        .graphicsLayer {
            rotationX = touchPointY * deep * animate.value
            rotationY = touchPointX * deep * animate.value
        }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Touch3D(deep: Float = 20F, content: @Composable () -> Unit) {
    with(LocalDensity.current) {
        BoxWithConstraints {
            val width = remember { maxWidth.toPx() }
            val height = remember { maxHeight.toPx() }
            var touchPointX by remember { mutableFloatStateOf(0F) }
            var touchPointY by remember { mutableFloatStateOf(0F) }

            val animate = remember { Animatable(0F) }
            val scope = rememberCoroutineScope()
            val modifier = Modifier.pointerInput(Unit) {
                forEachGesture {
                    awaitPointerEventScope {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        //按下后
                        if (!animate.isRunning) {
                            touchPointX = down.position.x
                            touchPointY = down.position.y
                            scope.launch { animate.animateTo(1F) }
                        }
                        //拖动
                        drag(down.id) {
                            it.consume()
                            touchPointX = it.position.x.coerceIn(0F, width)
                            touchPointY = it.position.y.coerceIn(0F, height)
                        }
                        //抬起
                        scope.launch { animate.animateTo(0F) }
                    }
                }
            }

            val rotateX = (touchPointX - width / 2) / (width / 2) * deep * animate.value
            val rotateY = (height / 2 - touchPointY) / (height / 2) * deep * animate.value

            Box(modifier = modifier.graphicsLayer {
                rotationX = rotateY
                rotationY = rotateX
            }) {
                content()
            }
        }
    }
}
