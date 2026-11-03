package app.apothecary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.apothecary.data.Item
import app.apothecary.data.Place
import app.apothecary.domain.Glow
import app.apothecary.domain.Shop
import app.apothecary.domain.Words
import app.apothecary.ui.LocalRu
import app.apothecary.ui.art.Flask
import app.apothecary.ui.art.shapeOf
import app.apothecary.ui.theme.C
import app.apothecary.ui.theme.R8
import app.apothecary.ui.theme.T

fun glowColor(g: Glow): Color = when (g) {
    Glow.BRIGHT -> C.moss
    Glow.SOON -> C.amber
    Glow.LAST -> C.copper
    Glow.MURKY -> C.rose
}

/** What a row says under the name: opened, frozen, a course, where. */
@Composable
fun itemLine(shop: Shop, i: Item): String {
    val ru = LocalRu.current
    val parts = mutableListOf<String>()
    if (i.jar > 0) parts += "№${i.jar}"
    when {
        i.place == Place.ICE && i.frozen != null -> parts += if (ru) "в ларе с ${Words.date(i.frozen, true)}" else "frozen ${Words.date(i.frozen, false)}"
        i.opened != null -> parts += if (ru) "вскрыто ${Words.ago(shop.today - i.opened, true)}" else "opened ${Words.ago(shop.today - i.opened, false)}"
        i.cat == app.apothecary.data.Cat.LEFTOVER -> parts += if (ru) "сварено ${Words.ago(shop.today - i.bought, true)}" else "cooked ${Words.ago(shop.today - i.bought, false)}"
        else -> parts += if (ru) "куплено ${Words.ago(shop.today - i.bought, true)}" else "bought ${Words.ago(shop.today - i.bought, false)}"
    }
    Shop.course(i, shop.today)?.let { (n, d) -> parts += if (ru) "осталось $n шт., на ${Words.days(d.toLong(), true)}" else "$n left, ${Words.days(d.toLong(), false)}" }
    Shop.deadline(i)?.let { parts += if (ru) "до ${Words.date(it, true)}" else "until ${Words.date(it, false)}" }
    return parts.joinToString(" · ")
}

/** An ingredient in a list: its bottle, name, a line of facts and how long it has left. */
@Composable
fun IngredientRow(shop: Shop, i: Item, onClick: () -> Unit, modifier: Modifier = Modifier, selected: Boolean? = null) {
    val ru = LocalRu.current
    val g = shop.glow(i)
    val left = shop.left(i)
    Row(
        modifier.fillMaxWidth().clip(R8.Md).background(if (selected == true) C.amberSoft else C.surface).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.width(36.dp), contentAlignment = Alignment.Center) { Flask(C.cat(i.cat), shop.potency(i), g, 46.dp, shapeOf(i.name), i.id.toInt()) }
        Column(Modifier.weight(1f)) {
            Text(i.name, style = T.Strong, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(itemLine(shop, i), style = T.Small, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (selected != null) Check(selected)
        else Tag(Words.badge(left, ru), glowColor(g).copy(alpha = if (g == Glow.BRIGHT) .25f else 1f), ink = if (g == Glow.BRIGHT) C.moss else C.onAmber)
    }
}

@Composable
fun Check(on: Boolean) {
    Box(Modifier.width(26.dp).padding(2.dp).clip(R8.Sm).background(if (on) C.amber else C.surface2), contentAlignment = Alignment.Center) {
        Text(if (on) "✓" else " ", style = T.Strong.copy(color = C.onAmber))
    }
}
