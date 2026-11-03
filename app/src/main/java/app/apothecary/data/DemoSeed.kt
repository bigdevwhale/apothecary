package app.apothecary.data

import android.content.Context
import app.apothecary.AppContainer
import app.apothecary.domain.Book
import app.apothecary.domain.Decor
import app.apothecary.domain.Kind
import app.apothecary.domain.Lore
import app.apothecary.domain.Shop
import app.apothecary.ru
import kotlin.random.Random

/** A shop two and a half months old, for the demo build and screenshots. */
object DemoSeed {
    suspend fun seedIfEmpty(context: Context, c: AppContainer) {
        val dao = c.db.dao()
        if (dao.itemCount() > 0) return
        val ru = context.ru
        val today = Shop.today()
        val ms = System.currentTimeMillis()
        fun k(name: String): Kind = Lore.kinds.first { it.ru == name }

        /** An item from the guide: bought [ago] days back, the package date from the guide unless [left] says how many days remain. */
        fun item(name: String, ago: Int, left: Int? = null, opened: Int? = null, frozen: Int? = null, jar: Int = 0, price: Int = 0,
                 perDay: Int = 0, count: Int = 0, courseAgo: Int = 0, place: Place? = null): Item {
            val kd = k(name)
            val bought = today - ago
            return Item(
                name = kd.name(ru), cat = kd.cat, place = place ?: kd.place, bought = bought,
                printed = if (left != null) today + left else kd.keep?.let { bought + it },
                opened = opened?.let { today - it }, openDays = kd.open,
                frozen = frozen?.let { today - it }, iceDays = if (frozen != null) kd.ice ?: Lore.ICE_DAYS else null,
                jar = jar, price = price * 100, need = kd.need,
                perDay = perDay, left = count, courseFrom = if (perDay > 0) today - courseAgo else null,
                added = ms - ago * 86_400_000L,
            )
        }

        val stock = listOf(
            item("Молоко", 3, left = 6, opened = 2, price = 95),
            item("Кефир", 9, left = 0, price = 80),
            item("Сметана", 4, opened = 1, price = 110),
            item("Сыр твёрдый", 12, opened = 10, price = 420),
            item("Куриное филе", 2, left = 1, price = 380),
            item("Укроп", 4, left = 1, price = 60),
            item("Помидоры", 3, price = 220),
            item("Йогурт", 15, left = -1, price = 70),
            item("Яйца", 6, price = 120),
            item("Борщ", 1, jar = 3),
            item("Клубника", 0, price = 350),
            item("Сливочное масло", 10, opened = 6, price = 230),
            item("Морковь", 8, price = 50),
            item("Моцарелла", 5, left = 2, price = 190),
            item("Гречка", 30, price = 110),
            item("Макароны", 40, price = 90),
            item("Хлеб", 2, price = 65),
            item("Бананы", 3, price = 120),
            item("Мёд", 60, price = 450),
            item("Томатная паста", 20, opened = 3, price = 85),
            item("Пельмени", 14, price = 390),
            item("Фарш", 20, frozen = 19, price = 420, place = Place.ICE),
            item("Замороженные ягоды", 35, price = 260),
            item("Парацетамол", 200, left = 500),
            item("Ибупрофен", 400, left = 20),
            item("Пластыри", 300, left = 900),
            item("Хлоргексидин", 150, left = 600),
            item("Активированный уголь", 250, left = 700),
            item("Глазные капли", 60, left = 400, opened = 20),
            item("Термометр", 500),
            item("Витамин D", 25, left = 600, perDay = 1, count = 60, courseAgo = 25),
            item("Магний", 40, left = 500, perDay = 2, count = 90, courseAgo = 38),
        )
        dao.insertItems(stock)

        // Seventy days of suppers: mostly brewed in time, a few bottles poured out (more of them early on).
        val r = Random(7)
        val pool = listOf("Молоко", "Творог", "Сыр твёрдый", "Курица", "Фарш", "Говядина", "Рыба", "Сельдь", "Яйца", "Укроп", "Петрушка", "Шпинат", "Огурцы", "Помидоры",
            "Перец", "Кабачок", "Брокколи", "Грибы", "Картофель", "Яблоки", "Апельсины", "Лимон", "Клубника", "Черника", "Хлеб", "Батон", "Рис", "Гречка", "Овсянка",
            "Макароны", "Песто", "Кетчуп", "Торт", "Печенье", "Суп", "Котлеты", "Плов", "Сметана", "Йогурт", "Моцарелла")
        val pastItems = mutableListOf<Item>()
        for (ago in 70 downTo 1) {
            val day = today - ago
            val meals = if (r.nextInt(5) == 0) 2 else 1
            repeat(meals) {
                val n = 1 + r.nextInt(if (r.nextInt(6) == 0) 5 else 3)
                val names = pool.shuffled(r).take(n)
                val kinds = names.map { k(it) }
                val rescued = if (r.nextInt(3) == 0) 1 + r.nextInt(n) else 0
                val cats = kinds.map { it.cat }
                val potion = Book.brew(cats, rescued)
                val coins = Shop.coins(n, rescued, if (r.nextInt(4) == 0) 1 else 0)
                val id = dao.insert(Brew(day = day, at = ms - ago * 86_400_000L + it * 3_600_000L, potion = potion.key, names = kinds.joinToString("\n") { x -> x.name(ru) },
                    cats = cats.joinToString(",") { x -> x.name }, color = Shop.blend(cats), coins = coins, rescued = rescued))
                for (x in kinds) pastItems += Item(name = x.name(ru), cat = x.cat, place = x.place, bought = day - 3, printed = day + (x.keep ?: 30).coerceAtMost(30) - 3,
                    fate = Fate.EATEN, closed = day, brew = id, price = (60 + r.nextInt(300)) * 100, added = ms - (ago + 3) * 86_400_000L)
            }
            if (r.nextInt(if (ago > 40) 6 else 14) == 0) {
                val x = k(listOf("Кефир", "Салат листовой", "Кинза", "Хлеб", "Творог", "Клубника", "Сметана").random(r))
                pastItems += Item(name = x.name(ru), cat = x.cat, place = x.place, bought = day - 6, printed = day - 1, fate = Fate.TOSSED, closed = day,
                    price = (60 + r.nextInt(200)) * 100, added = ms - (ago + 6) * 86_400_000L)
            }
        }
        dao.insertItems(pastItems)

        val earned = dao.brewList().sumOf { it.coins }
        c.settings.update {
            it.copy(
                onboarded = true, shop = if (ru) "Зелёная склянка" else "The Green Phial",
                bought = setOf(Decor.CANDLES, Decor.HERBS, Decor.CAT, Decor.BOOKS, Decor.LANTERN, Decor.SIGN, Decor.STAINED, Decor.PLANT).map { d -> d.name }.toSet(),
                spent = earned - 37, audit = today - 9,
            )
        }
    }
}
