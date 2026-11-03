package app.apothecary

import app.apothecary.data.Cat
import app.apothecary.data.Fate
import app.apothecary.data.Item
import app.apothecary.data.Place
import app.apothecary.data.Settings
import app.apothecary.domain.Book
import app.apothecary.domain.Glow
import app.apothecary.domain.Lore
import app.apothecary.domain.Shop
import app.apothecary.domain.Words
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShopTest {
    private val today = 20_000L
    private fun item(cat: Cat = Cat.DAIRY, printed: Long? = today + 10, opened: Long? = null, openDays: Int? = null, place: Place = Place.CELLAR, frozen: Long? = null, iceDays: Int? = null) =
        Item(name = "x", cat = cat, place = place, bought = today - 2, printed = printed, opened = opened, openDays = openDays, frozen = frozen, iceDays = iceDays)

    @Test fun openingShortensTheDeadline() {
        val milk = item(printed = today + 10, opened = today - 1, openDays = 3)
        assertEquals(today + 2, Shop.deadline(milk))
        assertEquals(2L, Shop.left(milk, today))
    }

    @Test fun printedDateWinsWhenEarlier() {
        assertEquals(today + 1, Shop.deadline(item(printed = today + 1, opened = today, openDays = 5)))
    }

    @Test fun iceRestartsTheClock() {
        val i = item(place = Place.ICE, printed = today - 5, frozen = today - 10, iceDays = 90)
        assertEquals(today + 80, Shop.deadline(i))
        assertEquals(Glow.BRIGHT, Shop.glow(i, today))
    }

    @Test fun noDateKeeps() {
        val honey = item(printed = null)
        assertNull(Shop.deadline(honey))
        assertEquals(1f, Shop.potency(honey, today))
    }

    @Test fun glowStates() {
        assertEquals(Glow.LAST, Shop.glow(item(printed = today), today))
        assertEquals(Glow.SOON, Shop.glow(item(printed = today + 2), today))
        assertEquals(Glow.MURKY, Shop.glow(item(printed = today - 1), today))
        // Medicines warn a month ahead.
        assertEquals(Glow.SOON, Shop.glow(item(cat = Cat.MEDS, place = Place.CABINET, printed = today + 20), today))
    }

    @Test fun recipes() {
        assertEquals("CLARITY", Book.brew(listOf(Cat.DAIRY, Cat.BERRY), 0).key)
        assertEquals("FEAST", Book.brew(listOf(Cat.MEAT, Cat.VEG, Cat.GREENS), 0).key)
        assertEquals(Book.GRAND, Book.brew(listOf(Cat.MEAT, Cat.VEG, Cat.GREENS, Cat.BREAD, Cat.CHEESE), 0).key)
        assertEquals(Book.LAST, Book.brew(listOf(Cat.DAIRY, Cat.DAIRY, Cat.BREAD), 3).key)
        assertEquals("DAIRY", Book.brew(listOf(Cat.DAIRY), 1).key)
        assertEquals(Book.potions.size, Book.potions.map { it.key }.toSet().size)
    }

    @Test fun waste() {
        val eaten = (1..9).map { item().copy(fate = Fate.EATEN, closed = today - 1) }
        val tossed = item().copy(fate = Fate.TOSSED, closed = today - 2)
        val s = Shop(eaten + tossed, emptyList(), Settings(), today)
        assertEquals(.1f, s.month.waste, 1e-4f)
        assertEquals(1, s.month.tossed)
    }

    @Test fun ordersAskForUrgentEssences() {
        val food = listOf(item(cat = Cat.MEAT, printed = today + 1), item(cat = Cat.GREENS, printed = today), item(cat = Cat.GRAIN, printed = today + 300))
        val orders = Shop.orders(food, today)
        assertTrue(orders.isNotEmpty())
        assertTrue(orders.all { o -> o.cats.all { it == Cat.MEAT || it == Cat.GREENS } })
        assertTrue(Shop.orders(listOf(item(printed = today + 9)), today).isEmpty())
    }

    @Test fun guideFindsBothLanguages() {
        assertEquals("Молоко", Lore.find("мол", true).first().ru)
        assertEquals("Milk", Lore.find("mil", false).first().en)
        assertTrue(Lore.find("ибу", true).any { it.ru == "Ибупрофен" })
    }

    @Test fun russianPlurals() {
        assertEquals("1 день", Words.days(1, true))
        assertEquals("3 дня", Words.days(3, true))
        assertEquals("11 дней", Words.days(11, true))
        assertEquals("21 день", Words.days(21, true))
    }
}
