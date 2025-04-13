package osp.sparkj.cubic

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import osp.sparkj.cartoon.curves.interval
import osp.sparkj.cartoon.wings.alpha
import osp.sparkj.cartoon.wings.dpf
import osp.sparkj.cartoon.wings.transForm
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
internal fun FoldFlip(
    modifier: Modifier = Modifier,
    headHeightFactor: Float = .23F,
    cardWidthFactor: Float = .877F,
    progress: () -> Float,
    expand: @Composable () -> Unit,
    head: @Composable () -> Unit,
    card: @Composable () -> Unit
) {
    with(LocalDensity.current) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val headHeight = remember { maxHeight * headHeightFactor }
            val offset = { (maxHeight / 2 - headHeight).toPx() }
            val horizontalPadding = remember { (maxWidth * (1 - cardWidthFactor) / 2) }
            val showExpand by remember { derivedStateOf { progress() < .9 } }
            if (showExpand) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .flipExpand(
                            progressProvider = progress,
                            offsetProvider = offset,
                            cardWidthFactor = cardWidthFactor
                        )
                ) {
                    expand()
                }
            }
            val showHead by remember { derivedStateOf { progress() > .5 } }
            if (showHead) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = horizontalPadding)
                        .graphicsLayer {
                            scaleX = 1 + (1 / cardWidthFactor - 1) * (1 - progress())
                        }
                ) {

                    Box(
                        modifier = Modifier
                            .height(headHeight)
                            .flipHead(progressProvider = progress, offsetProvider = offset)
                    ) {
                        head()
                    }

                    Box(
                        modifier = Modifier
                            .flipCard(progressProvider = progress, offsetProvider = offset)
                    ) {
                        card()
                    }
                }
            }
        }
    }
}

@Composable
fun FoldFlip(
    headHeightFactor: Float = .20F,
    cardWidthFactor: Float = .85F,
    expanded: @Composable () -> Unit,
    head: @Composable () -> Unit,
    card: @Composable () -> Unit
) {

    val animate = remember { Animatable(1F) }
    val scope = rememberCoroutineScope()
    val screenHeight = LocalConfiguration.current.screenHeightDp.toFloat()
    var scrollOffset = remember { (if (animate.value > .5) screenHeight else 0F) }

    val modifier = Modifier.pointerInput(Unit) {
        val velocityTracker = VelocityTracker()
        detectDragGestures(
            onDragCancel = {
                scope.launch {
                    scrollOffset = if (animate.value > .5) screenHeight else 0F
                    animate.animateTo(if (animate.value > .5) 1F else 0F)
                }
            },
            onDragEnd = {
                scope.launch {
                    scrollOffset = if (animate.value > .5) screenHeight else 0F
                    animate.animateTo(
                        if (animate.value > .5) 1F else 0F,
                        animationSpec = tween(500, easing = LinearOutSlowInEasing)
//                        animationSpec = spring(
//                            dampingRatio = Spring.DampingRatioLowBouncy,
//                        )
                    )
                    velocityTracker.resetTracking()
//                    progress.animateTo(
//                        (if (progress.value > .5) 1F else 0F),
//                        animationSpec = spring(
//                            stiffness = Spring.StiffnessHigh,
//                            visibilityThreshold = Spring.StiffnessMedium
//                        )
//                    )
                }
            },
        ) { change, dragAmount ->
            if (!animate.isRunning) {
                velocityTracker.addPosition(
                    change.uptimeMillis,
                    change.position
                )
                val velocity = velocityTracker.calculateVelocity().y
                println("============= velocity $velocity")
                change.consume()
                scope.launch {
                    scrollOffset += dragAmount.y
                    animate.snapTo((scrollOffset / screenHeight).coerceIn(0F, 1F))
                }
            }
        }
    }

    FoldFlip(modifier, headHeightFactor, cardWidthFactor, { animate.value }, expanded, head, card)
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