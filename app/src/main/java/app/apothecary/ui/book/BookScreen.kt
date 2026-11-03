package app.apothecary.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.drawText
import app.apothecary.domain.Book
import app.apothecary.domain.Glow
import app.apothecary.domain.Potion
import app.apothecary.domain.Shop
import app.apothecary.domain.Words
import app.apothecary.ui.LocalRu
import app.apothecary.ui.Nav
import app.apothecary.ui.art.Flask
import app.apothecary.ui.components.Bar
import app.apothecary.ui.components.Card
import app.apothecary.ui.components.Chip
import app.apothecary.ui.components.Flow
import app.apothecary.ui.components.Section
import app.apothecary.ui.components.t
import app.apothecary.ui.theme.C
import app.apothecary.ui.theme.R8
import app.apothecary.ui.theme.T

@Composable
fun BookScreen(shop: Shop, nav: Nav) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Flow {
            Chip(t("Рецепты", "Recipes"), page == 0, { page = 0 })
            Chip(t("Книга учёта", "Ledger"), page == 1, { page = 1 })
            Chip(t("Гильдия", "Guild"), page == 2, { page = 2 })
        }
        when (page) {
            0 -> Recipes(shop)
            1 -> LedgerPage(shop)
            else -> Guild(shop)
        }
    }
}

/** The colour a recipe shows before it's brewed: its essences blended. */
fun Potion.color(): Color = Color(Shop.blend(needs.toList().ifEmpty { listOf(app.apothecary.data.Cat.BERRY, app.apothecary.data.Cat.DRINK) }))

@Composable
private fun Recipes(shop: Shop) {
    val ru = LocalRu.current
    val known = shop.known
    val counts = shop.brews.groupingBy { it.potion }.eachCount()
    Text(if (ru) "Открыто ${known.size} из ${Book.potions.size}" else "${known.size} of ${Book.potions.size} found", style = T.Italic)
    Text(t("Зелье получается из сочетания эссенций в котле. Чем больше разных эссенций, тем реже рецепт.", "A potion comes from the mix of essences in the pot. The more different essences, the rarer the recipe."), style = T.Small)
    for (row in Book.potions.chunked(3)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (p in row) {
                val has = p.key in known
                Column(
                    Modifier.weight(1f).height(158.dp).clip(R8.Md).background(if (has) C.surface else C.bg).border(1.dp, if (p.rare && has) C.amber else C.line, R8.Md).padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val col = p.color()
                    Box(Modifier.height(66.dp), contentAlignment = Alignment.Center) {
                        Flask(if (has) col else C.glass.copy(alpha = .4f), if (has) .85f else -1f, if (has) Glow.BRIGHT else Glow.MURKY, 62.dp, p.shape, p.key.hashCode())
                    }
                    Text(if (has) p.name(ru) else "???", style = T.Label.copy(color = if (has) C.ink else C.faint), textAlign = TextAlign.Center, maxLines = 3)
                    val hint = if (p.rare) Book.rareHint(p.key, ru) else Words.list(p.needs.map { Words.cat(it, ru) }, ru)
                    Text(hint, style = T.Small.copy(fontSize = T.Small.fontSize * .9f), textAlign = TextAlign.Center, maxLines = 3)
                    if (has) Text("×${counts[p.key] ?: 0}", style = T.Small.copy(color = C.amber))
                }
            }
            repeat(3 - row.size) { Box(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun LedgerPage(shop: Shop) {
    val ru = LocalRu.current
    var span by rememberSaveable { mutableIntStateOf(0) }
    Flow {
        Chip(t("30 дней", "30 days"), span == 0, { span = 0 })
        Chip(t("Год", "Year"), span == 1, { span = 1 })
        Chip(t("Всё время", "All time"), span == 2, { span = 2 })
    }
    val from = when (span) { 0 -> shop.today - 29; 1 -> shop.today - 364; else -> Long.MIN_VALUE / 2 }
    val l = shop.ledger(from, shop.today)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Stat("${l.brews}", t("зелий сварено", "potions brewed"), Modifier.weight(1f))
        Stat("${l.rescued}", t("спасено в последний день", "saved on the last day"), Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Stat("${l.tossed}", t("вылито", "poured out"), Modifier.weight(1f), if (l.tossed > 0) C.rose else C.moss)
        Stat("${(l.waste * 100).toInt()}%", t("доля потерь", "share lost"), Modifier.weight(1f), if (l.waste > .15f) C.rose else C.moss)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Stat(Words.money(l.tossedMoney, ru), t("ушло в помои", "down the drain"), Modifier.weight(1f), C.rose)
        Stat("${shop.clean}", t("дней без потерь", "days without loss"), Modifier.weight(1f), C.moss)
    }
    Section(t("Восемь недель", "Eight weeks"))
    Weeks(shop)
    if (l.byCat.isNotEmpty()) {
        Section(t("Что пропадает", "What goes to waste"))
        val max = l.byCat.maxOf { it.second }
        Card {
            for ((c, n) in l.byCat) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(Words.cat(c, ru), style = T.Body, modifier = Modifier.width(120.dp))
                    Bar(n.toFloat() / max, Modifier.weight(1f), C.cat(c))
                    Text(" $n", style = T.Small)
                }
            }
        }
    }
    if (l.worst.isNotEmpty()) {
        Section(t("Чаще всего вылито", "Most often poured out"))
        Flow { for ((name, n) in l.worst) app.apothecary.ui.components.Tag("$name ×$n", C.rose.copy(alpha = .2f), ink = C.rose) }
        Text(t("Может, покупать их поменьше или сразу класть в ларь?", "Maybe buy less of these, or put them straight in the ice chest?"), style = T.Small)
    }
    if (l.potions.isNotEmpty()) {
        Section(t("Любимые зелья", "Favourite potions"))
        Card(padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
            for ((key, n) in l.potions.take(5)) {
                val p = Book.potion(key)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 2.dp)) {
                    Flask(p.color(), .85f, Glow.BRIGHT, 34.dp, p.shape, key.hashCode())
                    Text(p.name(ru), style = T.Name, modifier = Modifier.weight(1f))
                    Text("×$n", style = T.Strong.copy(color = C.amber))
                }
            }
        }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier, color: Color = C.amber) {
    Card(modifier, padding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
        Text(value, style = T.Number.copy(color = color))
        Text(label, style = T.Small)
    }
}

@Composable
private fun Guild(shop: Shop) {
    val ru = LocalRu.current
    val m = shop.month
    Card {
        Text(t("Ваше звание", "Your rank"), style = T.Eyebrow)
        Text(Words.rank(shop.rank, ru), style = T.Display.copy(color = C.amber))
        val next = shop.nextRank()
        if (next == null) Text(t("Выше званий в гильдии нет. Держите его: звание считается по последним тридцати дням.", "There's no higher rank. Keep it: the rank looks at the last thirty days."), style = T.Muted)
        else {
            val (brews, waste) = next
            Text((if (ru) "До звания «${Words.rank(shop.rank + 1, true)}»" else "To ${Words.rank(shop.rank + 1, false)}"), style = T.Strong, modifier = Modifier.padding(top = 8.dp))
            Text(if (ru) "Зелий всего: ${shop.brews.size} из $brews" else "Potions in all: ${shop.brews.size} of $brews", style = T.Muted)
            Bar(shop.brews.size.toFloat() / brews, Modifier.fillMaxWidth().padding(vertical = 6.dp))
            val ok = m.waste <= waste
            Text(if (ru) "Потери за 30 дней: ${(m.waste * 100).toInt()}%, нужно не больше ${(waste * 100).toInt()}%" else "Lost in 30 days: ${(m.waste * 100).toInt()}%, at most ${(waste * 100).toInt()}% allowed", style = T.Muted.copy(color = if (ok) C.moss else C.rose))
        }
    }
    Section(t("Лестница званий", "The ranks"))
    for (r in 0..3) {
        val req = when (r) {
            0 -> t("с первого дня", "from day one")
            1 -> t("10 зелий, потери до 30%", "10 potions, under 30% lost")
            2 -> t("40 зелий, потери до 15%", "40 potions, under 15% lost")
            else -> t("100 зелий, потери до 7%", "100 potions, under 7% lost")
        }
        Row(Modifier.fillMaxWidth().clip(R8.Md).background(if (r == shop.rank) C.amberSoft else C.surface).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(Words.rank(r, ru), style = T.Name.copy(color = if (r <= shop.rank) C.amber else C.muted), modifier = Modifier.weight(1f))
            Text(req, style = T.Small)
        }
    }
    Text(t("Потери — это то, что вылито, от всего, что ушло с полок. Сваренное, отданное и замороженное потерей не считается.",
        "Loss is what was poured out, out of everything that left the shelves. Brewed, given away and frozen don't count as loss."), style = T.Small)
}

/** Brewed (amber, up) against poured out (red, down), week by week, this week on the right. */
@Composable
private fun Weeks(shop: Shop) {
    val ru = LocalRu.current
    val weeks = (7 downTo 0).map { w ->
        val to = shop.today - w * 7L; val from = to - 6
        shop.brews.count { it.day in from..to } to shop.items.count { it.fate == app.apothecary.data.Fate.TOSSED && it.cat.food && (it.closed ?: -1) in from..to }
    }
    val max = weeks.maxOf { maxOf(it.first, it.second * 2) }.coerceAtLeast(1)
    Card {
        val measurer = androidx.compose.ui.text.rememberTextMeasurer()
        val label = T.Small.copy(color = C.muted)
        androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(110.dp)) {
            val slot = size.width / weeks.size
            val top = 16.dp.toPx()
            val mid = size.height * .8f
            drawLine(C.line, androidx.compose.ui.geometry.Offset(0f, mid), androidx.compose.ui.geometry.Offset(size.width, mid), 1.dp.toPx())
            for ((k, w) in weeks.withIndex()) {
                val x = slot * k + slot * .22f; val bw = slot * .56f
                val up = (mid - top) * w.first / max
                if (w.first > 0) {
                    val m = measurer.measure("${w.first}", label)
                    drawText(m, topLeft = androidx.compose.ui.geometry.Offset(x + bw / 2 - m.size.width / 2, mid - up - m.size.height - 2.dp.toPx()))
                }
                drawRoundRect(if (k == weeks.lastIndex) C.amber else C.amber.copy(alpha = .55f), androidx.compose.ui.geometry.Offset(x, mid - up), androidx.compose.ui.geometry.Size(bw, up), androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
                val down = (size.height - mid) * (w.second * 2f / max).coerceAtMost(1f)
                if (w.second > 0) drawRoundRect(C.rose, androidx.compose.ui.geometry.Offset(x, mid + 2.dp.toPx()), androidx.compose.ui.geometry.Size(bw, down), androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
            Text(t("8 недель назад", "8 weeks ago"), style = T.Small, modifier = Modifier.weight(1f))
            Text(t("эта неделя", "this week"), style = T.Small)
        }
        Text(if (ru) "Сварено за неделю: ${weeks.last().first}, вылито: ${weeks.last().second}" else "This week: ${weeks.last().first} brewed, ${weeks.last().second} poured out", style = T.Muted, modifier = Modifier.padding(top = 4.dp))
    }
}
