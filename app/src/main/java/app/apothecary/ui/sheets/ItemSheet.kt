package app.apothecary.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.apothecary.container
import app.apothecary.data.Fate
import app.apothecary.data.Item
import app.apothecary.data.Place
import app.apothecary.domain.Glow
import app.apothecary.domain.Shop
import app.apothecary.domain.Words
import app.apothecary.ui.LocalRu
import app.apothecary.ui.Nav
import app.apothecary.ui.Overlay
import app.apothecary.ui.art.Flask
import app.apothecary.ui.art.shapeOf
import app.apothecary.ui.components.Bar
import app.apothecary.ui.components.Btn
import app.apothecary.ui.components.Card
import app.apothecary.ui.components.Kind
import app.apothecary.ui.components.Note
import app.apothecary.ui.components.Sheet
import app.apothecary.ui.components.Tag
import app.apothecary.ui.components.glowColor
import app.apothecary.ui.components.t
import app.apothecary.ui.theme.C
import app.apothecary.ui.theme.T

/** One ingredient up close: its facts and what can be done with it. */
@Composable
fun ItemSheet(shop: Shop, i: Item, nav: Nav) {
    val ru = LocalRu.current
    val c = LocalContext.current.container
    val today = shop.today
    val g = shop.glow(i)
    val left = shop.left(i)
    val med = !i.cat.food
    var sure by remember { mutableStateOf(false) }
    Sheet(onDismiss = nav::close) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.padding(start = 6.dp)) { Flask(C.of(i), shop.potency(i), if (i.fate != null) Glow.MURKY else g, 110.dp, shapeOf(i.name), i.id.toInt()) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(i.name, style = T.Title)
                Text("${i.essence.ifBlank { Words.cat(i.cat, ru) }} · ${Words.place(i.place, ru).lowercase()}", style = T.Muted)
                if (i.fate == null) Tag(Words.left(left, ru), glowColor(g))
                else Tag(fate(i.fate, ru), C.surface2, ink = C.muted)
            }
        }
        if (i.fate == null && left != null) Bar(shop.potency(i), Modifier.fillMaxWidth(), glowColor(g))

        Card {
            if (i.amount > 0) Fact(t("Сколько", "How much"), Words.amount(i.amount) + " " + Words.measure(i.measure ?: app.apothecary.data.Measure.PCS, ru))
            Fact(t("Куплено", "Bought"), Words.date(i.bought, ru, year = true))
            i.printed?.let { Fact(t("Срок на упаковке", "Package date"), Words.date(it, ru, year = true)) }
            i.opened?.let { Fact(t("Вскрыто", "Opened"), Words.date(it, ru) + (i.openDays?.let { d -> if (ru) ", открытым годно ${Words.days(d.toLong(), true)}" else ", keeps ${Words.days(d.toLong(), false)} open" } ?: "")) }
            if (i.opened == null && i.openDays != null && i.place != Place.ICE) Fact(t("После вскрытия", "Once opened"), if (ru) "годно ${Words.days(i.openDays.toLong(), true)}" else "keeps ${Words.days(i.openDays.toLong(), false)}")
            i.frozen?.let { Fact(t("В ларе", "In the ice chest"), if (ru) "с ${Words.date(it, true)}" else "since ${Words.date(it, false)}") }
            Shop.deadline(i)?.let { Fact(t("Сила иссякнет", "Strength runs out"), Words.date(it, ru, year = true)) }
            Shop.course(i, today)?.let { (n, d) -> Fact(t("Курс", "Course"), if (ru) "${i.perDay} в день, осталось $n, хватит на ${Words.days(d.toLong(), true)}" else "${i.perDay} a day, $n left, ${Words.days(d.toLong(), false)}") }
            if (i.jar > 0) Fact(t("На крышке", "On the lid"), "№${i.jar}")
            if (i.price > 0) Fact(t("Цена", "Price"), Words.money(i.price.toLong(), ru))
        }

        if (i.fate != null) {
            Btn(t("Вернуть на полку", "Put back on the shelf"), { c.write { clerk.reopen(i) }; nav.close() }, Modifier.fillMaxWidth(), kind = Kind.SOFT)
            return@Sheet
        }

        if (g == Glow.MURKY && !med) Note(t("Колба помутнела: срок вышел. Если не уверены, лучше не рисковать.", "The bottle has gone murky: the date has passed. If in doubt, don't risk it."), t("Кот", "The cat"), edge = C.rose)

        if (i.opened == null && i.openDays != null && i.place != Place.ICE)
            Btn(t("Сорвать печать: вскрыто сегодня", "Break the seal: opened today"), { c.write { clerk.open(i, today) }; nav.toast(if (ru) "Печать сорвана" else "Seal broken") }, Modifier.fillMaxWidth(), kind = Kind.COPPER)

        if (!med) {
            Btn(t("В котёл", "Into the cauldron"), { nav.go(Overlay.Brew(listOf(i.id))) }, Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (i.place == Place.ICE) Btn(t("Достать из ларя", "Take out to thaw"), { c.write { clerk.thaw(i, today) }; nav.toast(if (ru) "Оттаивает в погребке" else "Thawing in the cellar") }, Modifier.weight(1f), kind = Kind.SOFT)
                else Btn(t("В ледяной ларь", "Into the ice chest"), { c.write { clerk.freeze(i, today) }; nav.toast(if (ru) "Сила застыла в кристалле" else "Its strength is frozen in crystal") }, Modifier.weight(1f), kind = Kind.SOFT)
                Btn(t("Отдать", "Give away"), { c.write { clerk.close(i, Fate.GIVEN, today) }; nav.toast(if (ru) "Отдано в хорошие руки" else "Given to good hands"); nav.close() }, Modifier.weight(1f), kind = Kind.SOFT)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Btn(t("Закончилось", "Used up"), { c.write { clerk.close(i, Fate.EATEN, today) }; nav.close() }, Modifier.weight(1f), kind = Kind.SOFT)
                Btn(t("Сдать в аптеку", "Return to a pharmacy"), { c.write { clerk.close(i, Fate.RETURNED, today) }; nav.toast(if (ru) "Просроченные лекарства принимают в аптеках с контейнером для сбора" else "Pharmacies with a collection box take expired medicines"); nav.close() }, Modifier.weight(1f), kind = Kind.SOFT)
            }
        }
        if (!med) Btn(t("Вылить", "Pour out"), { c.write { clerk.close(i, Fate.TOSSED, today) }; nav.toast(if (ru) "Вылито. В книге учёта это запомнят." else "Poured out. The ledger will remember."); nav.close() }, Modifier.fillMaxWidth(), kind = Kind.DANGER)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Btn(t("Изменить", "Edit"), { nav.go(Overlay.Add(edit = i)) }, Modifier.weight(1f), kind = Kind.GHOST, small = true)
            Btn(if (sure) t("Точно удалить?", "Really delete?") else t("Удалить запись", "Delete entry"), {
                if (!sure) sure = true else { c.write { clerk.delete(i) }; nav.close() }
            }, Modifier.weight(1f), kind = if (sure) Kind.DANGER else Kind.GHOST, small = true)
        }
        Text(t("«Удалить» стирает запись, будто её не было. «Вылить» оставляет след в книге учёта.", "“Delete” erases the entry as if it never was. “Pour out” leaves a mark in the ledger."), style = T.Small)
    }
}

@Composable
private fun Fact(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, style = T.Muted, modifier = Modifier.weight(1f))
        Text(value, style = T.Body)
    }
}

private fun fate(f: Fate, ru: Boolean) = when (f) {
    Fate.EATEN -> if (ru) "сварено" else "brewed"
    Fate.GIVEN -> if (ru) "отдано" else "given away"
    Fate.TOSSED -> if (ru) "вылито" else "poured out"
    Fate.RETURNED -> if (ru) "сдано в аптеку" else "returned"
}
