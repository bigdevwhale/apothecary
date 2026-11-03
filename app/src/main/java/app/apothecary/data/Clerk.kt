package app.apothecary.data

import app.apothecary.domain.Book
import app.apothecary.domain.Decor
import app.apothecary.domain.Lore
import app.apothecary.domain.Potion
import app.apothecary.domain.Shop

/** What a brew gave back, for the reveal. */
data class Brewed(val brew: Brew, val potion: Potion, val first: Boolean, val served: List<Order>)

/** Every change to the shop goes through here, so the rules live in one place. */
class Clerk(private val dao: ShopDao, private val settings: SettingsStore) {

    suspend fun add(i: Item): Long = dao.insert(i)

    suspend fun save(i: Item) = dao.update(i)

    suspend fun delete(i: Item) = dao.delete(i)

    suspend fun nextJar(): Int = (dao.lastJar() ?: 0) + 1

    /** The seal is broken: from now on the open-life clock runs too. */
    suspend fun open(i: Item, today: Long) = dao.update(i.copy(opened = today))

    /** Into the ice chest: the clock freezes, a new and longer one starts. */
    suspend fun freeze(i: Item, today: Long) {
        val days = Lore.exact(i.name)?.ice ?: i.iceDays ?: Lore.ICE_DAYS
        dao.update(i.copy(place = Place.ICE, frozen = today, iceDays = days))
    }

    /** Out of the ice chest into the cellar: thawed things keep a day or two. */
    suspend fun thaw(i: Item, today: Long) {
        val keep = if (i.cat == Cat.MEAT || i.cat == Cat.FISH) 1 else 2
        dao.update(i.copy(place = Place.CELLAR, frozen = null, printed = today + keep, opened = null))
    }

    suspend fun close(i: Item, fate: Fate, today: Long) = dao.update(i.copy(fate = fate, closed = today))

    /** Puts it back on the shelf, as if the last farewell never happened. */
    suspend fun reopen(i: Item) = dao.update(i.copy(fate = null, closed = null, brew = null))

    /** Today's visitors, written once in the morning so they don't change as the shelves empty. */
    suspend fun refreshOrders(today: Long) {
        val s = settings.now()
        if (s.ordersDay == today) return
        val food = dao.stock().filter { it.place != Place.CABINET && it.cat.food }
        val orders = Shop.orders(food, today)
        settings.update { it.copy(ordersDay = today, orders = orders) }
    }

    /** Cooks the chosen ingredients: they leave the shelves eaten, and a potion goes into the book. */
    suspend fun brew(ids: List<Long>, today: Long): Brewed? {
        val all = dao.itemList()
        val used = all.filter { it.id in ids && it.fate == null }
        if (used.isEmpty()) return null
        val rescued = used.count { (Shop.left(it, today) ?: 99) in 0..1 }
        val cats = used.map { it.cat }
        val potion = Book.brew(cats, rescued)
        val s = settings.now()
        val before = dao.brewList()
        val done = before.filter { it.day == today }.flatMap { it.orders.split(',') }.toSet()
        val served = (if (s.ordersDay == today) s.orders else emptyList()).filter { it.key() !in done && cats.containsAll(it.cats) }
        val b = Brew(
            day = today, at = System.currentTimeMillis(), potion = potion.key,
            names = used.joinToString("\n") { it.name }, cats = cats.joinToString(",") { it.name },
            color = Shop.blend(cats), orders = served.joinToString(",") { it.key() },
            coins = Shop.coins(used.size, rescued, served.size), rescued = rescued,
        )
        val id = dao.insert(b)
        dao.updateItems(used.map { it.copy(fate = Fate.EATEN, closed = today, brew = id) })
        return Brewed(b.copy(id = id), potion, before.none { it.potion == potion.key }, served)
    }

    suspend fun buy(d: Decor): Boolean {
        val coins = dao.brewList().sumOf { it.coins } - settings.now().spent
        if (coins < d.price) return false
        settings.update { it.copy(spent = it.spent + d.price, bought = it.bought + d.name) }
        return true
    }
}
