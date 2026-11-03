package app.apothecary.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.apothecary.container
import app.apothecary.data.Brewed
import app.apothecary.data.Item
import app.apothecary.data.Place
import app.apothecary.data.Settings
import app.apothecary.domain.Shop
import app.apothecary.domain.Words
import app.apothecary.ru
import app.apothecary.ui.book.BookScreen
import app.apothecary.ui.calendar.CalendarScreen
import app.apothecary.ui.components.t
import app.apothecary.ui.onboarding.Onboarding
import app.apothecary.ui.settings.SettingsPage
import app.apothecary.ui.sheets.AddSheet
import app.apothecary.ui.sheets.AuditSheet
import app.apothecary.ui.sheets.BrewSheet
import app.apothecary.ui.sheets.DecorSheet
import app.apothecary.ui.sheets.ItemSheet
import app.apothecary.ui.sheets.RevealSheet
import app.apothecary.ui.shelves.ShelvesScreen
import app.apothecary.ui.shop.ShopScreen
import app.apothecary.ui.theme.C
import app.apothecary.ui.theme.T
import kotlinx.coroutines.delay
import java.util.Calendar

val LocalRu = staticCompositionLocalOf { true }

enum class Tab { SHOP, SHELVES, CALENDAR, BOOK }

/** What is open over the tabs. */
sealed class Overlay {
    /** A new ingredient, or an existing one being edited; [place] preselects the shelf. */
    data class Add(val edit: Item? = null, val place: Place? = null) : Overlay()
    data class Ingredient(val id: Long) : Overlay()
    data class Brew(val chosen: List<Long> = emptyList()) : Overlay()
    data class Reveal(val b: Brewed) : Overlay()
    data object Decor : Overlay()
    data object Audit : Overlay()
    data object Settings : Overlay()
}

/** Ways screens move around and talk to each other. */
@Stable
class Nav(val go: (Overlay?) -> Unit, val tab: (Tab) -> Unit) {
    fun close() = go(null)
    var toast by mutableStateOf("" to 0L)
        private set
    fun toast(t: String) { toast = t to System.nanoTime() }
}

val LocalNav = staticCompositionLocalOf<Nav> { error("no nav") }

/** [clock], [tab0] and [overlay0] let renders open a fixed moment on a given screen; the app passes none. */
@Composable
fun ShopRoot(s: Settings, clock: Long? = null, tab0: Tab = Tab.SHOP, overlay0: Overlay? = null) {
    val ctx = LocalContext.current
    val c = ctx.container
    val ru = ctx.ru
    CompositionLocalProvider(LocalRu provides ru) {
        if (!s.onboarded) { Onboarding(); return@CompositionLocalProvider }
        val items by c.db.dao().items().collectAsStateWithLifecycle(initialValue = null)
        val brews by c.db.dao().brews().collectAsStateWithLifecycle(initialValue = null)
        var now by remember { mutableLongStateOf(clock ?: System.currentTimeMillis()) }
        LaunchedEffect(Unit) { if (clock == null) while (true) { delay(60_000); now = System.currentTimeMillis() } }
        val today = Shop.epoch(now)
        LaunchedEffect(today, items == null) { if (items != null) c.clerk.refreshOrders(today) }
        val shop = remember(items, brews, s, today) { if (items == null || brews == null) null else Shop(items!!, brews!!, s, today) } ?: return@CompositionLocalProvider
        val hour = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.HOUR_OF_DAY)
        var tab by rememberSaveable { mutableStateOf(tab0) }
        var overlay by remember { mutableStateOf(overlay0) }
        val nav = remember { Nav({ overlay = it }, { tab = it; overlay = null }) }

        CompositionLocalProvider(LocalNav provides nav) {
            Box(Modifier.fillMaxSize().background(C.bg)) {
                Column(Modifier.fillMaxSize()) {
                    Header(shop, onCoins = { overlay = Overlay.Decor }, onRank = { tab = Tab.BOOK }, onSettings = { overlay = Overlay.Settings })
                    Box(Modifier.weight(1f)) {
                        when (tab) {
                            Tab.SHOP -> ShopScreen(shop, hour, nav)
                            Tab.SHELVES -> ShelvesScreen(shop, nav)
                            Tab.CALENDAR -> CalendarScreen(shop, nav)
                            Tab.BOOK -> BookScreen(shop, nav)
                        }
                        if (tab == Tab.SHOP || tab == Tab.SHELVES) AddButton(Modifier.align(Alignment.BottomEnd).padding(18.dp)) { overlay = Overlay.Add() }
                    }
                    TabBar(tab) { tab = it }
                }
                when (val o = overlay) {
                    is Overlay.Add -> AddSheet(shop, o.edit, o.place, nav)
                    is Overlay.Ingredient -> shop.item(o.id)?.let { ItemSheet(shop, it, nav) }
                    is Overlay.Brew -> BrewSheet(shop, o.chosen, nav)
                    is Overlay.Reveal -> RevealSheet(shop, o.b, nav)
                    Overlay.Decor -> DecorSheet(shop, nav)
                    Overlay.Audit -> AuditSheet(shop, nav)
                    Overlay.Settings -> SettingsPage(shop, nav)
                    null -> Unit
                }
                Toast(nav, Modifier.align(Alignment.TopCenter))
            }
        }
    }
}

@Composable
private fun Header(shop: Shop, onCoins: () -> Unit, onRank: () -> Unit, onSettings: () -> Unit) {
    val ru = LocalRu.current
    Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(1f).clickable(onClick = onRank)) {
            val name = shop.s.shop.ifBlank { if (ru) "Лавка зельевара" else "The Apothecary" }
            Text(name, style = T.Title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(Words.rank(shop.rank, ru), style = T.Italic.copy(fontSize = T.Italic.fontSize * .85f))
        }
        Row(Modifier.clip(CircleShape).background(C.amberSoft).border(1.dp, C.amber.copy(alpha = .4f), CircleShape).clickable(onClick = onCoins).padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Canvas(Modifier.size(14.dp)) {
                drawCircle(C.amber, size.width / 2)
                drawCircle(C.onAmber.copy(alpha = .35f), size.width / 2 * .62f, style = Stroke(1.5f))
            }
            Text("${shop.coins}", style = T.Strong.copy(color = C.amber))
        }
        val label = t("Настройки", "Settings")
        Box(Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onSettings).semantics { contentDescription = label }, contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(20.dp)) {
                val r = size.width / 2; val w = 2.dp.toPx()
                for (i in 0 until 8) {
                    val a = i * Math.PI / 4
                    drawLine(C.muted, Offset(r + (r * .55f) * kotlin.math.cos(a).toFloat(), r + (r * .55f) * kotlin.math.sin(a).toFloat()),
                        Offset(r + r * kotlin.math.cos(a).toFloat(), r + r * kotlin.math.sin(a).toFloat()), w * 1.4f)
                }
                drawCircle(C.muted, r * .62f, style = Stroke(w))
                drawCircle(C.muted, r * .22f, style = Stroke(w))
            }
        }
    }
}

@Composable
private fun AddButton(modifier: Modifier, onClick: () -> Unit) {
    val label = t("Добавить", "Add")
    Box(modifier.size(60.dp).shadow(10.dp, CircleShape).clip(CircleShape).background(C.amber).clickable(onClick = onClick).semantics { contentDescription = label }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(22.dp)) {
            val w = 3.dp.toPx()
            drawLine(C.onAmber, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), w, StrokeCap.Round)
            drawLine(C.onAmber, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), w, StrokeCap.Round)
        }
    }
}

@Composable
private fun TabBar(tab: Tab, onTab: (Tab) -> Unit) {
    Row(Modifier.fillMaxWidth().background(C.surface).navigationBarsPadding().padding(horizontal = 10.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for ((tb, l) in listOf(Tab.SHOP to ("Лавка" to "Shop"), Tab.SHELVES to ("Полки" to "Shelves"), Tab.CALENDAR to ("Календарь" to "Calendar"), Tab.BOOK to ("Книга" to "Book"))) {
            val on = tb == tab
            Text(t(l.first, l.second), style = T.Button.copy(color = if (on) C.onAmber else C.muted, fontSize = T.Button.fontSize * .92f), textAlign = TextAlign.Center, maxLines = 1,
                modifier = Modifier.weight(1f).clip(CircleShape).background(if (on) C.amber else C.surface).clickable { onTab(tb) }.padding(vertical = 10.dp))
        }
    }
}

@Composable
private fun Toast(nav: Nav, modifier: Modifier) {
    val (text, stamp) = nav.toast
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(stamp) { if (text.isNotEmpty()) { shown = true; delay(3200); shown = false } }
    if (!shown || text.isEmpty()) return
    Text(text, style = T.Body.copy(color = C.onAmber), modifier = modifier.statusBarsPadding().padding(top = 64.dp, start = 16.dp, end = 16.dp).fillMaxWidth()
        .clip(RoundedCornerShape(16.dp)).background(C.amber).padding(horizontal = 16.dp, vertical = 12.dp))
}
