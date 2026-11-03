package app.apothecary.ui.sheets

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.apothecary.container
import app.apothecary.data.Brewed
import app.apothecary.domain.Book
import app.apothecary.domain.Glow
import app.apothecary.domain.Regulars
import app.apothecary.domain.Shop
import app.apothecary.domain.Words
import app.apothecary.ui.LocalRu
import app.apothecary.ui.Nav
import app.apothecary.ui.Overlay
import app.apothecary.ui.art.Art
import app.apothecary.ui.art.Flask
import app.apothecary.ui.components.Btn
import app.apothecary.ui.components.Card
import app.apothecary.ui.components.IngredientRow
import app.apothecary.ui.components.Note
import app.apothecary.ui.components.Section
import app.apothecary.ui.components.Sheet
import app.apothecary.ui.components.Tag
import app.apothecary.ui.components.t
import app.apothecary.ui.theme.C
import app.apothecary.ui.theme.T
import kotlinx.coroutines.launch

/**
 * The cauldron: tick what went into the meal and see which potion it makes. Ingredients on their last days
 * come first, and the recipe is named before you brew, so it's easy to aim for a new one.
 */
@Composable
fun BrewSheet(shop: Shop, chosen0: List<Long>, nav: Nav) {
    val ru = LocalRu.current
    val c = LocalContext.current.container
    val scope = rememberCoroutineScope()
    var chosen by remember { mutableStateOf(chosen0.toSet()) }
    val food = shop.urgent
    Sheet(onDismiss = nav::close) {
        Text(t("Котёл", "The cauldron"), style = T.Title)
        Text(t("Отметьте, что пошло в еду. Всё отмеченное уйдёт с полок как сваренное.", "Tick what went into the meal. Everything ticked leaves the shelves as brewed."), style = T.Muted)
        val used = food.filter { it.id in chosen }
        if (used.isNotEmpty()) {
            val rescued = used.count { (shop.left(it) ?: 99) in 0..1 }
            val p = Book.brew(used.map { it.cat }, rescued)
            val col = Color(Shop.blend(used.map { it.cat }))
            Card {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Flask(col, .9f, Glow.BRIGHT, 64.dp, p.shape, p.key.hashCode())
                    androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                        Text(t("Выйдет", "It will make"), style = T.Eyebrow)
                        Text(p.name(ru), style = T.Name.copy(color = col))
                        if (p.key !in shop.known) Tag(t("новый рецепт", "new recipe"), C.amber)
                    }
                }
            }
        }
        if (food.isEmpty()) Text(t("На полках нет еды. Сначала поставьте что-нибудь.", "There's no food on the shelves. Put something up first."), style = T.Muted)
        Section(t("Ингредиенты", "Ingredients"), if (chosen.isEmpty()) null else if (ru) "выбрано ${chosen.size}" else "${chosen.size} chosen")
        for (i in food) IngredientRow(shop, i, { chosen = if (i.id in chosen) chosen - i.id else chosen + i.id }, selected = i.id in chosen)
        Btn(t("Сварить", "Brew"), {
            scope.launch {
                val b = c.clerk.brew(chosen.toList(), shop.today) ?: return@launch
                nav.go(Overlay.Reveal(b))
            }
        }, Modifier.fillMaxWidth().padding(top = 4.dp), enabled = chosen.isNotEmpty())
    }
}

/** The potion rising out of the pot: its name, the coins, and who it served. */
@Composable
fun RevealSheet(shop: Shop, b: Brewed, nav: Nav) {
    val ru = LocalRu.current
    val grow = remember { Animatable(if (Art.still) 1f else .2f) }
    LaunchedEffect(Unit) { grow.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
    val col = Color(b.brew.color)
    Sheet(onDismiss = nav::close) {
        Box(Modifier.fillMaxWidth().height(190.dp), contentAlignment = Alignment.Center) {
            Flask(col, .92f, Glow.BRIGHT, 170.dp, b.potion.shape, b.brew.id.toInt(), Modifier.scale(grow.value))
        }
        Text(if (b.first) t("Новый рецепт в книге!", "A new recipe in the book!") else t("Сварено", "Brewed"), style = T.Eyebrow.copy(color = C.amber), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(b.potion.name(ru), style = T.Display.copy(color = col), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(b.brew.names.lines().joinToString(", "), style = T.Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) { Tag("+${b.brew.coins} " + (if (ru) Words.plural(b.brew.coins.toLong(), "монета", "монеты", "монет") else "coins"), C.amber) }
        if (b.brew.rescued > 0) Text(if (ru) "Спасено в последний момент: ${b.brew.rescued}" else "Saved at the last moment: ${b.brew.rescued}", style = T.Body.copy(color = C.moss), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        for (o in b.served) Note(Regulars.line(o.who, ru, b.brew.id.toInt()) + " " + (if (ru) "Спасибо, это то, что нужно!" else "Thank you, just what I needed!"), Regulars.name(o.who, ru), edge = C.moss)
        Btn(t("Поставить на полку", "Put it on the shelf"), nav::close, Modifier.fillMaxWidth())
    }
}
