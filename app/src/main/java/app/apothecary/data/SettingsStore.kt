package app.apothecary.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

/** A customer's request for the day: who came and which essences they need. */
data class Order(val who: Customer, val cats: List<Cat>) {
    fun key() = "${who.name}:${cats.joinToString("+") { it.name }}"

    companion object {
        fun of(s: String): Order? = runCatching {
            val (w, c) = s.split(':')
            Order(Customer.valueOf(w), c.split('+').map { Cat.valueOf(it) })
        }.getOrNull()
    }
}

enum class Customer { HERBALIST, MILLER, KNIGHT, CLOCKMAKER, BARD, FERRYWOMAN }

data class Settings(
    val onboarded: Boolean = false,
    /** The shop's name on the sign; empty = none yet. */
    val shop: String = "",
    /** Coins spent on the shop. */
    val spent: Int = 0,
    val bought: Set<String> = emptySet(),
    /** What the cabinet should hold. */
    val needs: Set<Need> = Need.entries.toSet(),
    /** Hour of the morning bell; -1 = off. */
    val bell: Int = 9,
    /** The day [orders] were written for. */
    val ordersDay: Long = -1,
    val orders: List<Order> = emptyList(),
    /** Days the bell has already rung for. */
    val rung: Long = -1,
    /** The last day the night audit was done. */
    val audit: Long = -1,
)

class SettingsStore(context: Context) {
    private val store = context.applicationContext.dataStore

    val settings: Flow<Settings> = store.data.map { it.read() }

    private fun Preferences.read() = Settings(
        onboarded = this[ONBOARDED] ?: false,
        shop = this[SHOP] ?: "",
        spent = this[SPENT] ?: 0,
        bought = this[BOUGHT]?.split(',')?.filter { it.isNotBlank() }?.toSet() ?: emptySet(),
        needs = this[NEEDS]?.let { s -> s.split(',').mapNotNull { n -> runCatching { Need.valueOf(n) }.getOrNull() }.toSet() } ?: Need.entries.toSet(),
        bell = this[BELL] ?: 9,
        ordersDay = this[ORDERS_DAY] ?: -1,
        orders = this[ORDERS]?.split(';')?.mapNotNull { Order.of(it) } ?: emptyList(),
        rung = this[RUNG] ?: -1,
        audit = this[AUDIT] ?: -1,
    )

    suspend fun now(): Settings = settings.first()

    suspend fun update(change: (Settings) -> Settings) {
        store.edit {
            val s = change(it.read())
            it[ONBOARDED] = s.onboarded
            it[SHOP] = s.shop
            it[SPENT] = s.spent
            it[BOUGHT] = s.bought.sorted().joinToString(",")
            it[NEEDS] = s.needs.map { n -> n.name }.sorted().joinToString(",")
            it[BELL] = s.bell
            it[ORDERS_DAY] = s.ordersDay
            it[ORDERS] = s.orders.joinToString(";") { o -> o.key() }
            it[RUNG] = s.rung
            it[AUDIT] = s.audit
        }
    }

    private companion object {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val SHOP = stringPreferencesKey("shop")
        val SPENT = intPreferencesKey("spent")
        val BOUGHT = stringPreferencesKey("bought")
        val NEEDS = stringPreferencesKey("needs")
        val BELL = intPreferencesKey("bell")
        val ORDERS_DAY = longPreferencesKey("orders_day")
        val ORDERS = stringPreferencesKey("orders")
        val RUNG = longPreferencesKey("rung")
        val AUDIT = longPreferencesKey("audit")
    }
}
