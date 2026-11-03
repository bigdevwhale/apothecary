package app.apothecary.domain

import app.apothecary.data.Brew
import app.apothecary.data.Cat
import app.apothecary.data.Customer
import app.apothecary.data.Fate
import app.apothecary.data.Item
import app.apothecary.data.Need
import app.apothecary.data.Order
import app.apothecary.data.Place
import app.apothecary.data.Settings
import java.time.LocalDate
import kotlin.random.Random

/** How an ingredient looks in its flask. */
enum class Glow { BRIGHT, SOON, LAST, MURKY }

/** Things to buy for the shop with coins. */
enum class Decor(val price: Int) {
    CANDLES(5), HERBS(8), PLANT(10), BOOKS(12), LANTERN(14), CAT(15), CLOCK(18), OWL(20), SIGN(20), STAINED(25), COPPER(30), CRYSTAL(40);

    fun name(ru: Boolean) = when (this) {
        CANDLES -> if (ru) "Свечи на прилавке" else "Candles on the counter"
        HERBS -> if (ru) "Пучки трав под потолком" else "Herb bundles from the beams"
        PLANT -> if (ru) "Цветок в горшке" else "A potted plant"
        BOOKS -> if (ru) "Полка старых книг" else "A shelf of old books"
        LANTERN -> if (ru) "Фонарь у двери" else "A lantern by the door"
        CAT -> if (ru) "Кот на прилавке" else "A cat on the counter"
        CLOCK -> if (ru) "Часы с кукушкой" else "A cuckoo clock"
        OWL -> if (ru) "Сова на жёрдочке" else "An owl on a perch"
        SIGN -> if (ru) "Вывеска с названием" else "A painted sign"
        STAINED -> if (ru) "Витраж в окне" else "A stained-glass window"
        COPPER -> if (ru) "Медный котёл" else "A copper cauldron"
        CRYSTAL -> if (ru) "Хрустальный шар" else "A crystal ball"
    }
}

/** A month or a year of the shop's ledger. */
data class Ledger(
    val brews: Int,
    val used: Int,
    val rescued: Int,
    val tossed: Int,
    val tossedMoney: Long,
    val byCat: List<Pair<Cat, Int>>,
    val worst: List<Pair<String, Int>>,
    val potions: List<Pair<String, Int>>,
) {
    val waste: Float get() = if (used + tossed == 0) 0f else tossed.toFloat() / (used + tossed)
}

/** Everything the screens need, computed from the store at [today]. */
class Shop(val items: List<Item>, val brews: List<Brew>, val s: Settings, val today: Long) {
    val stock: List<Item> = items.filter { it.fate == null }
    val food: List<Item> = stock.filter { it.place != Place.CABINET && it.cat.food }
    val cabinet: List<Item> = stock.filter { it.place == Place.CABINET || !it.cat.food }

    fun item(id: Long) = items.firstOrNull { it.id == id }

    /** Food by urgency: what loses its strength first comes first; things that keep go last. */
    val urgent: List<Item> = food.sortedWith(compareBy<Item> { left(it) ?: Long.MAX_VALUE }.thenBy { it.name })

    fun on(p: Place): List<Item> = stock.filter { it.place == p }.sortedWith(compareBy<Item> { left(it) ?: Long.MAX_VALUE }.thenBy { it.name })

    val coins: Int get() = brews.sumOf { it.coins } - s.spent

    // ---- Ledger and rank

    private fun closedFood(from: Long, to: Long) = items.filter { it.fate != null && it.cat.food && (it.closed ?: -1) in from..to }

    fun ledger(from: Long, to: Long): Ledger {
        val closed = closedFood(from, to)
        val tossed = closed.filter { it.fate == Fate.TOSSED }
        val bs = brews.filter { it.day in from..to }
        return Ledger(
            brews = bs.size,
            used = closed.count { it.fate == Fate.EATEN || it.fate == Fate.GIVEN },
            rescued = bs.sumOf { it.rescued },
            tossed = tossed.size,
            tossedMoney = tossed.sumOf { it.price.toLong() },
            byCat = tossed.groupingBy { it.cat }.eachCount().toList().sortedByDescending { it.second },
            worst = tossed.groupingBy { it.name.trim().lowercase().replaceFirstChar { c -> c.uppercase() } }.eachCount().toList().sortedByDescending { it.second }.take(5),
            potions = bs.groupingBy { it.potion }.eachCount().toList().sortedByDescending { it.second },
        )
    }

    /** The last thirty days decide the rank: how many brews in all, and how little was lost lately. */
    val month: Ledger get() = ledger(today - 29, today)

    val rank: Int get() {
        val n = brews.size
        val w = month.waste
        return when {
            n >= 100 && w <= .07f -> 3
            n >= 40 && w <= .15f -> 2
            n >= 10 && w <= .30f -> 1
            else -> 0
        }
    }

    /** What the next rank asks for, or null at the top. */
    fun nextRank(): Pair<Int, Float>? = when (rank) {
        0 -> 10 to .30f
        1 -> 40 to .15f
        2 -> 100 to .07f
        else -> null
    }

    /** Days since something was last thrown away (or since the first day, if never). */
    val clean: Long get() {
        val last = items.filter { it.fate == Fate.TOSSED && it.cat.food }.maxOfOrNull { it.closed ?: 0 }
        val first = items.minOfOrNull { epoch(it.added) } ?: today
        return today - (last ?: (first - 1)) - 1
    }

    val known: Set<String> get() = brews.map { it.potion }.toSet()

    // ---- Cabinet

    fun needMet(n: Need): Boolean = cabinet.any { it.need == n && (left(it) ?: 1) >= 0 }

    val ready: Pair<Int, Int> get() = s.needs.count { needMet(it) } to s.needs.size

    // ---- Orders

    val served: Set<String> get() = brews.filter { it.day == today }.flatMap { it.orders.split(',') }.filter { it.isNotBlank() }.toSet()

    companion object {
        fun epoch(ms: Long): Long = java.time.Instant.ofEpochMilli(ms).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay()
        fun today(): Long = LocalDate.now().toEpochDay()

        fun deadline(i: Item): Long? {
            if (i.place == Place.ICE && i.frozen != null) return i.frozen + (i.iceDays ?: Lore.ICE_DAYS)
            var d = i.printed
            if (i.opened != null && i.openDays != null) d = minOf(d ?: Long.MAX_VALUE, i.opened + i.openDays)
            return d
        }

        /** The day the strength started draining from. */
        fun start(i: Item): Long = if (i.place == Place.ICE && i.frozen != null) i.frozen else i.opened ?: i.bought

        /** How much of its life the ingredient still has, 0..1. */
        fun potency(i: Item, today: Long): Float {
            val d = deadline(i) ?: return 1f
            val span = (d - start(i)).coerceAtLeast(1)
            return ((d - today).toFloat() / span).coerceIn(0f, 1f)
        }

        fun glow(i: Item, today: Long): Glow {
            val n = left(i, today) ?: return Glow.BRIGHT
            val cab = !i.cat.food || i.place == Place.CABINET
            return when {
                n < 0 -> Glow.MURKY
                n == 0L -> Glow.LAST
                n <= (if (cab) 30 else 2) -> Glow.SOON
                else -> Glow.BRIGHT
            }
        }

        fun left(i: Item, today: Long): Long? = deadline(i)?.let { it - today }

        /** Pills left in a course and how many days they last. */
        fun course(i: Item, today: Long): Pair<Int, Int>? {
            if (i.perDay <= 0 || i.courseFrom == null) return null
            val n = (i.left - i.perDay * (today - i.courseFrom)).coerceAtLeast(0).toInt()
            return n to n / i.perDay
        }

        /**
         * Today's visitors: they ask for what is about to be lost. One or two orders, each for one or two essences
         * found among ingredients on their last three days. A quiet day when nothing is urgent.
         */
        fun orders(food: List<Item>, today: Long): List<Order> {
            val soon = food.filter { (left(it, today) ?: 99) in 0..2 }.map { it.cat }.distinct()
            if (soon.isEmpty()) return emptyList()
            val r = Random(today * 31 + soon.size)
            val who = Customer.entries.shuffled(r)
            val cats = soon.shuffled(r)
            val n = if (cats.size >= 3) 2 else 1
            return (0 until n).map { k ->
                val pick = if (k == 0 && n == 2) cats.take(2) else if (k == 1) cats.drop(2).take(1) else cats.take(if (cats.size >= 2 && r.nextBoolean()) 2 else 1)
                Order(who[k], pick)
            }
        }

        /** Coins for a brew: one per ingredient up to five, two for each saved on its last days, three per order. */
        fun coins(n: Int, rescued: Int, orders: Int) = minOf(n, 5) + 2 * rescued + 3 * orders

        /** The ingredients' essences blended into one colour. */
        fun blend(cats: List<Cat>): Int {
            if (cats.isEmpty()) return 0xFFA9B2AE.toInt()
            var r = 0; var g = 0; var b = 0
            for (c in cats) { r += (c.color shr 16 and 0xFF).toInt(); g += (c.color shr 8 and 0xFF).toInt(); b += (c.color and 0xFF).toInt() }
            r /= cats.size; g /= cats.size; b /= cats.size
            // A little richer than a plain average, so mixtures don't turn to mud.
            val m = maxOf(r, g, b, 1)
            val k = 235f / m
            fun f(x: Int) = (x + (x * k - x) * .45f).toInt().coerceIn(0, 255)
            return (0xFF shl 24) or (f(r) shl 16) or (f(g) shl 8) or f(b)
        }
    }

    fun left(i: Item) = left(i, today)
    fun glow(i: Item) = glow(i, today)
    fun potency(i: Item) = potency(i, today)
}
