package app.apothecary.ui.shop

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
import androidx.compose.ui.unit.dp
import app.apothecary.domain.Glow
import app.apothecary.domain.Regulars
import app.apothecary.domain.Shop
import app.apothecary.domain.Words
import app.apothecary.ui.LocalRu
import app.apothecary.ui.Nav
import app.apothecary.ui.Overlay
import app.apothecary.ui.art.ShopScene
import app.apothecary.ui.components.Btn
import app.apothecary.ui.components.Card
import app.apothecary.ui.components.IngredientRow
import app.apothecary.ui.components.Kind
import app.apothecary.ui.components.Note
import app.apothecary.ui.components.Section
import app.apothecary.ui.components.Tag
import app.apothecary.ui.components.t
import app.apothecary.ui.theme.C
import app.apothecary.ui.theme.T

@Composable
fun ShopScreen(shop: Shop, hour: Int, nav: Nav) {
    val ru = LocalRu.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 90.dp)) {
        ShopScene(shop, hour, onFlask = { nav.go(Overlay.Ingredient(it.id)) }, onCauldron = { nav.go(Overlay.Brew()) })
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val last = shop.food.filter { shop.glow(it) == Glow.LAST }
            val soon = shop.food.filter { shop.glow(it) == Glow.SOON }
            val murky = shop.food.filter { shop.glow(it) == Glow.MURKY }
            Text(summary(shop.food.size, last.size, soon.size, murky.size, ru), style = T.Italic, modifier = Modifier.padding(top = 12.dp))

            val orders = if (shop.s.ordersDay == shop.today) shop.s.orders else emptyList()
            if (orders.isNotEmpty()) {
                Section(t("Заказы на сегодня", "Today's orders"))
                val served = shop.served
                for ((k, o) in orders.withIndex()) {
                    val done = o.key() in served
                    val need = Words.list(o.cats.map { Words.cat(it, ru) }, ru)
                    val line = Regulars.line(o.who, ru, (shop.today + k).toInt())
                    Note(
                        if (done) (if (ru) "Спасибо! Зелье что надо." else "Thank you! Just what I needed.")
                        else "$line " + (if (ru) "Сварите мне что-нибудь, где есть $need." else "Brew me something with $need."),
                        Regulars.name(o.who, ru), edge = if (done) C.moss else C.copper,
                    )
                    if (!done) {
                        val ids = shop.food.filter { it.cat in o.cats && (shop.left(it) ?: 99) in 0..2 }.map { it.id }
                        Btn(t("Сварить для заказа", "Brew for the order"), { nav.go(Overlay.Brew(ids)) }, Modifier.fillMaxWidth(), kind = Kind.SOFT, small = true)
                    }
                }
            }

            val urgent = shop.urgent.filter { shop.glow(it) != Glow.BRIGHT }
            if (urgent.isNotEmpty()) {
                Section(t("Теряют силу", "Losing strength"), t("на полки", "all shelves"), onNote = { nav.tab(app.apothecary.ui.Tab.SHELVES) })
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

            Btn(t("К котлу", "To the cauldron"), { nav.go(Overlay.Brew()) }, Modifier.fillMaxWidth().padding(top = 6.dp))
            if (shop.stock.isEmpty()) Text(t("Полки пусты. Нажмите «+», чтобы поставить первый ингредиент: что лежит в холодильнике, в шкафу, в морозилке или в аптечке.",
                "The shelves are empty. Tap “+” to put up the first ingredient: whatever is in the fridge, the cupboard, the freezer or the medicine box."), style = T.Muted)
        }
    }
}

private fun summary(total: Int, last: Int, soon: Int, murky: Int, ru: Boolean): String = when {
    total == 0 -> if (ru) "В лавке тихо, полки ждут." else "The shop is quiet; the shelves are waiting."
    murky > 0 -> if (ru) "На полках есть помутневшие колбы: ${murky}. Загляните, пока не поздно для остальных." else "There are murky bottles on the shelves: $murky. Check on the rest before it's too late."
    last > 0 -> if (ru) "Сегодня последний день у $last ${Words.plural(last.toLong(), "ингредиента", "ингредиентов", "ингредиентов")}. Котёл ждёт." else "$last ${if (last == 1) "ingredient is" else "ingredients are"} on their last day. The cauldron is waiting."
    soon > 0 -> if (ru) "Скоро ослабнут: $soon. Самое время подумать об ужине." else "Weakening soon: $soon. A good time to think about supper."
    else -> if (ru) "Все колбы светятся ровно. Можно спокойно варить." else "Every bottle glows steadily. Brew at leisure."
}
