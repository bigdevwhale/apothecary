package app.apothecary.domain

import app.apothecary.data.Cat
import app.apothecary.data.Need
import app.apothecary.data.Place

/**
 * An ingredient the shop knows by name: where it lives and how long it keeps.
 * [keep] is days from purchase when the package has no date, [open] days once opened, [ice] days in the ice chest.
 * The numbers are the usual household guidance, rounded to the careful side; any of them can be changed on the item.
 */
data class Kind(
    val ru: String, val en: String, val cat: Cat, val place: Place,
    val keep: Int?, val open: Int? = null, val ice: Int? = null, val need: Need? = null,
) {
    fun name(ru: Boolean) = if (ru) this.ru else en
}

object Lore {
    private val C = Place.CELLAR
    private val P = Place.PANTRY
    private val I = Place.ICE
    private val M = Place.CABINET

    val kinds: List<Kind> = listOf(
        // Dairy
        Kind("Молоко", "Milk", Cat.DAIRY, C, 7, 3, 90),
        Kind("Молоко ультрапастеризованное", "UHT milk", Cat.DAIRY, P, 180, 4),
        Kind("Кефир", "Kefir", Cat.DAIRY, C, 10, 3),
        Kind("Ряженка", "Baked milk kefir", Cat.DAIRY, C, 7, 3),
        Kind("Йогурт", "Yogurt", Cat.DAIRY, C, 14, 3),
        Kind("Сметана", "Sour cream", Cat.DAIRY, C, 14, 5),
        Kind("Творог", "Cottage cheese", Cat.DAIRY, C, 7, 3, 60),
        Kind("Сливки", "Cream", Cat.DAIRY, C, 10, 3),
        Kind("Сливочное масло", "Butter", Cat.DAIRY, C, 30, 21, 180),
        Kind("Сгущёнка", "Condensed milk", Cat.DAIRY, P, 365, 7),
        Kind("Растительное молоко", "Plant milk", Cat.DRINK, P, 180, 7),
        // Cheese
        Kind("Сыр твёрдый", "Hard cheese", Cat.CHEESE, C, 60, 21, 120),
        Kind("Пармезан", "Parmesan", Cat.CHEESE, C, 120, 30),
        Kind("Моцарелла", "Mozzarella", Cat.CHEESE, C, 20, 3),
        Kind("Брынза", "Feta", Cat.CHEESE, C, 30, 7),
        Kind("Сыр с плесенью", "Blue cheese", Cat.CHEESE, C, 30, 7),
        Kind("Сливочный сыр", "Cream cheese", Cat.CHEESE, C, 30, 7),
        Kind("Плавленый сыр", "Processed cheese", Cat.CHEESE, C, 60, 7),
        // Meat
        Kind("Курица", "Chicken", Cat.MEAT, C, 3, null, 270),
        Kind("Куриное филе", "Chicken breast", Cat.MEAT, C, 3, null, 270),
        Kind("Фарш", "Mince", Cat.MEAT, C, 2, null, 90),
        Kind("Говядина", "Beef", Cat.MEAT, C, 4, null, 180),
        Kind("Свинина", "Pork", Cat.MEAT, C, 4, null, 180),
        Kind("Индейка", "Turkey", Cat.MEAT, C, 3, null, 180),
        Kind("Варёная колбаса", "Cooked sausage", Cat.MEAT, C, 10, 4),
        Kind("Копчёная колбаса", "Smoked sausage", Cat.MEAT, C, 30, 10),
        Kind("Сосиски", "Frankfurters", Cat.MEAT, C, 10, 3, 60),
        Kind("Ветчина", "Ham", Cat.MEAT, C, 10, 3),
        Kind("Бекон", "Bacon", Cat.MEAT, C, 14, 5, 30),
        Kind("Пельмени", "Dumplings", Cat.MEAT, I, 90),
        // Fish
        Kind("Рыба", "Fresh fish", Cat.FISH, C, 2, null, 90),
        Kind("Слабосолёная сёмга", "Lightly salted salmon", Cat.FISH, C, 10, 3),
        Kind("Сельдь", "Herring", Cat.FISH, C, 14, 3),
        Kind("Креветки", "Prawns", Cat.FISH, I, 120),
        Kind("Рыбные консервы", "Tinned fish", Cat.FISH, P, 730, 2),
        Kind("Икра", "Caviar", Cat.FISH, C, 30, 3),
        // Eggs
        Kind("Яйца", "Eggs", Cat.EGGS, C, 25),
        Kind("Перепелиные яйца", "Quail eggs", Cat.EGGS, C, 30),
        // Greens
        Kind("Укроп", "Dill", Cat.GREENS, C, 5),
        Kind("Петрушка", "Parsley", Cat.GREENS, C, 5),
        Kind("Кинза", "Coriander", Cat.GREENS, C, 4),
        Kind("Базилик", "Basil", Cat.GREENS, C, 4),
        Kind("Зелёный лук", "Spring onions", Cat.GREENS, C, 5),
        Kind("Салат листовой", "Lettuce", Cat.GREENS, C, 4),
        Kind("Шпинат", "Spinach", Cat.GREENS, C, 4),
        Kind("Руккола", "Rocket", Cat.GREENS, C, 3),
        Kind("Мята", "Mint", Cat.GREENS, C, 5),
        // Vegetables
        Kind("Огурцы", "Cucumbers", Cat.VEG, C, 7),
        Kind("Помидоры", "Tomatoes", Cat.VEG, C, 7),
        Kind("Черри", "Cherry tomatoes", Cat.VEG, C, 7),
        Kind("Перец", "Peppers", Cat.VEG, C, 10),
        Kind("Кабачок", "Courgette", Cat.VEG, C, 10),
        Kind("Баклажан", "Aubergine", Cat.VEG, C, 7),
        Kind("Брокколи", "Broccoli", Cat.VEG, C, 5, null, 240),
        Kind("Цветная капуста", "Cauliflower", Cat.VEG, C, 7),
        Kind("Капуста", "Cabbage", Cat.VEG, C, 30),
        Kind("Морковь", "Carrots", Cat.VEG, C, 30),
        Kind("Свёкла", "Beetroot", Cat.VEG, C, 30),
        Kind("Грибы", "Mushrooms", Cat.VEG, C, 4, null, 180),
        Kind("Авокадо", "Avocado", Cat.VEG, P, 4),
        Kind("Картофель", "Potatoes", Cat.VEG, P, 60),
        Kind("Лук", "Onions", Cat.VEG, P, 60),
        Kind("Чеснок", "Garlic", Cat.VEG, P, 90),
        Kind("Имбирь", "Ginger", Cat.VEG, C, 21),
        Kind("Тыква", "Pumpkin", Cat.VEG, P, 60),
        Kind("Замороженные овощи", "Frozen vegetables", Cat.VEG, I, 240),
        // Fruit
        Kind("Яблоки", "Apples", Cat.FRUIT, C, 30),
        Kind("Груши", "Pears", Cat.FRUIT, C, 10),
        Kind("Бананы", "Bananas", Cat.FRUIT, P, 5),
        Kind("Апельсины", "Oranges", Cat.FRUIT, C, 21),
        Kind("Мандарины", "Tangerines", Cat.FRUIT, C, 14),
        Kind("Лимон", "Lemon", Cat.FRUIT, C, 21, 7),
        Kind("Лайм", "Lime", Cat.FRUIT, C, 14, 5),
        Kind("Виноград", "Grapes", Cat.FRUIT, C, 7),
        Kind("Персики", "Peaches", Cat.FRUIT, C, 4),
        Kind("Киви", "Kiwi", Cat.FRUIT, C, 21),
        Kind("Ананас", "Pineapple", Cat.FRUIT, P, 5, 3),
        Kind("Хурма", "Persimmon", Cat.FRUIT, C, 7),
        Kind("Гранат", "Pomegranate", Cat.FRUIT, C, 30),
        // Berries
        Kind("Клубника", "Strawberries", Cat.BERRY, C, 3, null, 240),
        Kind("Малина", "Raspberries", Cat.BERRY, C, 2, null, 240),
        Kind("Черника", "Blueberries", Cat.BERRY, C, 7, null, 240),
        Kind("Вишня", "Cherries", Cat.BERRY, C, 5, null, 240),
        Kind("Замороженные ягоды", "Frozen berries", Cat.BERRY, I, 240),
        // Bread
        Kind("Хлеб", "Bread", Cat.BREAD, P, 4, null, 90),
        Kind("Батон", "Loaf", Cat.BREAD, P, 3, null, 90),
        Kind("Лаваш", "Flatbread", Cat.BREAD, P, 5, 3),
        Kind("Тортильи", "Tortillas", Cat.BREAD, P, 14, 5),
        Kind("Тесто", "Dough", Cat.BREAD, C, 3, null, 90),
        // Grains
        Kind("Рис", "Rice", Cat.GRAIN, P, 540),
        Kind("Гречка", "Buckwheat", Cat.GRAIN, P, 540),
        Kind("Макароны", "Pasta", Cat.GRAIN, P, 730),
        Kind("Овсянка", "Oats", Cat.GRAIN, P, 365),
        Kind("Мука", "Flour", Cat.GRAIN, P, 365),
        Kind("Хлопья", "Cereal", Cat.GRAIN, P, 180, 60),
        Kind("Булгур", "Bulgur", Cat.GRAIN, P, 365),
        Kind("Чечевица", "Lentils", Cat.GRAIN, P, 540),
        // Sauces and jars
        Kind("Кетчуп", "Ketchup", Cat.SAUCE, P, 365, 30),
        Kind("Майонез", "Mayonnaise", Cat.SAUCE, P, 90, 30),
        Kind("Горчица", "Mustard", Cat.SAUCE, P, 180, 60),
        Kind("Соевый соус", "Soy sauce", Cat.SAUCE, P, 730, 180),
        Kind("Песто", "Pesto", Cat.SAUCE, C, 60, 5),
        Kind("Томатная паста", "Tomato paste", Cat.SAUCE, P, 365, 5),
        Kind("Хумус", "Hummus", Cat.SAUCE, C, 14, 4),
        Kind("Оливки", "Olives", Cat.SAUCE, P, 730, 14),
        Kind("Солёные огурцы", "Pickles", Cat.SAUCE, P, 365, 30),
        Kind("Варенье", "Jam", Cat.SWEET, P, 365, 30),
        Kind("Мёд", "Honey", Cat.SWEET, P, null),
        // Sweets
        Kind("Шоколад", "Chocolate", Cat.SWEET, P, 365),
        Kind("Печенье", "Biscuits", Cat.SWEET, P, 120, 30),
        Kind("Торт", "Cake", Cat.SWEET, C, 3),
        Kind("Пирожные", "Pastries", Cat.SWEET, C, 2),
        Kind("Мороженое", "Ice cream", Cat.SWEET, I, 90),
        // Drinks
        Kind("Сок", "Juice", Cat.DRINK, P, 180, 4),
        Kind("Морс", "Berry drink", Cat.DRINK, C, 7, 3),
        Kind("Квас", "Kvass", Cat.DRINK, C, 14, 2),
        Kind("Вино", "Wine", Cat.DRINK, P, null, 4),
        Kind("Тофу", "Tofu", Cat.OTHER, C, 30, 3, 90),
        // Leftovers
        Kind("Суп", "Soup", Cat.LEFTOVER, C, 3, null, 90),
        Kind("Борщ", "Borscht", Cat.LEFTOVER, C, 4, null, 90),
        Kind("Котлеты", "Patties", Cat.LEFTOVER, C, 3, null, 90),
        Kind("Плов", "Pilaf", Cat.LEFTOVER, C, 3),
        Kind("Гарнир", "Side dish", Cat.LEFTOVER, C, 3),
        Kind("Каша", "Porridge", Cat.LEFTOVER, C, 2),
        Kind("Заправленный салат", "Dressed salad", Cat.LEFTOVER, C, 1),
        Kind("Пицца", "Pizza", Cat.LEFTOVER, C, 3, null, 60),
        Kind("Рагу", "Stew", Cat.LEFTOVER, C, 3, null, 90),
        // Medicines
        Kind("Парацетамол", "Paracetamol", Cat.MEDS, M, 1095, need = Need.FEVER),
        Kind("Детский жаропонижающий сироп", "Children's fever syrup", Cat.MEDS, M, 730, 180, need = Need.FEVER),
        Kind("Ибупрофен", "Ibuprofen", Cat.MEDS, M, 1095, need = Need.PAIN),
        Kind("Хлоргексидин", "Chlorhexidine", Cat.MEDS, M, 1095, need = Need.ANTISEPTIC),
        Kind("Перекись водорода", "Hydrogen peroxide", Cat.MEDS, M, 730, 30, need = Need.ANTISEPTIC),
        Kind("Йод", "Iodine", Cat.MEDS, M, 1825, need = Need.ANTISEPTIC),
        Kind("Бинт", "Bandage", Cat.MEDS, M, 1825, need = Need.BANDAGE),
        Kind("Пластыри", "Plasters", Cat.MEDS, M, 1825, need = Need.BANDAGE),
        Kind("Активированный уголь", "Activated charcoal", Cat.MEDS, M, 1095, need = Need.SORBENT),
        Kind("Сорбент в порошке", "Sorbent powder", Cat.MEDS, M, 1095, need = Need.SORBENT),
        Kind("Лоратадин", "Loratadine", Cat.MEDS, M, 1095, need = Need.ALLERGY),
        Kind("Цетиризин", "Cetirizine", Cat.MEDS, M, 1095, need = Need.ALLERGY),
        Kind("Средство от изжоги", "Antacid", Cat.MEDS, M, 1095, need = Need.STOMACH),
        Kind("Термометр", "Thermometer", Cat.MEDS, M, null, need = Need.THERMOMETER),
        Kind("Глазные капли", "Eye drops", Cat.MEDS, M, 730, 28),
        Kind("Капли в нос", "Nasal spray", Cat.MEDS, M, 730, 30),
        Kind("Сироп от кашля", "Cough syrup", Cat.MEDS, M, 730, 30),
        Kind("Мазь", "Ointment", Cat.MEDS, M, 730, 90),
        Kind("Инсулин", "Insulin", Cat.MEDS, C, 730, 28),
        // Vitamins
        Kind("Витамин D", "Vitamin D", Cat.VITAMIN, M, 730, 365),
        Kind("Витамин C", "Vitamin C", Cat.VITAMIN, M, 730, 365),
        Kind("Магний", "Magnesium", Cat.VITAMIN, M, 730, 365),
        Kind("Омега-3", "Omega-3", Cat.VITAMIN, M, 730, 90),
        Kind("Железо", "Iron", Cat.VITAMIN, M, 730, 365),
        Kind("Цинк", "Zinc", Cat.VITAMIN, M, 730, 365),
        Kind("Мультивитамины", "Multivitamins", Cat.VITAMIN, M, 730, 365),
    )

    /** Known names that start with [q], in either language, best first. */
    fun find(q: String, ru: Boolean, limit: Int = 6): List<Kind> {
        val s = q.trim().lowercase()
        if (s.isEmpty()) return emptyList()
        fun score(k: Kind): Int {
            val a = k.name(ru).lowercase(); val b = k.name(!ru).lowercase()
            return when {
                a == s -> 0
                a.startsWith(s) -> 1
                a.split(' ', '-').any { it.startsWith(s) } -> 2
                b.startsWith(s) -> 3
                b.split(' ', '-').any { it.startsWith(s) } -> 4
                else -> 9
            }
        }
        return kinds.map { it to score(it) }.filter { it.second < 9 }.sortedBy { it.second }.take(limit).map { it.first }
    }

    /** The kind with exactly this name, in either language. */
    fun exact(name: String): Kind? = name.trim().lowercase().let { n -> kinds.firstOrNull { it.ru.lowercase() == n || it.en.lowercase() == n } }

    /** The usual home for a category when the name isn't known. */
    fun place(c: Cat): Place = when (c) {
        Cat.GRAIN, Cat.BREAD -> Place.PANTRY
        Cat.MEDS, Cat.VITAMIN -> Place.CABINET
        else -> Place.CELLAR
    }

    /** A safe guess at how long something unknown keeps, by category. */
    fun keep(c: Cat): Int = when (c) {
        Cat.MEAT, Cat.FISH -> 3
        Cat.LEFTOVER -> 3
        Cat.GREENS, Cat.BERRY -> 4
        Cat.DAIRY, Cat.BREAD -> 5
        Cat.VEG, Cat.FRUIT -> 7
        Cat.EGGS -> 25
        Cat.CHEESE -> 30
        Cat.SAUCE, Cat.DRINK, Cat.OTHER, Cat.SWEET -> 60
        Cat.GRAIN -> 365
        Cat.MEDS, Cat.VITAMIN -> 730
    }

    /** Ice chest default when a kind says nothing. */
    const val ICE_DAYS = 90
}
