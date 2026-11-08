package app.apothecary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** What an ingredient is. The colour is the glow of its essence in the flask. */
enum class Cat(val color: Long, val food: Boolean = true) {
    DAIRY(0xF2EBDD), CHEESE(0xF0C35A), MEAT(0xC8463F), FISH(0x4F8FC9), EGGS(0xF5D88E),
    GREENS(0x58B368), VEG(0x8DBF4A), FRUIT(0xF0953A), BERRY(0xB4407A), BREAD(0xC8955A),
    GRAIN(0xD9C08A), SAUCE(0xD8673A), SWEET(0xE78FB3), DRINK(0x9C7BD6), LEFTOVER(0xC9A66B),
    OTHER(0xA9B2AE),
    MEDS(0x7FD3C7, food = false), VITAMIN(0xF4B942, food = false),
}

/** Where it is kept in the shop. */
enum class Place { CELLAR, PANTRY, ICE, CABINET }

/** How an ingredient left the shop. */
enum class Fate { EATEN, GIVEN, TOSSED, RETURNED }

/** What a medicine cabinet should always hold. */
enum class Need { FEVER, PAIN, ANTISEPTIC, BANDAGE, SORBENT, ALLERGY, STOMACH, THERMOMETER }

/**
 * One ingredient on a shelf. Days are epoch days.
 * The deadline is the printed date or, once the seal is broken, [opened] + [openDays], whichever comes first.
 * In the ice chest it is [frozen] + [iceDays] instead.
 */
@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val cat: Cat,
    val place: Place,
    val bought: Long,
    /** The date on the package; null when there is none (salt, honey, a thermometer). */
    val printed: Long? = null,
    val opened: Long? = null,
    /** How long it keeps once open; null = the opening doesn't matter. */
    val openDays: Int? = null,
    val frozen: Long? = null,
    val iceDays: Int? = null,
    /** In kopecks or cents; 0 = unknown. */
    val price: Int = 0,
    /** The number written on a leftover's lid; 0 = none. */
    val jar: Int = 0,
    val need: Need? = null,
    /** A course of vitamins: so many a day, [left] at [courseFrom]. */
    val perDay: Int = 0,
    val left: Int = 0,
    val courseFrom: Long? = null,
    val fate: Fate? = null,
    val closed: Long? = null,
    val brew: Long? = null,
    val added: Long = System.currentTimeMillis(),
    /** How much of it, in [measure]; 0 = not said. */
    val amount: Double = 0.0,
    val measure: Measure? = null,
)

/** Units an amount can be given in (#1). */
enum class Measure { PCS, G, KG, ML, L, TABS, PACKS }

/** A meal cooked from ingredients: in the shop it is a potion. */
@Entity(tableName = "brews")
data class Brew(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val day: Long,
    val at: Long,
    /** The recipe key in the book. */
    val potion: String,
    /** Ingredient names, one per line. */
    val names: String,
    /** Categories used, comma separated. */
    val cats: String,
    /** The ingredient colours blended, as ARGB. */
    val color: Int,
    /** Customers served by it, comma separated. */
    val orders: String = "",
    val coins: Int = 0,
    /** How many ingredients were saved on their last day or two. */
    val rescued: Int = 0,
)
