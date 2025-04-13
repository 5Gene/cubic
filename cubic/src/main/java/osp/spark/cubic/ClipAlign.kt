package osp.spark.cubic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun test() {

    Box(Modifier.background(Color.Red)) {
        Box(
            modifier = Modifier
                .size(100.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Red)
            )
        }
    }

    Box() {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clipAlign(
                    Alignment.Center,
                    widthRatio = 1f,
                    heightRatio = .5f
                )
                .background(Color.Cyan)
        ) {

        }
    }
}

//https://developer.android.com/develop/ui/compose/custom-modifiers?hl=zh-cn
//用于存储修饰符的逻辑和状态的 Modifier.Node 实现。
//用于创建和更新修饰符节点实例的 ModifierNodeElement。
//如上所述的可选修饰符工厂。
class ClipAlignNode(
    var align: Alignment,
    var widthRatio: Float,
    var heightRatio: Float
) : LayoutModifierNode, Modifier.Node() {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(constraints.copy())
        val realWidth = (placeable.width * widthRatio).toInt()
        val realHeight = (placeable.height * heightRatio).toInt()
        val size = IntSize(realWidth, realHeight)
        val space = IntSize(placeable.width, placeable.height)
        val offset = align.align(size, space, layoutDirection)
        return layout(realWidth, realHeight) {
            placeable.place(offset.x, offset.y)
        }
    }
}

data class ClipAlignElement(
    val align: Alignment,
    val widthRatio: Float,
    val heightRatio: Float
) : ModifierNodeElement<ClipAlignNode>() {
    override fun create() = ClipAlignNode(align, widthRatio, heightRatio)

    override fun update(node: ClipAlignNode) {
        node.align = align
        node.widthRatio = widthRatio
        node.heightRatio = heightRatio
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "clipAlign"
        properties["align"] = align
        properties["widthRatio"] = widthRatio
        properties["heightRatio"] = heightRatio
    }
}

class ClipRatioAlignNode(
    var align: Alignment,
    var widthRatio: () -> Float = { 1F },
    var heightRatio: () -> Float = { 1F }
) : LayoutModifierNode, Modifier.Node() {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        //MeasureScope是Density的子类
        val placeable = measurable.measure(constraints.copy())
        val realWidth = (placeable.width * widthRatio()).toInt()
        val realHeight = (placeable.height * heightRatio()).toInt()
        val size = IntSize(realWidth, realHeight)
        val space = IntSize(placeable.width, placeable.height)
        val offset = align.align(size, space, layoutDirection)
        return layout(realWidth, realHeight) {
            placeable.place(offset.x, offset.y)
        }
    }
}

data class ClipRatioAlignElement(
    val align: Alignment,
    var widthRatio: () -> Float = { 1F },
    var heightRatio: () -> Float = { 1F }
) : ModifierNodeElement<ClipRatioAlignNode>() {
    override fun create() = ClipRatioAlignNode(align, widthRatio, heightRatio)

    override fun update(node: ClipRatioAlignNode) {
        node.align = align
        node.widthRatio = widthRatio
        node.heightRatio = heightRatio
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "clipAlign"
        properties["align"] = align
        properties["widthRatio"] = widthRatio
        properties["heightRatio"] = heightRatio
    }
}

fun Modifier.clipAlign(
    align: Alignment = Alignment.Center,
    widthRatio: Float = 1f,
    heightRatio: Float = 1f
) = this then ClipAlignElement(align, widthRatio, heightRatio)

fun Modifier.clipAlign(
    align: Alignment = Alignment.Center,
    widthRatio: () -> Float = { 1F },
    heightRatio: () -> Float = { 1F }
) = this then ClipRatioAlignElement(align, widthRatio, heightRatio)

//还有另一个用于创建自定义修饰符的 API，即 composed {}。此 API 会导致性能问题，因此不再推荐使用
//@SuppressLint("UnnecessaryComposedModifier")
//fun Modifier.clipAlign(
//    align: Alignment,
//    widthRatio: Float,
//    heightRatio: Float
//) = composed {
//    layout { measurable, constraints ->
//        val placeable = measurable.measure(constraints.copy())
//        val realWidth = (placeable.width * widthRatio).toInt()
//        val realHeight = (placeable.height * heightRatio).toInt()
//        val size = IntSize(realWidth, realHeight)
//        val space = IntSize(placeable.width, placeable.height)
//        val offset = align.align(size, space, layoutDirection)
//        layout(size.width, size.height) {
//            placeable.placeRelative(-offset.x, -offset.y)
//        }
//    }
//}
