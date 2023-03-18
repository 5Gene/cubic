package osp.sparkj.cubic

import android.graphics.PointF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangeIgnoreConsumed
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.*

@Preview
@Composable
fun TestBollLayout() {
    BollLayout {
        for (i in 0 until 20)
            Box(
                modifier = Modifier
                    .background(color = Color.Red)
                    .clickable {
                        println("===== $i")
//                        Toast.makeText(LocalContext.current,"$i",Toast.LENGTH_SHORT).show()
                    }
                    .padding(2.dp)
            ) {
                Text(text = "N0.$i")
            }
    }
}

fun Modifier.touchWithVelocity(animatable: Animatable<Offset, AnimationVector2D>) = composed {
    val scope = rememberCoroutineScope()
    pointerInput(Unit) {
        val velocityTracker = VelocityTracker()
        forEachGesture {
            //处理移动
            awaitPointerEventScope {
                //忽略事件被消费 这样就不会因为child可点击的时候从child开始滑动的时候无法滚动
                val down = awaitFirstDown(requireUnconsumed = false)
                velocityTracker.resetTracking()
                scope.launch {
                    animatable.stop()
                }

                //<editor-fold desc="awaitTouchSlopOrCancellation用法">
//                    awaitTouchSlopOrCancellation(down.id) { change, overSlop ->
//                            //change不消费 这里就会在移动的时候一直回调
//                    }
//                    do {
//                        val inputChange = awaitTouchSlopOrCancellation(down.id) { change, overSlop ->
//                            //change这里消费了这里就结束 需要循环这里才可以拿到移动坐标
//                        }
//                    } while (inputChange != null && inputChange.isConsumed)
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

@Composable
fun BollLayout(content: @Composable @UiComposable () -> Unit) {
    val offset = remember {
        Animatable(Offset.Zero, typeConverter = Offset.VectorConverter)
    }
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
            .touchWithVelocity(offset)
    ) {
        val bollLayoutMeasurePolicy = remember(offset.value) {
            bollLayoutMeasurePolicy(offset.value)
        }
        Layout(content = content, measurePolicy = bollLayoutMeasurePolicy)
    }
}

fun Offset.swap() = Offset(y, 0f)
//fun Offset.swap() = Offset(y, -x)

fun Float.toAngle(radius: Int): Float {
    val rowSide = this / 2.0
    val multiplier = (rowSide / radius) % 2
    val side = rowSide % radius
    return (Math.PI * multiplier.toInt() + 2 * asin(side / radius)).toFloat()
}

data class Coordinate3D(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
)

class SphereCoordinate {
    //<editor-fold desc="X轴旋转">
    var sinX = 0f
    var cosX = 0f
    //</editor-fold>

    //<editor-fold desc="Y轴旋转">
    var sinY = 0f
    var cosY = 0f
    //</editor-fold>

    //<editor-fold desc="Z轴旋转">
    var sinZ = 0f
    var cosZ = 0f
    //</editor-fold>

    fun rotationTransform(x: Float, y: Float, z: Float = 0f) {
        println("========= $x  $y ${Math.PI * 2}===================")
        sinX = sin(x)
        cosX = cos(x)
        sinY = sin(y)
        cosY = cos(y)
//        sinZ = sin(z)
//        cosZ = cos(z)
        sinZ = 0f
        cosZ = 1f
    }

    fun calculeCoordinate(phi: Double, theta: Double): Coordinate3D {
//        球体坐标
//        x=r sinθ cosφ.
//        y=r sinθ sinφ.
//        z=r cosθ.
        val x: Float = 1 * (sin(phi) * cos(theta)).toFloat()
        val y: Float = 1 * (sin(phi) * sin(theta)).toFloat()
        val z: Float = 1 * (cos(phi)).toFloat()

//        https://blog.csdn.net/csxiaoshui/article/details/65446125
//        围绕 x 旋转 -------------------
//        y′=ycosθ−zsinθ
//        z′=ysinθ+zcosθ
        val xx = x
        val xy: Float = y * cosX + z * -sinX
        val xz: Float = y * sinX + z * cosX
//        围绕 y 旋转 -------------------
//        x′=zsinθ+xcosθ
//        z′=zcosθ−xsinθ
        val yxx: Float = xz * sinY + xx * cosY
        val yxy = xy
        val yxz: Float = xx * -sinY + xz * cosY
//        围绕 z 旋转 ------------------
//        x′=xcosθ−ysinθ
//        y′=xsinθ+ycosθ
        val zyxx: Float = yxx * cosZ + yxy * -sinZ
        val zyxy: Float = yxx * sinZ + yxy * cosZ
        val zyxz = yxz
        return Coordinate3D(
            x = zyxx,
            y = zyxy,
            z = zyxz
        )
    }
}

fun bollLayoutMeasurePolicy(touch: Offset) = MeasurePolicy { measurables, constraints ->
    val center = PointF(constraints.maxWidth / 2f, constraints.maxHeight / 2f)
    val slide = constraints.maxWidth.coerceAtMost(constraints.maxHeight)
    val bolls = mutableListOf<Boll>()
    val max = measurables.size
    var maxSide = 0
    val sphereCoordinate = SphereCoordinate()
    sphereCoordinate.rotationTransform(touch.x.toAngle(slide / 2), touch.y.toAngle(slide / 2))
    measurables.forEachIndexed { index, measurable ->
        val placeable = measurable.measure(constraints.copy())
        maxSide = maxSide.coerceAtLeast(placeable.width.coerceAtLeast(placeable.height))
        //    0 ≤ θ ≤ π    phi
        //    0 ≤ φ < 2π   theta
        val phi = acos(-1.0 + (2.0 * (index + 1) - 1.0) / max)
        val theta = sqrt(max * Math.PI) * phi
//            phi = Math.random() * Math.PI
//            theta = Math.random() * Math.PI * 2
        val coordinates = sphereCoordinate.calculeCoordinate(phi, theta)
        val scale = 2 / 1.0f / (2 + coordinates.z)
        bolls.add(
            Boll(
                coordinates = coordinates,
                scale = scale,
                placeable = placeable
            )
        )
    }
    val radius = slide / 2 - maxSide / 2
    layout(slide, slide) {
        bolls.asSequence().sortedBy { it.scale }.forEach {
            val size = IntSize(it.placeable.width, it.placeable.height)
            //把点放在 盒子中间
            val align = Alignment.Center.align(IntSize(1, 1), size, layoutDirection)
            //实际要的是把盒子中间放点上 所以移动要取反
            it.placeable.placeWithLayer(
                (center.x + it.coordinates.x * radius - align.x).toInt(),
                (center.y + it.coordinates.y * radius - align.y).toInt()
            ) {
                scaleX = it.scale
                scaleY = it.scale
            }
        }
    }
}

data class Boll(
    val coordinates: Coordinate3D,
    val scale: Float = 0f,
    val placeable: Placeable
)