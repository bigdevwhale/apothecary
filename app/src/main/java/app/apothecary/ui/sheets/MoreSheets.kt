package app.apothecary.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.apothecary.container
import app.apothecary.data.Fate
import app.apothecary.data.Place
import app.apothecary.domain.Decor
import app.apothecary.domain.Shop
import app.apothecary.domain.Words
import app.apothecary.ui.LocalRu
import app.apothecary.ui.Nav
import app.apothecary.ui.components.Btn
import app.apothecary.ui.components.Field
import app.apothecary.ui.components.IngredientRow
import app.apothecary.ui.components.Kind
import app.apothecary.ui.components.Section
import app.apothecary.ui.components.Sheet
import app.apothecary.ui.components.Tag
import app.apothecary.ui.components.t
import app.apothecary.ui.theme.C
import app.apothecary.ui.theme.R8
import app.apothecary.ui.theme.T
import kotlinx.coroutines.launch

/** The coins go into the shop: candles, a cat, a copper cauldron. */
@Composable
fun DecorSheet(shop: Shop, nav: Nav) {
    val ru = LocalRu.current
    val c = LocalContext.current.container
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(shop.s.shop) }
    Sheet(onDismiss = nav::close) {
        Text(t("Обустройство лавки", "Fitting out the shop"), style = T.Title)
        Text(if (ru) "В кошельке ${shop.coins} ${Words.plural(shop.coins.toLong(), "монета", "монеты", "монет")}. Монеты приносят зелья: по одной за ингредиент, две за спасённый в последний день, три за выполненный заказ."
            else "${shop.coins} coins in the purse. Potions bring coins: one per ingredient, two for one saved on its last day, three for an order served.", style = T.Muted)
        for (d in Decor.entries) {
            val has = d.name in shop.s.bought
            Row(Modifier.fillMaxWidth().clip(R8.Md).background(if (has) C.amberSoft else C.surface2).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(d.name(ru), style = T.Body, modifier = Modifier.weight(1f))
                if (has) Tag(t("есть", "owned"), C.moss)
                else Btn("${d.price}", {
                    scope.launch { if (c.clerk.buy(d)) nav.toast(if (ru) "Теперь в лавке: ${d.name(true).lowercase()}" else "New in the shop: ${d.name(false).lowercase()}") else nav.toast(if (ru) "Не хватает монет" else "Not enough coins") }
                }, kind = if (shop.coins >= d.price) Kind.AMBER else Kind.SOFT, small = true)
            }
        }
        if (Decor.SIGN.name in shop.s.bought) {
            Section(t("Название на вывеске", "The name on the sign"))
            Field(name, { name = it.take(28) }, t("Например, «Зелёная склянка»", "For example, The Green Phial"))
            Btn(t("Повесить вывеску", "Hang the sign"), { c.write { settings.update { it.copy(shop = name.trim()) } }; nav.toast(if (ru) "Вывеска на месте" else "The sign is up") }, Modifier.fillMaxWidth(), kind = Kind.SOFT)
        }
    }
}

private enum class Seen { HERE, EATEN, TOSSED }

/** The night audit: walk the shelves and mark what's no longer there. */
@Composable
fun AuditSheet(shop: Shop, nav: Nav) {
    val ru = LocalRu.current
    val c = LocalContext.current.container
    var marks by remember { mutableStateOf(mapOf<Long, Seen>()) }
    Sheet(onDismiss = nav::close, bottom = {
        Btn(t("Ревизия окончена", "Audit done"), {
            val today = shop.today
            val done = marks.filterValues { it != Seen.HERE }
            c.write {
                for ((id, m) in done) shop.item(id)?.let { clerk.close(it, if (m == Seen.EATEN) Fate.EATEN else Fate.TOSSED, today) }
                settings.update { it.copy(audit = today) }
            }
            nav.toast(if (done.isEmpty()) (if (ru) "Всё на месте" else "Everything's in place") else (if (ru) "Снято с полок: ${done.size}" else "Taken off the shelves: ${done.size}"))
            nav.close()
        }, Modifier.fillMaxWidth())
    }) {
        Text(t("Ночная ревизия", "Night audit"), style = T.Title)
        Text(t("Нажмите на то, чего уже нет: один раз — съели, второй — вылили, третий — снова на месте.", "Tap what's gone: once for eaten, twice for poured out, a third time to put it back."), style = T.Muted)
        for (p in Place.entries) {
            val list = shop.on(p)
            if (list.isEmpty()) continue
            Section(Words.place(p, ru))
            for (i in list) {
                val m = marks[i.id] ?: Seen.HERE
                Column {
                    IngredientRow(shop, i, {
                        marks = marks + (i.id to Seen.entries[(m.ordinal + 1) % 3])
                    })
                    if (m != Seen.HERE) Text(if (m == Seen.EATEN) t("  ↳ съели", "  ↳ eaten") else t("  ↳ вылили", "  ↳ poured out"), style = T.Small.copy(color = if (m == Seen.EATEN) C.moss else C.rose))
                }
            }
        }
    }
}
