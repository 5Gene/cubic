package jzy.jonas.complayout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.AnimationVector3D
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jzy.jonas.complayout.ui.theme.CompLayoutTheme
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompLayoutTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {

    var angle by remember {
        mutableStateOf(Offset(0F, 0F))
    }
    Layout(
        content = {
            repeat(30) {
                Text("$it")
            }
        }
    ) { measurables, constraints ->
        val radius = 500F
        val poistions = mutableListOf<AnimationVector3D>()
        // measure and position children given constraints logic here
        val placeables = measurables.mapIndexed { index, measurable ->

            val size = measurables.size
            val phi = (acos(-1.0 + (2.0 * index - 1.0) / size))
            val theta = sqrt(size * Math.PI) * phi
            val x = radius * cos(phi) * sin(theta)
            val y = radius * sin(phi) * sin(theta)
            val z = radius * cos(theta)

            val xx = x
            val xy = y * cos(angle.x) - z * sin(angle.x)
            val xz = y * sin(angle.x) + z * cos(angle.x)

            val zx = xx * cos(angle.y) - xy * sin(angle.y)
            val zy = xy * cos(angle.y) + xx * sin(angle.y)
            val zz = xz

            poistions.add(AnimationVector3D(zx.toFloat(), zy.toFloat(), zz.toFloat()))
            measurable.measure(constraints)
        }
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                val poi: AnimationVector3D = poistions[index]
                placeable.place(constraints.maxWidth/2 + poi.v1.toInt(), constraints.maxHeight/2 + poi.v2.toInt())
            }
        }
    }

    Box(modifier = Modifier
        .size(300.dp)
        .pointerInput("") {
            detectDragGestures { change, dragAmount ->
                println(change.position)
                println("---------------- $dragAmount")
                angle += Offset(-dragAmount.y * 0.01F, dragAmount.x * 0.01F)
            }
        }
        .drawWithCache {
//            val radius = 100F
            val radius = size.width / 2F
            val portion = 15

            onDrawBehind {
                drawContext.canvas.nativeCanvas.translate(size.width / 2, size.height / 2)
                for (i in 0 until portion) {
                    val theta = Math.PI / portion * i
                    for (j in 0 until portion) {
                        val phi = 2 * Math.PI / portion * j

                        val x = radius * cos(phi) * sin(theta)
                        val y = radius * sin(phi) * sin(theta)
                        val z = radius * cos(theta)

                        val xx = x
                        val xy = y * cos(angle.x) - z * sin(angle.x)
                        val xz = y * sin(angle.x) + z * cos(angle.x)

                        val zx = xx * cos(angle.y) - xy * sin(angle.y)
                        val zy = xy * cos(angle.y) + xx * sin(angle.y)
                        val zz = xz

//                        drawCircle(Color.Green, 6F, center = Offset(x.toFloat(), z.toFloat()))
//                        drawCircle(Color.Green, 6F, center = Offset(yx.toFloat(), yz.toFloat()))
//                        drawCircle(Color.Green, 10F, center = Offset(xx.toFloat(), xz.toFloat()))
                        if (i==6) {
                            drawCircle(Color.Red, 8F, center = Offset(zx.toFloat(), zz.toFloat()))
                        } else {
                            drawCircle(Color.Green, 8F, center = Offset(zx.toFloat(), zz.toFloat()))
                        }
                    }
                }
            }
        })
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CompLayoutTheme {
        Greeting("Android")
    }
}