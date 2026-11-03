package app.apothecary.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.apothecary.container
import app.apothecary.data.Fate
import app.apothecary.data.Item
import app.apothecary.data.Order
import app.apothecary.domain.Glow
import app.apothecary.domain.Regulars
import app.apothecary.domain.Shop
import app.apothecary.domain.Words
import app.apothecary.ui.LocalRu
import app.apothecary.ui.Nav
import app.apothecary.ui.Overlay
import app.apothecary.ui.Tab
import app.apothecary.ui.art.ShopScene
import app.apothecary.ui.components.Btn
import app.apothecary.ui.components.Card
import app.apothecary.ui.components.IngredientRow
import app.apothecary.ui.components.Kind
import app.apothecary.ui.components.Section
import app.apothecary.ui.components.Tag
import app.apothecary.ui.components.t
import app.apothecary.ui.theme.C
import app.apothecary.ui.theme.R8
import app.apothecary.ui.theme.T

@Composable
fun ShopScreen(shop: Shop, hour: Int, nav: Nav) {
    val ru = LocalRu.current
    val murky = shop.urgent.filter { shop.glow(it) == Glow.MURKY }
    val urgent = shop.urgent.filter { shop.glow(it) == Glow.LAST || shop.glow(it) == Glow.SOON }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 96.dp)) {
        ShopScene(shop, hour, onFlask = { nav.go(Overlay.Ingredient(it.id)) }, onCauldron = { nav.go(Overlay.Brew()) })
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(summary(shop, ru), style = T.Italic, modifier = Modifier.padding(top = 12.dp))

            if (murky.isNotEmpty()) {
                Section(t("Помутнели", "Gone murky"))
                for (i in murky) MurkyRow(shop, i, nav)
            }

            val orders = if (shop.s.ordersDay == shop.today) shop.s.orders else emptyList()
            if (orders.isNotEmpty()) {
                Section(t("Заказы на сегодня", "Today's orders"), t("за заказ +3 монеты", "+3 coins an order"))
                val served = shop.served
                for ((k, o) in orders.withIndex()) OrderCard(shop, o, o.key() in served, k, nav)
            }

            if (urgent.isNotEmpty()) {
                Section(t("Теряют силу", "Losing strength"), t("все полки", "all shelves"), onNote = { nav.tab(Tab.SHELVES) })
                for (i in urgent) IngredientRow(shop, i, { nav.go(Overlay.Ingredient(i.id)) })
            }

            if (shop.today - shop.s.audit >= 7 && shop.stock.size >= 5) {
                Card(onClick = { nav.go(Overlay.Audit) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(t("Ночная ревизия", "Night audit"), style = T.Name)
                            Text(t("Раз в неделю пройдитесь по полкам со свечой: что на месте, а чего уже нет.", "Once a week, walk the shelves with a candle: what's still there and what's gone."), style = T.Muted)
                        }
                        Tag(t("пора", "due"), C.amber)
                    }
                }
            }

            if (shop.stock.isEmpty()) Card {
                Text(t("Полки пусты", "The shelves are empty"), style = T.Name)
                Text(t("Нажмите «+» и поставьте первый ингредиент: что лежит в холодильнике, в шкафу, в морозилке или в аптечке. Начните с того, что скоро испортится.",
                    "Tap “+” and put up the first ingredient: whatever is in the fridge, the cupboard, the freezer or the medicine box. Start with what spoils soonest."), style = T.Muted)
            } else Btn(t("К котлу", "To the cauldron"), { nav.go(Overlay.Brew()) }, Modifier.fillMaxWidth().padding(top = 6.dp))
        }
    }
}

/** A spoiled ingredient with the two honest answers right on the row. */
@Composable
private fun MurkyRow(shop: Shop, i: Item, nav: Nav) {
    val ru = LocalRu.current
    val c = LocalContext.current.container
    Column(Modifier.fillMaxWidth().clip(R8.Md).background(C.surface).border(1.dp, C.rose.copy(alpha = .35f), R8.Md)) {
        IngredientRow(shop, i, { nav.go(Overlay.Ingredient(i.id)) })
        Row(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Btn(t("Вылить", "Pour out"), { c.write { clerk.close(i, Fate.TOSSED, shop.today) }; nav.toast(if (ru) "Вылито" else "Poured out") }, Modifier.weight(1f), kind = Kind.DANGER, small = true)
            Btn(t("Всё-таки съели", "Ate it anyway"), { c.write { clerk.close(i, Fate.EATEN, shop.today) } }, Modifier.weight(1f), kind = Kind.SOFT, small = true)
        }
    }
}

/** A customer's note: who, what they need, which of your bottles fit, and the brew button on the note itself. */
@Composable
private fun OrderCard(shop: Shop, o: Order, done: Boolean, k: Int, nav: Nav) {
    val ru = LocalRu.current
    val edge = if (done) C.moss else C.copper
    val fit = shop.food.filter { it.cat in o.cats && (shop.left(it) ?: 99) in 0..2 }
    Column(Modifier.fillMaxWidth().clip(R8.Md).background(C.paper).border(2.dp, edge, R8.Md).padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(Regulars.name(o.who, ru).uppercase(), style = T.Eyebrow.copy(color = edge))
        if (done) Text(if (ru) "Спасибо! Зелье что надо." else "Thank you! Just what I needed.", style = T.Say)
        else {
            val need = Words.list(o.cats.map { Words.cat(it, ru) }, ru)
            Text(Regulars.line(o.who, ru, (shop.today + k).toInt()) + " " + (if (ru) "Сварите мне что-нибудь, где есть $need." else "Brew me something with $need."), style = T.Say)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Text(if (fit.isEmpty()) "" else (if (ru) "Подойдут: " else "Will do: ") + fit.joinToString(", ") { it.name.lowercase() }, style = T.Small.copy(color = C.paperMuted), modifier = Modifier.weight(1f).padding(end = 8.dp))
                if (fit.isNotEmpty()) Btn(t("Сварить", "Brew"), { nav.go(Overlay.Brew(fit.map { it.id })) }, kind = Kind.COPPER, small = true)
            }
        }
    }
}

/** One plain sentence about the shelves, naming what matters. */
private fun summary(shop: Shop, ru: Boolean): String {
    val food = shop.food
    if (food.isEmpty()) return if (ru) "В лавке тихо, полки ждут." else "The shop is quiet; the shelves are waiting."
    fun names(g: Glow) = food.filter { shop.glow(it) == g }.map { it.name.lowercase() }
    val murky = names(Glow.MURKY); val last = names(Glow.LAST)
    val tomorrow = food.filter { shop.left(it) == 1L }.map { it.name.lowercase() }
    val parts = buildList {
        if (last.isNotEmpty()) add((if (ru) "Сегодня последний день: " else "Last day today: ") + Words.list(last, ru) + ".")
        if (tomorrow.isNotEmpty()) add((if (ru) "Завтра: " else "Tomorrow: ") + Words.list(tomorrow.take(4), ru) + ".")
        if (murky.isNotEmpty()) add(if (ru) "Помутнело: ${Words.list(murky, true)}." else "Gone murky: ${Words.list(murky, false)}.")
    }
    return parts.joinToString(" ").replaceFirstChar { it.uppercase() }.ifEmpty {
        if (ru) "Все колбы светятся ровно. Можно спокойно варить." else "Every bottle glows steadily. Brew at leisure."
    }
}
