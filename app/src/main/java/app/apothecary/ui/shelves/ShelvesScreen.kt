package app.apothecary.ui.shelves

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.apothecary.data.Place
import app.apothecary.domain.Shop
import app.apothecary.domain.Words
import app.apothecary.ui.LocalRu
import app.apothecary.ui.Nav
import app.apothecary.ui.Overlay
import app.apothecary.ui.components.Bar
import app.apothecary.ui.components.Card
import app.apothecary.ui.components.Chip
import app.apothecary.ui.components.Flow
import app.apothecary.ui.components.IngredientRow
import app.apothecary.ui.components.Section
import app.apothecary.ui.components.t
import app.apothecary.ui.theme.C
import app.apothecary.ui.theme.T

@Composable
fun ShelvesScreen(shop: Shop, nav: Nav) {
    val ru = LocalRu.current
    var place by rememberSaveable { mutableStateOf(Place.CELLAR) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PlaceTabs(shop, place) { place = it }
        if (place == Place.CABINET) Cabinet(shop)
        val list = shop.on(place)
        if (place == Place.CABINET) {
            // Medicines kept cold still belong to the cabinet; show them here too, marked by where they are.
            val cold = shop.cabinet.filter { it.place != Place.CABINET }
            if (list.isNotEmpty() || cold.isNotEmpty()) Section(t("В шкафу", "In the cabinet"))
            for (i in list + cold) IngredientRow(shop, i, { nav.go(Overlay.Ingredient(i.id)) })
        } else for (i in list.filter { it.cat.food }) IngredientRow(shop, i, { nav.go(Overlay.Ingredient(i.id)) })
        if (list.isEmpty()) Text(empty(place, ru), style = T.Muted, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun Cabinet(shop: Shop) {
    val ru = LocalRu.current
    val (have, all) = shop.ready
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(t("Набор гильдии", "The guild's kit"), style = T.Name)
                Text(if (have == all) t("Всё на месте и годно.", "All there and good.") else if (ru) "Готово $have из $all" else "$have of $all ready", style = T.Muted)
            }
            Text("$have/$all", style = T.Number.copy(color = if (have == all) C.moss else C.amber))
        }
        Bar(if (all == 0) 1f else have.toFloat() / all, Modifier.fillMaxWidth().padding(vertical = 8.dp), if (have == all) C.moss else C.amber)
        Flow {
            for (n in shop.s.needs.sortedBy { it.ordinal }) {
                val ok = shop.needMet(n)
                Text((if (ok) "✓ " else "○ ") + Words.need(n, ru), style = T.Small.copy(color = if (ok) C.moss else C.muted))
            }
        }
    }
}

private fun empty(p: Place, ru: Boolean) = when (p) {
    Place.CELLAR -> if (ru) "В погребке пусто. Молоко, сыр, зелень, вчерашний суп: всё, что живёт в холодильнике, ставится сюда." else "The cellar is empty. Milk, cheese, herbs, yesterday's soup: whatever lives in the fridge goes here."
    Place.PANTRY -> if (ru) "Кладовая пуста. Крупы, макароны, хлеб, банки и соусы." else "The pantry is empty. Grains, pasta, bread, jars and sauces."
    Place.ICE -> if (ru) "В ларе ничего нет. Положите сюда то, что не успеваете съесть: срок замрёт." else "Nothing in the ice chest. Put here what you can't eat in time: its clock freezes."
    Place.CABINET -> if (ru) "Шкаф пуст. Добавьте лекарства и витамины: гильдия проверит, всё ли на месте." else "The cabinet is empty. Add medicines and vitamins: the guild will check it's all there."
}

/** Four doors of the shop: the name, how many bottles, and what it is in an ordinary kitchen. */
@Composable
private fun PlaceTabs(shop: Shop, place: Place, onPick: (Place) -> Unit) {
    val ru = LocalRu.current
    Row(Modifier.fillMaxWidth().clip(app.apothecary.ui.theme.R8.Lg).background(C.surface).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (p in Place.entries) {
            val on = p == place
            val n = if (p == Place.CABINET) shop.cabinet.size else shop.on(p).count { it.cat.food }
            val alarm = (if (p == Place.CABINET) shop.cabinet else shop.on(p).filter { it.cat.food }).any { shop.glow(it) != app.apothecary.domain.Glow.BRIGHT }
            Column(
                Modifier.weight(1f).clip(app.apothecary.ui.theme.R8.Md).background(if (on) C.amber else C.surface).clickable { onPick(p) }.padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(short(p, ru), style = T.Strong.copy(color = if (on) C.onAmber else C.ink), maxLines = 1)
                    if (alarm) Box(Modifier.padding(start = 4.dp).size(6.dp).clip(CircleShape).background(if (on) C.onAmber else C.copper))
                }
                Text("${Words.placeHint(p, ru)} · $n", style = T.Small.copy(color = if (on) C.onAmber.copy(alpha = .7f) else C.faint, fontSize = T.Small.fontSize * .92f), maxLines = 1)
            }
        }
    }
}

private fun short(p: Place, ru: Boolean) = when (p) {
    Place.CELLAR -> if (ru) "Погребок" else "Cellar"
    Place.PANTRY -> if (ru) "Кладовая" else "Pantry"
    Place.ICE -> if (ru) "Ларь" else "Ice chest"
    Place.CABINET -> if (ru) "Шкаф" else "Cabinet"
}
