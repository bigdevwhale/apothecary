package app.apothecary.domain

import app.apothecary.data.Cat
import app.apothecary.data.Customer

/**
 * A recipe in the potion book. A brew becomes the most specific recipe whose essences it holds.
 * [shape] picks the bottle: 0 round flask, 1 tall vial, 2 squat jar, 3 heart bottle.
 */
data class Potion(val key: String, val ru: String, val en: String, val needs: Set<Cat>, val shape: Int, val rare: Boolean = false) {
    fun name(ru: Boolean) = if (ru) this.ru else en
}

object Book {
    const val GRAND = "GRAND"
    const val LAST = "LAST"

    private fun p(key: String, ru: String, en: String, shape: Int, vararg c: Cat) = Potion(key, ru, en, c.toSet(), shape)

    val potions: List<Potion> = listOf(
        // One essence
        p("DAIRY", "Молочный отвар", "Milk draught", 0, Cat.DAIRY),
        p("CHEESE", "Сырная настойка", "Cheese tincture", 2, Cat.CHEESE),
        p("MEAT", "Отвар силы", "Draught of strength", 2, Cat.MEAT),
        p("FISH", "Настойка глубины", "Tincture of the deep", 1, Cat.FISH),
        p("EGGS", "Золотой желток", "Golden yolk", 0, Cat.EGGS),
        p("GREENS", "Зелёный настой", "Green infusion", 1, Cat.GREENS),
        p("VEG", "Огородный отвар", "Garden broth", 2, Cat.VEG),
        p("FRUIT", "Солнечный нектар", "Sun nectar", 3, Cat.FRUIT),
        p("BERRY", "Ягодный эликсир", "Berry elixir", 3, Cat.BERRY),
        p("BREAD", "Хлебный бальзам", "Bread balm", 2, Cat.BREAD),
        p("GRAIN", "Зерновой отвар", "Grain brew", 0, Cat.GRAIN),
        p("SAUCE", "Пряная капля", "Spiced drop", 1, Cat.SAUCE),
        p("SWEET", "Сладкий сироп", "Sweet syrup", 3, Cat.SWEET),
        p("DRINK", "Освежающее питьё", "Cooling draught", 1, Cat.DRINK),
        p("LEFTOVER", "Второе дыхание", "Second wind", 0, Cat.LEFTOVER),
        p("OTHER", "Загадочная смесь", "Curious mixture", 2, Cat.OTHER),
        // Two
        p("CLARITY", "Зелье утренней ясности", "Potion of morning clarity", 3, Cat.DAIRY, Cat.BERRY),
        p("MORNING", "Оберег тёплого утра", "Warm morning charm", 0, Cat.DAIRY, Cat.GRAIN),
        p("CLOUD", "Облачный эликсир", "Cloud elixir", 0, Cat.EGGS, Cat.DAIRY),
        p("SPRING", "Весенний эликсир", "Spring elixir", 1, Cat.EGGS, Cat.GREENS),
        p("CAMP", "Зелье походного котла", "Campfire pot potion", 2, Cat.MEAT, Cat.VEG),
        p("SMITH", "Отвар кузнеца", "Blacksmith's draught", 2, Cat.MEAT, Cat.GRAIN),
        p("HARBOUR", "Настойка тихой гавани", "Tincture of the quiet harbour", 1, Cat.FISH, Cat.FRUIT),
        p("FISHER", "Рыбацкий эликсир", "Fisherman's elixir", 0, Cat.FISH, Cat.GRAIN),
        p("ROAD", "Бальзам долгой дороги", "Balm of the long road", 2, Cat.CHEESE, Cat.BREAD),
        p("MEADOW", "Эликсир зелёного луга", "Elixir of the green meadow", 1, Cat.GREENS, Cat.VEG),
        p("SUMMER", "Летний нектар", "Summer nectar", 3, Cat.FRUIT, Cat.BERRY),
        p("LULLABY", "Колыбельное зелье", "Lullaby potion", 3, Cat.SWEET, Cat.DAIRY),
        p("TEA", "Чайное зелье", "Teatime potion", 0, Cat.BREAD, Cat.SWEET),
        p("THRIFT", "Зелье бережливости", "Potion of thrift", 2, Cat.LEFTOVER, Cat.BREAD),
        p("FIRE", "Огненное зелье", "Fire potion", 1, Cat.SAUCE, Cat.MEAT),
        p("HARVEST", "Отвар осеннего урожая", "Harvest broth", 2, Cat.VEG, Cat.GRAIN),
        // Three
        p("GLADE", "Эликсир лесной поляны", "Glade elixir", 1, Cat.CHEESE, Cat.EGGS, Cat.GREENS),
        p("FEAST", "Зелье пира", "Feast potion", 2, Cat.MEAT, Cat.VEG, Cat.GREENS),
        p("BREEZE", "Морской бриз", "Sea breeze draught", 1, Cat.FISH, Cat.VEG, Cat.GREENS),
        p("RAINBOW", "Радужный эликсир", "Rainbow elixir", 3, Cat.DAIRY, Cat.FRUIT, Cat.BERRY),
        p("WAYFARER", "Зелье путника", "Wayfarer's potion", 2, Cat.BREAD, Cat.CHEESE, Cat.MEAT),
        p("SPICEPOT", "Пряный котелок", "Spiced pot", 0, Cat.GRAIN, Cat.VEG, Cat.SAUCE),
        // Rare
        Potion(LAST, "Зелье последнего часа", "Potion of the last hour", emptySet(), 3, rare = true),
        Potion(GRAND, "Великий эликсир", "Grand elixir", emptySet(), 0, rare = true),
    )

    fun potion(key: String): Potion = potions.firstOrNull { it.key == key } ?: potions.last { it.key == "OTHER" }

    /**
     * The recipe a brew makes. Five essences or more make the grand elixir; three or more ingredients all saved on
     * their last day make the potion of the last hour; otherwise the most specific recipe that fits.
     */
    fun brew(cats: List<Cat>, rescued: Int): Potion {
        val set = cats.filter { it.food }.toSet()
        if (set.size >= 5) return potion(GRAND)
        if (cats.size >= 3 && rescued == cats.size) return potion(LAST)
        return potions.filter { !it.rare && it.needs.isNotEmpty() && set.containsAll(it.needs) }
            .maxByOrNull { it.needs.size } ?: potion("OTHER")
    }

    /** What the rare ones ask for, said in words. */
    fun rareHint(key: String, ru: Boolean) = when (key) {
        GRAND -> if (ru) "пять разных эссенций в одном котле" else "five different essences in one pot"
        LAST -> if (ru) "три ингредиента, и все в свой последний день" else "three ingredients, all on their last day"
        else -> ""
    }
}

/** The six regulars of the shop, and the little things they say. */
object Regulars {
    fun name(c: Customer, ru: Boolean) = when (c) {
        Customer.HERBALIST -> if (ru) "Травница Мирта" else "Myrtle the herbalist"
        Customer.MILLER -> if (ru) "Мельник Ансельм" else "Anselm the miller"
        Customer.KNIGHT -> if (ru) "Сэр Годрик" else "Sir Godric"
        Customer.CLOCKMAKER -> if (ru) "Часовщик Тобиас" else "Tobias the clockmaker"
        Customer.BARD -> if (ru) "Бард Лиора" else "Liora the bard"
        Customer.FERRYWOMAN -> if (ru) "Паромщица Ингрид" else "Ingrid of the ferry"
    }

    fun line(c: Customer, ru: Boolean, seed: Int): String {
        val ls = when (c) {
            Customer.HERBALIST -> if (ru) listOf("Весь день сушила травы, руки пахнут мятой.", "Говорят, к вечеру будет туман.", "Мои травы не лечат от усталости. Твои зелья лечат.")
                else listOf("Drying herbs all day, my hands smell of mint.", "They say fog by evening.", "My herbs don't cure tiredness. Your potions do.")
            Customer.MILLER -> if (ru) listOf("Жернова встали, ветра нет.", "Мука по всему дому, а есть нечего.", "Принёс тебе мешочек отрубей. Шучу. Мне бы поесть.")
                else listOf("The millstones stopped, no wind.", "Flour all over the house and nothing to eat.", "I brought you bran. Joking. I need food.")
            Customer.KNIGHT -> if (ru) listOf("Доспехи стали тесноваты. Что-нибудь лёгкое?", "В мои годы драконов уже не ищут.", "Хорошее зелье лучше хорошего меча.")
                else listOf("The armour feels tight. Something light?", "At my age one no longer hunts dragons.", "A good potion beats a good sword.")
            Customer.CLOCKMAKER -> if (ru) listOf("Часы на башне опять отстают на четыре минуты.", "Я ненадолго. Ровно на семь минут.", "Пружины любят точность. Желудок тоже.")
                else listOf("The tower clock is four minutes slow again.", "Just a moment. Seven minutes exactly.", "Springs like precision. So does the stomach.")
            Customer.BARD -> if (ru) listOf("Голос сел после вчерашнего вечера в таверне.", "Сложу о тебе балладу. Если будет вкусно.", "Мне бы что-нибудь для вдохновения.")
                else listOf("Lost my voice after last night at the tavern.", "I'll write you a ballad. If it's tasty.", "Something for inspiration, please.")
            Customer.FERRYWOMAN -> if (ru) listOf("Река сегодня неспокойная.", "Сорок переправ с утра. Сил нет.", "На том берегу о твоей лавке уже рассказывают.")
                else listOf("The river is restless today.", "Forty crossings since dawn.", "They talk about your shop on the far bank now.")
        }
        return ls[Math.floorMod(seed, ls.size)]
    }
}
