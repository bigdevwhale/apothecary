package app.apothecary.domain

import app.apothecary.data.Cat
import app.apothecary.data.Need
import app.apothecary.data.Place
import java.time.LocalDate

/** Names and phrases in both languages: Russian first, English second. */
object Words {
    fun cat(c: Cat, ru: Boolean) = when (c) {
        Cat.DAIRY -> if (ru) "молочное" else "dairy"
        Cat.CHEESE -> if (ru) "сыр" else "cheese"
        Cat.MEAT -> if (ru) "мясо" else "meat"
        Cat.FISH -> if (ru) "рыба" else "fish"
        Cat.EGGS -> if (ru) "яйца" else "eggs"
        Cat.GREENS -> if (ru) "зелень" else "greens"
        Cat.VEG -> if (ru) "овощи" else "vegetables"
        Cat.FRUIT -> if (ru) "фрукты" else "fruit"
        Cat.BERRY -> if (ru) "ягоды" else "berries"
        Cat.BREAD -> if (ru) "хлеб" else "bread"
        Cat.GRAIN -> if (ru) "крупы" else "grains"
        Cat.SAUCE -> if (ru) "соусы" else "sauces"
        Cat.SWEET -> if (ru) "сладкое" else "sweets"
        Cat.DRINK -> if (ru) "напитки" else "drinks"
        Cat.LEFTOVER -> if (ru) "готовая еда" else "cooked food"
        Cat.OTHER -> if (ru) "прочее" else "other"
        Cat.MEDS -> if (ru) "лекарства" else "medicines"
        Cat.VITAMIN -> if (ru) "витамины" else "vitamins"
    }

    fun place(p: Place, ru: Boolean) = when (p) {
        Place.CELLAR -> if (ru) "Погребок" else "Cool cellar"
        Place.PANTRY -> if (ru) "Кладовая" else "Pantry"
        Place.ICE -> if (ru) "Ледяной ларь" else "Ice chest"
        Place.CABINET -> if (ru) "Шкаф за прилавком" else "Cabinet"
    }

    /** What the place is in an ordinary kitchen. */
    fun placeHint(p: Place, ru: Boolean) = when (p) {
        Place.CELLAR -> if (ru) "холодильник" else "the fridge"
        Place.PANTRY -> if (ru) "шкаф с запасами" else "the cupboard"
        Place.ICE -> if (ru) "морозилка" else "the freezer"
        Place.CABINET -> if (ru) "аптечка" else "the medicine box"
    }

    fun need(n: Need, ru: Boolean) = when (n) {
        Need.FEVER -> if (ru) "от жара" else "for fever"
        Need.PAIN -> if (ru) "от боли" else "for pain"
        Need.ANTISEPTIC -> if (ru) "антисептик" else "antiseptic"
        Need.BANDAGE -> if (ru) "бинт и пластыри" else "bandages & plasters"
        Need.SORBENT -> if (ru) "сорбент" else "sorbent"
        Need.ALLERGY -> if (ru) "от аллергии" else "for allergy"
        Need.STOMACH -> if (ru) "от изжоги" else "for heartburn"
        Need.THERMOMETER -> if (ru) "термометр" else "thermometer"
    }

    fun rank(r: Int, ru: Boolean) = when (r) {
        0 -> if (ru) "Ученик" else "Apprentice"
        1 -> if (ru) "Подмастерье" else "Journeyman"
        2 -> if (ru) "Мастер" else "Master"
        else -> if (ru) "Магистр гильдии" else "Guildmaster"
    }

    /** Russian plural: 1 день, 2 дня, 5 дней. */
    fun plural(n: Long, one: String, few: String, many: String): String {
        val a = kotlin.math.abs(n) % 100
        val b = a % 10
        return when {
            a in 11..14 -> many
            b == 1L -> one
            b in 2..4 -> few
            else -> many
        }
    }

    fun days(n: Long, ru: Boolean) = if (ru) "$n ${plural(n, "день", "дня", "дней")}" else if (n == 1L) "1 day" else "$n days"

    private val monthsRu = listOf("января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря")
    private val monthsEn = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val monthNamesRu = listOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")
    val monthNamesEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    fun date(day: Long, ru: Boolean, year: Boolean = false): String {
        val d = LocalDate.ofEpochDay(day)
        val y = if (year) " ${d.year}" else ""
        return if (ru) "${d.dayOfMonth} ${monthsRu[d.monthValue - 1]}$y" else "${d.dayOfMonth} ${monthsEn[d.monthValue - 1]}$y"
    }

    /** How much time is left, said plainly. */
    fun left(n: Long?, ru: Boolean): String = when {
        n == null -> if (ru) "без срока" else "keeps"
        n < -1 -> if (ru) "испортилось ${-n} ${plural(-n, "день", "дня", "дней")} назад" else "spoiled ${-n} days ago"
        n == -1L -> if (ru) "испортилось вчера" else "spoiled yesterday"
        n == 0L -> if (ru) "последний день" else "last day"
        n == 1L -> if (ru) "до завтра" else "until tomorrow"
        n < 60 -> if (ru) "ещё ${days(n, true)}" else "${days(n, false)} left"
        n < 730 -> if (ru) "ещё ${n / 30} ${plural(n / 30, "месяц", "месяца", "месяцев")}" else "${n / 30} months left"
        else -> if (ru) "ещё ${n / 365} ${plural(n / 365, "год", "года", "лет")}" else "${n / 365} years left"
    }

    /** Short badge: "3 д", "сегодня", "−2". */
    fun badge(n: Long?, ru: Boolean): String = when {
        n == null -> "∞"
        n < 0 -> "−${-n}"
        n == 0L -> if (ru) "сегодня" else "today"
        n < 60 -> if (ru) "$n д" else "${n}d"
        n < 730 -> if (ru) "${n / 30} мес" else "${n / 30}mo"
        else -> if (ru) "${n / 365} г" else "${n / 365}y"
    }

    fun ago(n: Long, ru: Boolean): String = when (n) {
        0L -> if (ru) "сегодня" else "today"
        1L -> if (ru) "вчера" else "yesterday"
        else -> if (ru) "${days(n, true)} назад" else "${days(n, false)} ago"
    }

    fun money(minor: Long, ru: Boolean): String {
        val whole = minor / 100
        return if (ru) "$whole ₽" else "$whole"
    }

    /** "мясо и овощи", "meat, greens and eggs". */
    fun list(xs: List<String>, ru: Boolean): String = when (xs.size) {
        0 -> ""
        1 -> xs[0]
        else -> xs.dropLast(1).joinToString(", ") + (if (ru) " и " else " and ") + xs.last()
    }
}
