package app.apothecary.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import app.apothecary.data.Item
import app.apothecary.domain.Decor
import app.apothecary.domain.Glow
import app.apothecary.domain.Shop
import app.apothecary.ui.theme.C
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The shop itself: two shelves holding the ingredients that run out first, a window that follows the
 * real hour, the cauldron on the counter in the colour of the last brew, and whatever the coins bought.
 */
@Composable
fun ShopScene(shop: Shop, hour: Int, onFlask: (Item) -> Unit, onCauldron: () -> Unit, modifier: Modifier = Modifier) {
    val tick = rememberTick()
    val shelf = shop.urgent.take(10)
    val hits = remember { mutableListOf<Pair<Rect, Item?>>() }
    val decor = shop.s.bought
    val brewColor = shop.brews.lastOrNull()?.let { Color(it.color) } ?: Color(0xFF8DBF6A)
    Canvas(modifier.fillMaxWidth().height(280.dp).pointerInput(shelf) {
        detectTapGestures { p -> hits.firstOrNull { it.first.contains(p) }?.let { (_, i) -> if (i != null) onFlask(i) else onCauldron() } }
    }) {
        hits.clear()
        val w = size.width; val h = size.height
        wall(w, h)
        window(w * .68f, h * .06f, w * .26f, h * .42f, hour, Decor.STAINED.name in decor, tick)
        if (Decor.CLOCK.name in decor) clock(w * .655f, h * .58f, w * .035f, hour)
        if (Decor.LANTERN.name in decor) lantern(w * .595f, 0f, w * .065f, h * .22f, tick)
        if (Decor.HERBS.name in decor) herbs(w * .2f, w * .58f, tick)
        // Shelves: five bottles each, the most urgent on the top shelf at eye level.
        val shelves = listOf(h * .35f, h * .63f)
        val left = w * .04f; val right = w * .62f
        val slot = (right - left) / 5
        for ((row, sy) in shelves.withIndex()) {
            for (k in 0 until 5) {
                val idx = row * 5 + k
                val it = shelf.getOrNull(idx)
                val fh = slot * 1.18f
                val fw = slot * .78f
                val fx = left + slot * k + (slot - fw) / 2
                val fy = sy - fh
                if (it != null) {
                    val glow = shop.glow(it)
                    flask(shapeOf(it.name), fx, fy, fw, fh, C.cat(it.cat), shop.potency(it), glow, tick, it.id.toInt())
                    hits += Rect(fx, fy, fx + fw, sy) to it
                } else if (row == 1 && k == 4 && Decor.BOOKS.name in decor) books(fx - slot * .1f, sy, slot * 1.2f, fh * .8f)
                else flask((idx * 3) % 4, fx + fw * .15f, fy + fh * .3f, fw * .7f, fh * .7f, C.glass.copy(alpha = .3f), -1f, Glow.MURKY, tick, idx)
            }
            plank(left - w * .02f, sy, right - left + w * .04f, h * .035f)
        }
        if (Decor.OWL.name in decor) owl(w * .81f, h * .48f, w * .07f, tick)
        // The counter.
        val ct = h * .78f
        drawRect(Brush.verticalGradient(listOf(Color(0xFF5A3A26), Color(0xFF3A2418)), ct, h), Offset(0f, ct), Size(w, h - ct))
        drawRect(Color(0xFF7A5034), Offset(0f, ct), Size(w, h * .025f))
        for (k in 1 until 6) drawLine(Color(0x22000000), Offset(w * k / 6f, ct + h * .03f), Offset(w * k / 6f, h), 2f)
        // The cauldron.
        val cx = w * .8f; val cr = w * .095f
        cauldron(cx, ct + h * .02f, cr, brewColor, Decor.COPPER.name in decor, tick)
        hits += Rect(cx - cr * 1.4f, ct - cr * 1.6f, cx + cr * 1.4f, h) to null
        if (Decor.CANDLES.name in decor) { candle(w * .56f, ct, w * .022f, h * .09f, tick, 0); candle(w * .6f, ct, w * .018f, h * .06f, tick, 5) }
        if (Decor.PLANT.name in decor) plant(w * .44f, ct, w * .05f)
        if (Decor.CRYSTAL.name in decor) crystal(w * .34f, ct, w * .04f, tick)
        if (Decor.CAT.name in decor) cat(w * .2f, ct, w * .042f, tick)
        // Candlelight falling from the left, dusk at the edges.
        drawRect(Brush.radialGradient(listOf(Color(0x00000000), Color(0x88000000)), Offset(w * .45f, h * .5f), w * .8f), Offset.Zero, size)
    }
}

private fun DrawScope.wall(w: Float, h: Float) {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF2B1C15), Color(0xFF3A271C), Color(0xFF2A1B14))), Offset.Zero, Size(w, h))
    // Planks.
    var x = 0f; var k = 0
    while (x < w) {
        val pw = w * (.11f + (k * 37 % 5) * .008f)
        drawLine(Color(0x33000000), Offset(x, 0f), Offset(x, h), 2.5f)
        drawLine(Color(0x14FFD9A0), Offset(x + 3f, 0f), Offset(x + 3f, h), 1f)
        x += pw; k++
    }
    // A beam across the top.
    drawRect(Color(0xFF241610), Offset(0f, 0f), Size(w, h * .05f))
    drawRect(Color(0x22FFD9A0), Offset(0f, h * .05f), Size(w, 2f))
    // Warm light pooling in the middle.
    drawCircle(Brush.radialGradient(listOf(Color(0x40F0B45A), Color(0x00F0B45A)), Offset(w * .4f, h * .55f), w * .55f), w * .55f, Offset(w * .4f, h * .55f))
}

private fun DrawScope.plank(x: Float, y: Float, w: Float, t: Float) {
    drawRoundRect(Color(0xFF6B4630), Offset(x, y), Size(w, t), CornerRadius(3f))
    drawRect(Color(0x55FFE0B0), Offset(x, y), Size(w, 2f))
    drawRect(Color(0x55000000), Offset(x, y + t), Size(w, t * .6f))
    // Brackets.
    for (bx in listOf(x + w * .08f, x + w * .92f)) drawLine(Color(0xFF3A2418), Offset(bx, y + t), Offset(bx - t * .9f, y + t * 2.4f), t * .45f)
}

/** An arched window: sky by the hour, stars and a moon at night; stained glass if bought. */
private fun DrawScope.window(x: Float, y: Float, w: Float, h: Float, hour: Int, stained: Boolean, tick: Int) {
    val arch = Path().apply {
        moveTo(x, y + h); lineTo(x, y + w / 2)
        arcTo(Rect(x, y, x + w, y + w), 180f, 180f, false)
        lineTo(x + w, y + h); close()
    }
    val night = hour < 6 || hour >= 20
    val dusk = hour in 17..19 || hour in 6..7
    val sky = when {
        night -> listOf(Color(0xFF0E1630), Color(0xFF1D2A4E))
        dusk -> listOf(Color(0xFF3D3A6E), Color(0xFFE9926A))
        else -> listOf(Color(0xFF7FB2D9), Color(0xFFE8D9B0))
    }
    clipPath(arch) {
        drawRect(Brush.verticalGradient(sky, y, y + h), Offset(x, y), Size(w, h))
        if (night) {
            for (k in 0 until 9) {
                val sx = x + w * ((k * 37 % 100) / 100f); val sy = y + h * ((k * 53 % 70) / 100f)
                val a = .5f + .5f * sin((tick + k * 7) * .15f)
                drawCircle(Color.White.copy(alpha = a), 1.6f, Offset(sx, sy))
            }
            drawCircle(Color(0xFFF4ECCF), w * .12f, Offset(x + w * .68f, y + h * .3f))
            drawCircle(sky[0], w * .1f, Offset(x + w * .74f, y + h * .27f))
        } else {
            drawCircle(Color(0x66FFF6D8), w * .16f, Offset(x + w * .3f, y + h * .35f))
            // Rooftops across the lane.
            val roof = Path().apply {
                moveTo(x, y + h * .82f); lineTo(x + w * .2f, y + h * .66f); lineTo(x + w * .42f, y + h * .82f); lineTo(x + w * .55f, y + h * .72f)
                lineTo(x + w * .78f, y + h * .6f); lineTo(x + w, y + h * .74f); lineTo(x + w, y + h); lineTo(x, y + h); close()
            }
            drawPath(roof, Color(0x667A5C6A))
        }
        if (stained) {
            val panes = listOf(Color(0x66C8463F), Color(0x66F0B45A), Color(0x664F8FC9), Color(0x6658B368), Color(0x66B4407A), Color(0x669C7BD6))
            val cols = 3; val rows = 5
            for (r in 0 until rows) for (c in 0 until cols) {
                drawRect(panes[(r * 2 + c) % panes.size], Offset(x + w * c / cols, y + h * r / rows), Size(w / cols, h / rows))
            }
            for (c in 1 until cols) drawLine(Color(0xFF241610), Offset(x + w * c / cols, y), Offset(x + w * c / cols, y + h), 2.5f)
            for (r in 1 until rows) drawLine(Color(0xFF241610), Offset(x, y + h * r / rows), Offset(x + w, y + h * r / rows), 2.5f)
        }
    }
    drawPath(arch, Color(0xFF5A3A26), style = Stroke(w * .06f))
    drawLine(Color(0xFF5A3A26), Offset(x + w / 2, y), Offset(x + w / 2, y + h), w * .035f)
    drawLine(Color(0xFF5A3A26), Offset(x, y + h * .55f), Offset(x + w, y + h * .55f), w * .035f)
    drawRect(Color(0xFF6B4630), Offset(x - w * .08f, y + h), Size(w * 1.16f, h * .06f))
}

private fun DrawScope.cauldron(cx: Float, top: Float, r: Float, brew: Color, copper: Boolean, tick: Int) {
    val metal = if (copper) Color(0xFFB8673A) else Color(0xFF2E2A28)
    val rim = if (copper) Color(0xFFE59A62) else Color(0xFF4A4440)
    // Steam.
    for (k in 0 until 3) {
        val ph = ((tick + k * 14) % 42) / 42f
        val sx = cx + sin((tick + k * 20) * .12f) * r * .3f + (k - 1) * r * .35f
        drawCircle(Color.White.copy(alpha = .14f * (1 - ph)), r * (.18f + ph * .3f), Offset(sx, top - r * .3f - ph * r * 1.6f))
    }
    // Body.
    val by = top + r * .35f
    drawCircle(metal, r, Offset(cx, by))
    drawCircle(Color.White.copy(alpha = .1f), r * .7f, Offset(cx - r * .3f, by - r * .1f))
    // Legs.
    drawRect(metal, Offset(cx - r * .7f, by + r * .7f), Size(r * .16f, r * .45f))
    drawRect(metal, Offset(cx + r * .54f, by + r * .7f), Size(r * .16f, r * .45f))
    // The brew at the rim, glowing.
    drawCircle(Brush.radialGradient(listOf(brew.copy(alpha = .5f), brew.copy(alpha = 0f)), Offset(cx, top - r * .3f), r * 1.6f), r * 1.6f, Offset(cx, top - r * .3f))
    drawOval(lerp(brew, Color.White, .15f), Offset(cx - r * .92f, top - r * .2f), Size(r * 1.84f, r * .42f))
    for (k in 0 until 3) {
        val ph = ((tick + k * 11) % 24) / 24f
        drawCircle(lerp(brew, Color.White, .5f).copy(alpha = 1 - ph), r * .08f * (1 + ph), Offset(cx + (k - 1) * r * .45f, top - r * .02f - ph * r * .1f))
    }
    drawOval(rim, Offset(cx - r * 1.02f, top - r * .26f), Size(r * 2.04f, r * .52f), style = Stroke(r * .12f))
}

private fun DrawScope.candle(x: Float, base: Float, w: Float, h: Float, tick: Int, seed: Int) {
    val top = base - h
    // A slow, soft breathing of the flame, not a flicker.
    val f = .95f + .035f * sin((tick + seed) * .11f) + .015f * sin((tick + seed) * .29f)
    drawCircle(Brush.radialGradient(listOf(Color(0x66FFC46A), Color(0x00FFC46A)), Offset(x, top - w), w * 6 * f), w * 6 * f, Offset(x, top - w))
    drawRoundRect(Color(0xFFF2E3C2), Offset(x - w / 2, top), Size(w, h), CornerRadius(w * .2f))
    drawLine(Color(0xFF2E1E14), Offset(x, top), Offset(x, top - w * .4f), w * .15f)
    val flame = Path().apply {
        moveTo(x, top - w * 2.1f * f); quadraticTo(x + w * .55f, top - w * .9f, x, top - w * .25f); quadraticTo(x - w * .55f, top - w * .9f, x, top - w * 2.1f * f); close()
    }
    drawPath(flame, Color(0xFFFFD27A))
}

private fun DrawScope.herbs(from: Float, to: Float, tick: Int) {
    val n = 4
    for (k in 0 until n) {
        val x = from + (to - from) * k / (n - 1)
        val sway = sin((tick + k * 9) * .08f) * 3f
        val len = size.height * (.12f + (k % 2) * .03f)
        drawLine(Color(0xFF8E7A66), Offset(x, size.height * .05f), Offset(x + sway, size.height * .05f + len * .3f), 2f)
        val c = listOf(Color(0xFF6E8F4E), Color(0xFF9C7BD6), Color(0xFF7E9A52), Color(0xFFC9A66B))[k % 4]
        for (j in 0 until 7) {
            val a = (j - 3) * .25f
            drawLine(c, Offset(x + sway, size.height * .05f + len * .3f), Offset(x + sway + sin(a) * len * .35f, size.height * .05f + len * .3f + cos(a) * len * .7f), 4f, StrokeCap.Round)
        }
    }
}

private fun DrawScope.lantern(x: Float, y: Float, w: Float, h: Float, tick: Int) {
    drawLine(Color(0xFF3A2418), Offset(x + w / 2, y), Offset(x + w / 2, y + h * .4f), 2f)
    val f = .96f + .04f * sin(tick * .09f)
    val c = Offset(x + w / 2, y + h * .62f)
    drawCircle(Brush.radialGradient(listOf(Color(0x77FFC46A), Color(0x00FFC46A)), c, w * 2.6f * f), w * 2.6f * f, c)
    drawRoundRect(Color(0xFF241610), Offset(x, y + h * .4f), Size(w, h * .45f), CornerRadius(w * .15f))
    drawRoundRect(Color(0xFFFFD27A).copy(alpha = .9f * f), Offset(x + w * .18f, y + h * .46f), Size(w * .64f, h * .33f), CornerRadius(w * .1f))
}

private fun DrawScope.clock(cx: Float, cy: Float, r: Float, hour: Int) {
    drawCircle(Color(0xFF5A3A26), r * 1.15f, Offset(cx, cy))
    drawCircle(Color(0xFFF2E3C2), r, Offset(cx, cy))
    val ha = (hour % 12) / 12f * 2 * PI - PI / 2
    drawLine(Color(0xFF2E1E14), Offset(cx, cy), Offset(cx + cos(ha).toFloat() * r * .5f, cy + sin(ha).toFloat() * r * .5f), r * .14f, StrokeCap.Round)
    drawLine(Color(0xFF2E1E14), Offset(cx, cy), Offset(cx, cy - r * .78f), r * .09f, StrokeCap.Round)
    drawLine(Color(0xFF5A3A26), Offset(cx, cy + r), Offset(cx, cy + r * 2.4f), r * .08f)
    drawCircle(Color(0xFFD07A44), r * .25f, Offset(cx, cy + r * 2.5f))
}

private fun DrawScope.books(x: Float, base: Float, w: Float, h: Float) {
    val cs = listOf(Color(0xFF7A2E2A), Color(0xFF2E4A6B), Color(0xFF4E6B3A), Color(0xFF6B4A2E))
    var bx = x
    for (k in 0 until 4) {
        val bw = w * (.18f + (k % 2) * .04f); val bh = h * (.75f + (k % 3) * .1f)
        drawRect(cs[k], Offset(bx, base - bh), Size(bw, bh))
        drawLine(Color(0x88F0B45A), Offset(bx, base - bh * .8f), Offset(bx + bw, base - bh * .8f), 1.5f)
        bx += bw + 1.5f
    }
}

private fun DrawScope.owl(x: Float, y: Float, s: Float, tick: Int) {
    drawLine(Color(0xFF5A3A26), Offset(x - s, y + s * 1.2f), Offset(x + s, y + s * 1.2f), s * .15f)
    drawOval(Color(0xFF7A5A40), Offset(x - s * .6f, y - s * .2f), Size(s * 1.2f, s * 1.45f))
    drawOval(Color(0xFFC9A66B), Offset(x - s * .35f, y + s * .4f), Size(s * .7f, s * .7f))
    val blink = (tick % 80) < 3
    for (dx in listOf(-.25f, .25f)) {
        drawCircle(Color(0xFFF2E3C2), s * .2f, Offset(x + dx * s, y + s * .2f))
        if (!blink) drawCircle(Color(0xFF241610), s * .09f, Offset(x + dx * s, y + s * .2f))
        else drawLine(Color(0xFF241610), Offset(x + dx * s - s * .15f, y + s * .2f), Offset(x + dx * s + s * .15f, y + s * .2f), s * .06f)
    }
    val beak = Path().apply { moveTo(x - s * .07f, y + s * .32f); lineTo(x + s * .07f, y + s * .32f); lineTo(x, y + s * .45f); close() }
    drawPath(beak, Color(0xFFF0B45A))
    for (dx in listOf(-.45f, .45f)) drawLine(Color(0xFF7A5A40), Offset(x + dx * s * .7f, y - s * .1f), Offset(x + dx * s, y - s * .4f), s * .12f, StrokeCap.Round)
}

private fun DrawScope.plant(x: Float, base: Float, s: Float) {
    for ((k, a) in listOf(-.7f, -.25f, .2f, .65f).withIndex()) {
        val c = Offset(x + sin(a) * s * 1.1f, base - s * 1.2f - cos(a) * s * .9f)
        drawLine(Color(0xFF4E7A3A), Offset(x, base - s * .9f), c, s * .1f)
        drawOval(if (k % 2 == 0) Color(0xFF6E9F4E) else Color(0xFF58B368), Offset(c.x - s * .35f, c.y - s * .2f), Size(s * .7f, s * .4f))
    }
    val pot = Path().apply { moveTo(x - s * .55f, base - s * .9f); lineTo(x + s * .55f, base - s * .9f); lineTo(x + s * .4f, base); lineTo(x - s * .4f, base); close() }
    drawPath(pot, Color(0xFFB8673A))
}

private fun DrawScope.crystal(x: Float, base: Float, r: Float, tick: Int) {
    val c = Offset(x, base - r * 1.2f)
    val a = .35f + .2f * sin(tick * .1f)
    drawCircle(Brush.radialGradient(listOf(Color(0xFF9C7BD6).copy(alpha = a), Color(0x009C7BD6)), c, r * 2.6f), r * 2.6f, c)
    drawCircle(Brush.radialGradient(listOf(Color(0xFFE6DDFF), Color(0xFF7A62B8)), Offset(c.x - r * .3f, c.y - r * .3f), r * 1.4f), r, c)
    drawRect(Color(0xFF6B4630), Offset(x - r * .7f, base - r * .3f), Size(r * 1.4f, r * .3f))
}

/** A black cat sitting on the counter, tail swaying, blinking now and then. */
private fun DrawScope.cat(x: Float, base: Float, s: Float, tick: Int) {
    val col = Color(0xFF15100E)
    val tail = Path().apply {
        moveTo(x + s * .7f, base - s * .2f)
        quadraticTo(x + s * 1.6f, base - s * .2f, x + s * 1.4f + sin(tick * .1f) * s * .35f, base - s * 1.3f)
    }
    drawPath(tail, col, style = Stroke(s * .2f, cap = StrokeCap.Round))
    drawOval(col, Offset(x - s * .75f, base - s * 1.5f), Size(s * 1.5f, s * 1.5f))
    drawCircle(col, s * .55f, Offset(x, base - s * 1.8f))
    for (dx in listOf(-1f, 1f)) {
        val ear = Path().apply { moveTo(x + dx * s * .5f, base - s * 1.95f); lineTo(x + dx * s * .45f, base - s * 2.55f); lineTo(x + dx * s * .1f, base - s * 2.25f); close() }
        drawPath(ear, col)
    }
    val blink = (tick % 70) < 3
    for (dx in listOf(-.22f, .22f)) {
        if (blink) drawLine(Color(0xFFF0B45A), Offset(x + dx * s - s * .1f, base - s * 1.85f), Offset(x + dx * s + s * .1f, base - s * 1.85f), s * .05f)
        else drawOval(Color(0xFFF0C35A), Offset(x + dx * s - s * .09f, base - s * 1.95f), Size(s * .18f, s * .2f))
    }
}
