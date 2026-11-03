package app.apothecary.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import app.apothecary.ui.theme.R8
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.apothecary.container
import app.apothecary.data.Cat
import app.apothecary.data.Item
import app.apothecary.data.Need
import app.apothecary.data.Place
import app.apothecary.domain.Kind
import app.apothecary.domain.Lore
import app.apothecary.domain.Shop
import app.apothecary.domain.Words
import app.apothecary.ui.LocalRu
import app.apothecary.ui.Nav
import app.apothecary.ui.Overlay
import app.apothecary.ui.components.Btn
import app.apothecary.ui.components.Chip
import app.apothecary.ui.components.Field
import app.apothecary.ui.components.Flow
import app.apothecary.ui.components.Note
import app.apothecary.ui.components.Section
import app.apothecary.ui.components.Sheet
import app.apothecary.ui.components.t
import app.apothecary.ui.theme.C
import app.apothecary.ui.theme.T

/** Putting an ingredient on a shelf, or changing one that's there. */
@Composable
fun AddSheet(shop: Shop, edit: Item?, place0: Place?, nav: Nav) {
    val ru = LocalRu.current
    val c = LocalContext.current.container
    val today = shop.today
    var name by remember { mutableStateOf(edit?.name ?: "") }
    var cat by remember { mutableStateOf(edit?.cat ?: if (place0 == Place.CABINET) Cat.MEDS else Cat.OTHER) }
    var catSet by remember { mutableStateOf(edit != null) }
    var place by remember { mutableStateOf(edit?.place ?: place0 ?: Place.CELLAR) }
    var bought by remember { mutableStateOf(edit?.bought ?: today) }
    var printed by remember { mutableStateOf(edit?.printed ?: (today + 7)) }
    var noDate by remember { mutableStateOf(edit != null && edit.printed == null) }
    var dateSet by remember { mutableStateOf(edit != null) }
    var opened by remember { mutableStateOf(edit?.opened) }
    var openDays by remember { mutableStateOf(edit?.openDays?.toString() ?: "") }
    var price by remember { mutableStateOf(edit?.price?.takeIf { it > 0 }?.let { (it / 100).toString() } ?: "") }
    var need by remember { mutableStateOf(edit?.need) }
    var perDay by remember { mutableStateOf(edit?.perDay?.takeIf { it > 0 }?.toString() ?: "") }
    var count by remember { mutableStateOf(edit?.left?.takeIf { it > 0 }?.toString() ?: "") }
    var iceDays by remember { mutableStateOf(edit?.iceDays) }
    var jar by remember { mutableIntStateOf(edit?.jar ?: 0) }
    var picking by remember { mutableStateOf<String?>(null) }
    var kind by remember { mutableStateOf<Kind?>(null) }
    var details by remember { mutableStateOf(edit != null) }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    fun pick(k: Kind) {
        focusManager.clearFocus()
        kind = k
        name = k.name(ru)
        cat = k.cat; catSet = true
        place = k.place
        if (k.keep == null) { noDate = true } else { noDate = false; printed = bought + k.keep }
        dateSet = true
        openDays = k.open?.toString() ?: ""
        need = k.need
        iceDays = k.ice
    }

    LaunchedEffect(cat) { if (cat == Cat.LEFTOVER && jar == 0) jar = c.clerk.nextJar() }

    val save: @Composable () -> Unit = {
        Btn(if (edit == null) t("Поставить на полку", "Put on the shelf") else t("Сохранить", "Save"), {
            val n = name.trim()
            if (n.isEmpty()) { nav.toast(if (ru) "Как называется?" else "What is it called?"); return@Btn }
            val base = edit ?: Item(name = n, cat = cat, place = place, bought = bought)
            val item = base.copy(
                name = n, cat = cat, place = place, bought = bought,
                printed = if (noDate) null else printed,
                opened = if (place == Place.ICE) null else opened,
                openDays = openDays.toIntOrNull(),
                // Bought frozen (dumplings, ice cream) keeps its printed date; anything else put in the chest starts the ice clock.
                frozen = if (place == Place.ICE) (base.frozen ?: if (Lore.exact(n)?.place == Place.ICE) null else today) else null,
                iceDays = if (place == Place.ICE) (iceDays ?: Lore.exact(n)?.ice) else base.iceDays,
                price = (price.toIntOrNull() ?: 0) * 100,
                jar = if (cat == Cat.LEFTOVER) jar else 0,
                need = if (cat == Cat.MEDS || cat == Cat.VITAMIN || place == Place.CABINET) need else null,
                perDay = perDay.toIntOrNull() ?: 0,
                left = count.toIntOrNull() ?: 0,
                courseFrom = if (perDay.toIntOrNull() != null && count.toIntOrNull() != null) (if (edit?.left == count.toIntOrNull() && edit?.perDay == perDay.toIntOrNull()) edit?.courseFrom ?: today else today) else null,
            )
            c.write { if (edit == null) clerk.add(item) else clerk.save(item) }
            nav.toast(if (edit == null) (if (ru) "«$n» на полке" else "“$n” is on the shelf") else (if (ru) "Сохранено" else "Saved"))
            if (edit != null) nav.go(Overlay.Ingredient(edit.id)) else nav.close()
        }, Modifier.fillMaxWidth())
    }
    Sheet(onDismiss = nav::close, bottom = save) {
        Text(if (edit == null) t("На полку", "Onto the shelf") else t("Изменить", "Edit"), style = T.Title)
        val focus = remember { FocusRequester() }
        LaunchedEffect(Unit) { if (edit == null) runCatching { focus.requestFocus() } }
        Field(name, { name = it; kind = null }, t("Что это? Молоко, гречка, ибупрофен…", "What is it? Milk, rice, ibuprofen…"), Modifier.focusRequester(focus))
        val found = remember(name) { if (kind == null) Lore.find(name, ru) else emptyList() }
        if (found.isNotEmpty()) Flow { for (k in found) Chip(k.name(ru), false, { pick(k) }) }

        if (kind != null && !details) {
            // Picked from the guide: everything is filled in, so show it in one line and keep the chips out of the way.
            Row(Modifier.fillMaxWidth().clip(R8.Md).background(C.surface2).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(listOfNotNull(Words.cat(cat, ru), Words.place(place, ru).lowercase(),
                    openDays.toIntOrNull()?.let { if (ru) "открытым ${Words.days(it.toLong(), true)}" else "${Words.days(it.toLong(), false)} open" }).joinToString(" · "),
                    style = T.Body, modifier = Modifier.weight(1f))
                Chip(t("изменить", "change"), false, { details = true })
            }
        }
        if (kind == null || details) {
        Section(t("Эссенция", "Essence"))
        Flow {
            for (x in Cat.entries) Chip(Words.cat(x, ru), x == cat, {
                cat = x; catSet = true
                if (kind == null) {
                    place = if (edit == null || place0 == null) Lore.place(x) else place
                    if (!dateSet) printed = bought + Lore.keep(x)
                }
            }, color = C.cat(x))
        }

        Section(t("Где хранится", "Kept in"))
        Flow { for (p in Place.entries) Chip(Words.place(p, ru), p == place, { place = p }) }
        }

        Section(t("Срок на упаковке", "Date on the package"), if (noDate) t("без срока", "none") else Words.date(printed, ru, year = printed - today > 300))
        Flow {
            for (d in listOf(1, 3, 7, 14, 30, 90, 365)) {
                val label = when (d) { 365 -> t("+год", "+1y"); 90 -> t("+3 мес", "+3mo"); 30 -> t("+месяц", "+1mo"); else -> "+$d" }
                Chip(label, !noDate && printed == bought + d, { printed = bought + d; noDate = false; dateSet = true })
            }
            Chip(t("дата…", "date…"), false, { picking = "printed" })
            Chip(t("без срока", "none"), noDate, { noDate = !noDate; dateSet = true })
        }

        Section(t("Куплено", "Bought"), Words.date(bought, ru))
        Flow {
            Chip(t("сегодня", "today"), bought == today, { val shift = today - bought; bought = today; if (!noDate && !dateSet) printed += shift })
            Chip(t("вчера", "yesterday"), bought == today - 1, { bought = today - 1 })
            Chip(t("другой день…", "another day…"), bought < today - 1, { picking = "bought" })
        }

        // Opening only matters for things that keep a shorter time once open (milk, jars); chicken doesn't ask.
        if (place != Place.ICE && (kind == null || kind?.open != null || details || opened != null)) {
            Section(t("Вскрыто", "Opened"), opened?.let { Words.date(it, ru) })
            Flow {
                Chip(t("запечатано", "sealed"), opened == null, { opened = null })
                Chip(t("сегодня", "today"), opened == today, { opened = today })
                Chip(t("вчера", "yesterday"), opened == today - 1, { opened = today - 1 })
                Chip(t("раньше…", "earlier…"), opened != null && opened!! < today - 1, { picking = "opened" })
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(t("Открытым годно, дней:", "Keeps once open, days:"), style = T.Muted, modifier = Modifier.weight(1f))
                Field(openDays, { s -> openDays = s.filter { it.isDigit() }.take(4) }, "—", Modifier.width(90.dp), number = true)
            }
        }

        if (cat == Cat.MEDS || cat == Cat.VITAMIN || place == Place.CABINET) {
            Section(t("Для набора гильдии", "For the guild's kit"))
            Flow {
                Chip(t("ничего", "none"), need == null, { need = null })
                for (n in Need.entries) Chip(Words.need(n, ru), n == need, { need = n })
            }
        }
        if (cat == Cat.VITAMIN) {
            Section(t("Курс", "Course"))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Field(perDay, { s -> perDay = s.filter { it.isDigit() }.take(2) }, t("в день", "a day"), Modifier.weight(1f), number = true)
                Field(count, { s -> count = s.filter { it.isDigit() }.take(4) }, t("штук в банке", "in the jar"), Modifier.weight(1f), number = true)
            }
        }

        Section(t("Цена, по желанию", "Price, optional"))
        Field(price, { s -> price = s.filter { it.isDigit() }.take(6) }, t("чтобы считать, сколько ушло в помои", "to count what went down the drain"), number = true)

        if (cat == Cat.LEFTOVER && jar > 0) Note(
            if (ru) "Напишите на крышке контейнера №$jar, хоть маркером на полоске малярного скотча. Тогда не придётся гадать, какой это суп." else "Write №$jar on the container lid, even in marker on a strip of masking tape. No more guessing which soup it is.",
            t("Склянка", "Jar"),
        )

    }

    picking?.let { what ->
        val init = when (what) { "printed" -> printed; "bought" -> bought; else -> opened ?: today }
        DateDialog(init, onDone = { d ->
            when (what) {
                "printed" -> { printed = d; noDate = false; dateSet = true }
                "bought" -> bought = d
                else -> opened = d
            }
            picking = null
        }, onCancel = { picking = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateDialog(day: Long, onDone: (Long) -> Unit, onCancel: () -> Unit) {
    val state = rememberDatePickerState(initialSelectedDateMillis = day * 86_400_000L)
    DatePickerDialog(
        onDismissRequest = onCancel,
        confirmButton = { TextButton({ state.selectedDateMillis?.let { onDone(it / 86_400_000L) } ?: onCancel() }) { Text("OK", style = T.Button.copy(color = C.amber)) } },
        dismissButton = { TextButton(onCancel) { Text(t("Отмена", "Cancel"), style = T.Button.copy(color = C.muted)) } },
    ) { DatePicker(state) }
}
