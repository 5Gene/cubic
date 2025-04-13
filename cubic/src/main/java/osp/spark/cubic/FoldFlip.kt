package osp.spark.cubic

import android.annotation.SuppressLint
import android.graphics.Camera
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import osp.spark.cartoon.curves.interval
import osp.spark.cartoon.wings.alpha
import osp.spark.cartoon.wings.dpf
import osp.spark.cartoon.wings.transForm
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * @author yun.
 * @date 2022/12/6
 * @des [一句话描述]
 * @since [https://github.com/5hmlA]
 * <p><a href="https://github.com/5hmlA">github</a>
 */

val transformTop = TransformOrigin(0.5f, 0f)

@Composable
internal fun Modifier.flipExpand(
    cardWidthFactor: Float = 0.95F,
    progressProvider: () -> Float = { .2F },
    offsetProvider: (() -> Float)? = null
): Modifier {
    val camera = remember { Camera() }
    val blackColor = remember { Color.Black.toArgb() }
    return drawWithContent {
        with(drawContext.canvas.nativeCanvas) {
            val progress = progressProvider()
            val height = size.height
            val topOffsetValue = (offsetProvider?.invoke() ?: (height / 4F)) * progress
            val widthScaleValue = 1 - (1 - cardWidthFactor) * progress
            transForm(
                translateY = -topOffsetValue,
                scaleX = widthScaleValue,
                clip = { offsetX, offsetY ->
                    val top = progress.interval(.5F, 1F) / 2 * height
                    clipRect(offsetX, top, -offsetX, -offsetY)
                }) {
                drawContent()
            }
            if (progress < 0.5) {
                transForm(
                    translateY = -topOffsetValue,
                    scaleX = widthScaleValue,
                    rotateX = -180F * progress,
                    locationZ = (-30).dpf(),
                    camera = camera,
                    clip = { offsetX, offsetY ->
                        clipRect(offsetX, offsetY, -offsetX, 0F)
                    }) {
                    drawContent()
                    if (progress > 0F) {
                        drawColor(blackColor.alpha(.6 * progress))
                    }
                }
            }
        }
    }
}

@SuppressLint("UnnecessaryComposedModifier")
internal fun Modifier.flipHead(
    progressProvider: () -> Float = { .2F },
    offsetProvider: () -> Float
): Modifier {
    return drawWithContent {
        val progress = progressProvider()

        // progress > 1-0.5
        val v = 1 - progress
        // v > 0-0.5
        val topOffsetValue = offsetProvider() * v

        with(drawContext.canvas) {
            save()
            //1-0.5    0-0.5
            //0-size.height
            val moving = size.height * v * 2
            translate(0F, topOffsetValue + moving)
            clipRect(0F, 0F, size.width, size.height - moving)
            drawContent()
            restore()
        }
    }.graphicsLayer {
        alpha = progressProvider()
    }
}

@SuppressLint("UnnecessaryComposedModifier")
internal fun Modifier.flipCard(
    progressProvider: () -> Float,
    offsetProvider: () -> Float
): Modifier {
    return graphicsLayer {
        // progress > 1-0.5
        val v = 1 - progressProvider()
        // v > 0-0.5
        val topOffsetValue = offsetProvider() * v
        rotationX = 180F * v
        translationY = topOffsetValue
        cameraDistance = 80F
        transformOrigin = transformTop
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun FoldFlip(
    headHeightFactor: Float = .2F,
    cardWidthFactor: Float = .85F,
    expand: @Composable () -> Unit,
    head: @Composable () -> Unit,
    card: @Composable () -> Unit
) {
    with(LocalDensity.current) {
        BoxWithConstraints {
            val cardHeight = maxHeight
            val cardWidth = maxWidth
            val cardHeightPx = cardHeight.toPx()
            val coroutineScope = rememberCoroutineScope()
            val animate = remember { Animatable(1F) }
            var scrollOffset = remember { (if (animate.value > .5) cardHeightPx else 0F) }
            SideEffect {
                println("FoldFlip_BoxWithConstraints: scrollOffset:$scrollOffset")
            }
            val modifier = Modifier.pointerInput(Unit) {
                val velocityTracker = VelocityTracker()
                detectDragGestures(
                    onDragCancel = {
                        coroutineScope.launch {
                            scrollOffset = if (animate.value > .5) cardHeightPx else 0F
                            animate.animateTo(if (animate.value > .5) 1F else 0F)
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            scrollOffset = if (animate.value > .5) cardHeightPx else 0F
                            animate.animateTo(
                                if (animate.value > .5) 1F else 0F,
                                animationSpec = tween(500, easing = LinearOutSlowInEasing)
                            )
                            //animatable.animateDecay(Offset(velocity.x, velocity.y), exponentialDecay())
                            velocityTracker.resetTracking()
                        }
                    },
                ) { change, dragAmount ->
                    if (!animate.isRunning) {
                        velocityTracker.addPosition(
                            change.uptimeMillis,
                            change.position
                        )
                        //展开->折叠的时候向下滑动，折叠起来，此时 velocity 为正值
                        //折叠->展开的时候向上滑动，展开，此时 velocity 为负值
//                        val velocity = velocityTracker.calculateVelocity().y
//                        println("FoldFlip_FoldFlip ======= velocity $velocity")
                        change.consume()
                        coroutineScope.launch {
                            scrollOffset += dragAmount.y
                            animate.snapTo((scrollOffset / cardHeightPx).coerceIn(0F, 1F))
                        }
                    }
                }
            }

            Box(modifier = modifier.fillMaxSize()) {
                val headHeight = remember { cardHeight * headHeightFactor }
                val offset = remember { { (cardHeight / 2 - headHeight).toPx() } }
                val horizontalPadding = remember { (cardWidth * (1 - cardWidthFactor) / 2) }
                val showExpand by remember { derivedStateOf { animate.value < .9 } }
                val showHead by remember { derivedStateOf { animate.value > .5 } }
                println("FoldFlip_BoxWithConstraints_Box: showExpand=$showExpand, showHead=$showHead, hPadding:$horizontalPadding")
                if (showExpand) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .flipExpand(
                                progressProvider = { animate.value },
                                offsetProvider = offset,
                                cardWidthFactor = cardWidthFactor
                            )
                    ) {
                        SideEffect {
                            println("FoldFlip_FoldFlip_Box_expand: $showExpand")
                        }
                        expand()
                    }
                }
                if (showHead) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = horizontalPadding)
                            .graphicsLayer {
                                scaleX = 1 + (1 / cardWidthFactor - 1) * (1 - animate.value)
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .height(headHeight)
                                .flipHead(progressProvider = { animate.value }, offsetProvider = offset)
                        ) {
                            SideEffect {
                                println("FoldFlip_FoldFlip_Box_head: $showHead")
                            }
                            head()
                        }

                        Box(
                            modifier = Modifier
                                .flipCard(progressProvider = { animate.value }, offsetProvider = offset)
                        ) {
                            SideEffect {
                                println("FoldFlip_FoldFlip_Box_card: $showHead")
                            }
                            card()
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("ReturnFromAwaitPointerEventScope")
fun Modifier.swipeToDismiss(
    onDismissed: () -> Unit
): Modifier = composed {
    val offsetX = remember { Animatable(0f) }
    pointerInput(Unit) {
        // Used to calculate fling decay.
        val decay = splineBasedDecay<Float>(this)
        // Use suspend functions for touch events and the Animatable.
        coroutineScope {
            while (true) {
                // Detect a touch down event.
                val pointerId = awaitPointerEventScope { awaitFirstDown().id }
                val velocityTracker = VelocityTracker()
                // Stop any ongoing animation.
                offsetX.stop()
                awaitPointerEventScope {
                    horizontalDrag(pointerId) { change ->
                        // Update the animation value with touch events.
                        launch {
                            offsetX.snapTo(
                                offsetX.value + change.positionChange().x
                            )
                        }
                        velocityTracker.addPosition(
                            change.uptimeMillis,
                            change.position
                        )
                    }
                }
                // No longer receiving touch events. Prepare the animation.
                val velocity = velocityTracker.calculateVelocity().x
                val targetOffsetX = decay.calculateTargetValue(
                    offsetX.value,
                    velocity
                )
                // The animation stops when it reaches the bounds.
                offsetX.updateBounds(
                    lowerBound = -size.width.toFloat(),
                    upperBound = size.width.toFloat()
                )
                launch {
                    if (targetOffsetX.absoluteValue <= size.width) {
                        // Not enough velocity; Slide back.
                        offsetX.animateTo(
                            targetValue = 0f,
                            initialVelocity = velocity
                        )
                    } else {
                        // The element was swiped away.
                        offsetX.animateDecay(velocity, decay)
                        onDismissed()
                    }
                }
            }
        }
    }.offset { IntOffset(offsetX.value.roundToInt(), 0) }
}