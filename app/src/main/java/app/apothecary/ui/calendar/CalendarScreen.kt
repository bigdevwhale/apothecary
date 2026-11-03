package app.apothecary.ui.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.apothecary.data.Fate
import app.apothecary.domain.Book
import app.apothecary.domain.Shop
import app.apothecary.domain.Words
import app.apothecary.ui.LocalRu
import app.apothecary.ui.Nav
import app.apothecary.ui.Overlay
import app.apothecary.ui.components.Card
import app.apothecary.ui.components.IngredientRow
import app.apothecary.ui.components.Section
import app.apothecary.ui.components.t
import app.apothecary.ui.theme.C
import app.apothecary.ui.theme.R8
import app.apothecary.ui.theme.T
import java.time.LocalDate

/**
 * The shop's moon calendar: each day shows which ingredients lose their strength then (coloured by essence),
 * and on past days what was brewed and what was thrown away.
 */
@Composable
fun CalendarScreen(shop: Shop, nav: Nav) {
    val ru = LocalRu.current
    var month by rememberSaveable { mutableLongStateOf(LocalDate.ofEpochDay(shop.today).withDayOfMonth(1).toEpochDay()) }
    var picked by rememberSaveable { mutableLongStateOf(shop.today) }
    val first = LocalDate.ofEpochDay(month)
    val deadlines = remember(shop) { shop.stock.mapNotNull { i -> Shop.deadline(i)?.let { it to i } }.groupBy({ it.first }, { it.second }) }
    val brewed = remember(shop) { shop.brews.groupBy { it.day } }
    val tossed = remember(shop) { shop.items.filter { it.fate == Fate.TOSSED }.groupBy { it.closed ?: -1 } }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 14.dp, end = 14.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Arrow("‹") { month = first.minusMonths(1).toEpochDay() }
            Text((if (ru) Words.monthNamesRu else Words.monthNamesEn)[first.monthValue - 1] + " " + first.year, style = T.Title, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            Arrow("›") { month = first.plusMonths(1).toEpochDay() }
        }
        Row {
            for (d in if (ru) listOf("пн", "вт", "ср", "чт", "пт", "сб", "вс") else listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"))
                Text(d, style = T.Small, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        }
        val offset = first.dayOfWeek.value - 1
        val days = first.lengthOfMonth()
        val rows = (offset + days + 6) / 7
        for (r in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (c in 0 until 7) {
                    val n = r * 7 + c - offset + 1
                    if (n !in 1..days) { Box(Modifier.weight(1f).aspectRatio(.8f)); continue }
                    val day = first.withDayOfMonth(n).toEpochDay()
                    val due = deadlines[day].orEmpty()
                    val past = day < shop.today
                    Column(
                        Modifier.weight(1f).aspectRatio(.8f).clip(R8.Sm)
                            .background(if (day == picked) C.amberSoft else C.surface)
                            .border(1.dp, if (day == shop.today) C.amber else C.line, R8.Sm)
                            .clickable { picked = day }.padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("$n", style = T.Strong.copy(color = if (past) C.faint else C.ink))
                        Canvas(Modifier.fillMaxWidth().height(14.dp)) {
                            // Filled dots in the essence's colour: runs out that day. Amber rings: brewed. Red crosses: poured out.
                            val dots = buildList {
                                for (i in due.take(4)) add(C.cat(i.cat) to 0)
                                if (past) {
                                    repeat(minOf(brewed[day]?.size ?: 0, 2)) { add(C.amber to 1) }
                                    repeat(minOf(tossed[day]?.size ?: 0, 2)) { add(C.rose to 2) }
                                }
                            }
                            val step = size.width / 5
                            for ((k, d) in dots.take(5).withIndex()) mark(d.first, d.second, Offset(step * (k + .5f) + (5 - minOf(dots.size, 5)) * step / 2, size.height / 2))
                        }
                    }
                }
            }
        }
        Legend()

        val due = deadlines[picked].orEmpty()
        val bs = brewed[picked].orEmpty()
        val ts = tossed[picked].orEmpty()
        Section(Words.date(picked, ru) + if (picked == shop.today) t(", сегодня", ", today") else "")
        if (due.isEmpty() && bs.isEmpty() && ts.isEmpty()) Text(t("В этот день ничего не теряет силу.", "Nothing loses its strength that day."), style = T.Muted)
        if (due.isNotEmpty()) {
            Text(if (picked < shop.today) t("Срок вышел:", "Ran out:") else t("Последний день у:", "Last day for:"), style = T.Small)
            for (i in due) IngredientRow(shop, i, { nav.go(Overlay.Ingredient(i.id)) })
        }
        for (b in bs) Card {
            Text(Book.potion(b.potion).name(ru), style = T.Name.copy(color = Color(b.color)))
            Text(b.names.lines().joinToString(", "), style = T.Muted)
        }
        if (ts.isNotEmpty()) Text(t("Вылито: ", "Poured out: ") + ts.joinToString(", ") { it.name }, style = T.Muted.copy(color = C.rose))
    }
}

@Composable
private fun Arrow(s: String, onClick: () -> Unit) {
    Box(Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) { Text(s, style = T.Title.copy(color = C.amber)) }
}

@Composable
private fun Legend() {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
        for (k in listOf(Triple(C.moss, 0, t("срок (цвет эссенции)", "runs out (essence colour)")), Triple(C.amber, 1, t("сварено", "brewed")), Triple(C.rose, 2, t("вылито", "poured out")))) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Canvas(Modifier.size(10.dp)) { mark(k.first, k.second, center) }
                Text(k.third, style = T.Small)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.mark(col: Color, kind: Int, at: Offset) {
    val r = 3.2.dp.toPx()
    when (kind) {
        0 -> drawCircle(col, r, at)
        1 -> drawCircle(col, r * .85f, at, style = androidx.compose.ui.graphics.drawscope.Stroke(1.6.dp.toPx()))
        else -> {
            drawLine(col, Offset(at.x - r, at.y - r), Offset(at.x + r, at.y + r), 1.8.dp.toPx())
            drawLine(col, Offset(at.x + r, at.y - r), Offset(at.x - r, at.y + r), 1.8.dp.toPx())
        }
    }
}
