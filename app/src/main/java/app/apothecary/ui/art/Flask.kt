package app.apothecary.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import app.apothecary.domain.Glow
import app.apothecary.ui.theme.C
import kotlin.math.sin

/** Global switches for the art. */
object Art {
    /** Screenshots and renders want a still frame: no loop, a fixed moment. */
    @Volatile var still = false
}

/** A frame counter, about 20 a second, while on screen (or a fixed frame for renders). */
@Composable
fun rememberTick(): Int {
    var tick by remember { mutableIntStateOf(if (Art.still) 40 else 0) }
    if (!Art.still) {
        LaunchedEffect(Unit) {
            var last = 0L
            while (true) withFrameMillis { ms -> if (ms - last >= 50) { last = ms; tick++ } }
        }
    }
    return tick
}

/** The glass outline of a bottle in a w×h box at (x, y), and where its belly starts (the liquid fills below that). */
private fun bottle(shape: Int, x: Float, y: Float, w: Float, h: Float): Pair<Path, Float> {
    return when (shape) {
        1 -> {
            val p = Path().apply {
                addRoundRect(RoundRect(Rect(x + w * .3f, y + h * .12f, x + w * .7f, y + h), topLeft = CornerRadius(w * .04f), topRight = CornerRadius(w * .04f),
                    bottomLeft = CornerRadius(w * .2f), bottomRight = CornerRadius(w * .2f)))
            }
            p to y + h * .22f
        }
        2 -> {
            val p = Path().apply { addRoundRect(RoundRect(Rect(x + w * .1f, y + h * .3f, x + w * .9f, y + h), CornerRadius(w * .14f))) }
            p to y + h * .36f
        }
        3 -> {
            val p = Path().apply {
                moveTo(x + w * .38f, y + h * .12f); lineTo(x + w * .62f, y + h * .12f); lineTo(x + w * .62f, y + h * .42f)
                lineTo(x + w * .94f, y + h * .9f); quadraticTo(x + w * .98f, y + h, x + w * .84f, y + h)
                lineTo(x + w * .16f, y + h); quadraticTo(x + w * .02f, y + h, x + w * .06f, y + h * .9f)
                lineTo(x + w * .38f, y + h * .42f); close()
            }
            p to y + h * .46f
        }
        else -> {
            val r = minOf(w * .48f, h * .32f)
            val cy = y + h - r
            val neck = Path().apply { addRect(Rect(x + w * .33f, y + h * .12f, x + w * .67f, cy - r * .6f)) }
            val body = Path().apply { addOval(Rect(x + w / 2 - r, cy - r, x + w / 2 + r, cy + r)) }
            Path().apply { op(neck, body, PathOperation.Union) } to cy - r * .75f
        }
    }
}

/**
 * Draws one bottle: a glow behind it, the liquid at [level] (0..1 of the belly), glass and a cork.
 * A murky one has its colour gone grey-green with specks in it.
 */
fun DrawScope.flask(shape: Int, x: Float, y: Float, w: Float, h: Float, color: Color, level: Float, glow: Glow, tick: Int, seed: Int = 0) {
    val (glass, belly) = bottle(shape, x, y, w, h)
    val bottom = y + h
    val murky = glow == Glow.MURKY
    val liquid = if (murky) lerp(color, C.murky, .7f) else color
    val strength = when (glow) { Glow.BRIGHT -> .55f; Glow.SOON -> .4f; Glow.LAST -> .3f + .15f * sin((tick + seed) * .35f); Glow.MURKY -> 0f }
    if (strength > 0f) {
        val c = Offset(x + w / 2, y + h * .7f)
        drawCircle(Brush.radialGradient(listOf(color.copy(alpha = strength), color.copy(alpha = 0f)), c, w * 1.05f), w * 1.05f, c)
    }
    // A spoiled bottle still holds its murky dregs; one on its last day keeps a thin layer.
    val lv = when {
        murky -> maxOf(level, .35f)
        glow == Glow.LAST -> maxOf(level, .14f)
        level <= 0f -> 0f
        else -> level.coerceIn(.08f, 1f)
    }
    val top = bottom - (bottom - belly) * lv
    clipPath(glass) {
        drawRect(Color.White.copy(alpha = .06f), Offset(x, y), Size(w, h))
        if (lv > 0f) {
            drawRect(Brush.verticalGradient(listOf(lerp(liquid, Color.White, .25f), liquid, lerp(liquid, Color.Black, .35f)), top, bottom), Offset(x, top), Size(w, bottom - top))
            drawLine(lerp(liquid, Color.White, .55f), Offset(x, top), Offset(x + w, top), w * .04f)
            if (murky) {
                for (k in 0 until 6) {
                    val sx = x + w * (.2f + ((seed * 37 + k * 53) % 60) / 100f)
                    val sy = top + (bottom - top) * (.15f + ((seed * 11 + k * 29) % 70) / 100f)
                    drawCircle(Color(0xFF3A3A26), w * .035f, Offset(sx, sy))
                }
            } else {
                // Bubbles rising, slower as strength fades.
                val n = if (glow == Glow.BRIGHT) 3 else 2
                for (k in 0 until n) {
                    val period = 40 + k * 13
                    val ph = ((tick + seed * 7 + k * 17) % period) / period.toFloat()
                    val by = bottom - (bottom - top) * ph
                    val bx = x + w * (.35f + .3f * ((k * 41 + seed) % 10) / 10f) + sin((tick + k * 9) * .3f) * w * .03f
                    drawCircle(Color.White.copy(alpha = .45f * (1 - ph)), w * (.035f + .015f * k), Offset(bx, by))
                }
            }
        }
    }
    drawPath(glass, C.glass.copy(alpha = .75f), style = Stroke(w * .045f))
    // Shine down the left side.
    drawLine(Color.White.copy(alpha = .35f), Offset(x + w * (if (shape == 2) .2f else .4f), belly + (bottom - belly) * .15f), Offset(x + w * (if (shape == 2) .2f else .36f), belly + (bottom - belly) * .55f), w * .05f, StrokeCap.Round)
    // Cork or lid.
    if (shape == 2) drawRoundRect(C.copper, Offset(x + w * .16f, y + h * .2f), Size(w * .68f, h * .11f), CornerRadius(w * .04f))
    else {
        val cw = if (shape == 3) w * .28f else if (shape == 1) w * .44f else w * .38f
        drawRoundRect(Color(0xFFB07A48), Offset(x + w / 2 - cw / 2, y), Size(cw, h * .13f), CornerRadius(w * .03f))
        drawLine(Color(0xFF7E5230), Offset(x + w / 2 - cw / 2, y + h * .045f), Offset(x + w / 2 + cw / 2, y + h * .045f), w * .025f)
    }
}

/** A single bottle as a composable. */
@Composable
fun Flask(color: Color, level: Float, glow: Glow, size: Dp, shape: Int = 0, seed: Int = 0, modifier: Modifier = Modifier) {
    val tick = rememberTick()
    Canvas(modifier.size(size * .72f, size)) { flask(shape, 0f, 0f, this.size.width, this.size.height, color, level, glow, tick, seed) }
}

/** A stable bottle shape per ingredient name, so the same milk always stands in the same bottle. */
fun shapeOf(name: String): Int = Math.floorMod(name.lowercase().hashCode(), 4)
