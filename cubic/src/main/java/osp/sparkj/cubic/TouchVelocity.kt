package osp.sparkj.cubic

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangeIgnoreConsumed
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlinx.coroutines.launch


fun Modifier.touchWithVelocity(animatable: Animatable<Offset, AnimationVector2D>) = composed {
    val scope = rememberCoroutineScope()
    pointerInput(Unit) {
        val velocityTracker = VelocityTracker()
        //处理移动
        awaitEachGesture {
            //忽略事件被消费 这样就不会因为child可点击的时候从child开始滑动的时候无法滚动
            val down = awaitFirstDown(requireUnconsumed = false)
            velocityTracker.resetTracking()
            scope.launch {
                animatable.stop()
            }

            //<editor-fold desc="awaitTouchSlopOrCancellation用法">
            // awaitTouchSlopOrCancellation(down.id) { change, overSlop ->
            //         //change不消费 这里就会在移动的时候一直回调
            // }
            // do {
            //     val inputChange = awaitTouchSlopOrCancellation(down.id) { change, overSlop ->
            //         //change这里消费了这里就结束 需要循环这里才可以拿到移动坐标
            //     }
            // } while (inputChange != null && inputChange.isConsumed)
            //</editor-fold>

            drag(down.id) {
                scope.launch {
                    animatable.snapTo(
                        animatable.value + it
                            .positionChangeIgnoreConsumed()
                            .swap()
                    )
                }
                velocityTracker.addPosition(it.uptimeMillis, animatable.value)
                it.consume()
            }
            val velocity = velocityTracker.calculateVelocity()
//                    DecayAnimation
//                动画要在新协程处理
//                offset.animateDecay(targetValue, splineBasedDecay)
            scope.launch {
//                    offset.updateBounds()
//                exponentialDecay<Offset>().calculateTargetValue(Offset.VectorConverter,offset.value,velocity)
//                    offset.animateDecay(Offset(velocity.x, velocity.y), splineBasedDecay<Offset>(this@pointerInput))
                animatable.animateDecay(Offset(velocity.x, velocity.y), exponentialDecay())
            }
        }
    }
}
