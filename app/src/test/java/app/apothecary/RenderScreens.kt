package app.apothecary

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import app.apothecary.data.DemoSeed
import app.apothecary.data.Settings
import app.apothecary.domain.Shop
import app.apothecary.ui.LocalRu
import app.apothecary.ui.Overlay
import app.apothecary.ui.ShopRoot
import app.apothecary.ui.Tab
import app.apothecary.ui.art.Art
import app.apothecary.ui.onboarding.Onboarding
import app.apothecary.ui.theme.ApothecaryTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.util.Calendar

/** Every main screen over the demo shop, drawn to PNGs (run with `-Prender`), in both languages. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class ScreensBase {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    abstract val lang: String
    private val out by lazy { File(System.getProperty("render.out") ?: "build/art", "screens-$lang").apply { mkdirs() } }

    /** Today at eight in the evening: candles lit, the window dark. */
    private val evening: Long = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 20); set(Calendar.MINUTE, 10) }.timeInMillis

    @Before fun still() { Art.still = true }

    private val app get() = ApplicationProvider.getApplicationContext<ApothecaryApp>()
    private val ru get() = lang == "ru"

    private fun seed(): Settings = runBlocking {
        DemoSeed.seedIfEmpty(app, app.container)
        app.container.clerk.refreshOrders(Shop.today())
        app.container.settings.now()
    }

    private fun shop(s: Settings) = runBlocking { Shop(app.container.db.dao().itemList(), app.container.db.dao().brews().first(), s, Shop.today()) }

    private fun shot(name: String, content: @Composable () -> Unit) {
        rule.mainClock.autoAdvance = false
        rule.setContent { ApothecaryTheme { CompositionLocalProvider(LocalRu provides ru) { content() } } }
        repeat(30) { rule.mainClock.advanceTimeBy(100); Thread.sleep(20) }
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(1_000)
        rule.runOnUiThread {
            val view = rule.activity.window.decorView
            val image = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(image))
            File(out, "$name.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    private fun screen(name: String, tab: Tab, overlay: (Shop) -> Overlay? = { null }) {
        val s = seed()
        val o = overlay(shop(s))
        shot(name) { ShopRoot(s, evening, tab, o) }
    }

    @Test fun welcome() = shot("0-welcome") { Onboarding() }
    @Test fun shop() = screen("1-shop", Tab.SHOP)
    @Test fun shelves() = screen("2-shelves", Tab.SHELVES)
    @Test fun calendar() = screen("3-calendar", Tab.CALENDAR)
    @Test fun book() = screen("4-book", Tab.BOOK)
    @Test fun brew() = screen("5-brew", Tab.SHOP) { s -> Overlay.Brew(s.urgent.take(3).map { it.id }) }
    @Test fun item() = screen("6-item", Tab.SHELVES) { s -> Overlay.Ingredient(s.urgent.first { it.opened != null }.id) }
    @Test fun add() = screen("7-add", Tab.SHELVES) { Overlay.Add() }
    @Test fun decor() = screen("8-decor", Tab.SHOP) { Overlay.Decor }
}

@Config(sdk = [34], qualifiers = "en-w411dp-h891dp-xxhdpi")
class RenderScreensEn : ScreensBase() { override val lang = "en" }

@Config(sdk = [34], qualifiers = "ru-w411dp-h891dp-xxhdpi")
class RenderScreensRu : ScreensBase() { override val lang = "ru" }
