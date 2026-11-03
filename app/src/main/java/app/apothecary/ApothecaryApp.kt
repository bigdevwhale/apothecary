package app.apothecary

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import app.apothecary.data.Clerk
import app.apothecary.data.DemoSeed
import app.apothecary.data.SettingsStore
import app.apothecary.data.ShopDb
import app.apothecary.notify.Bell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    val app: Context = context.applicationContext
    val db = ShopDb.create(app)
    val settings = SettingsStore(app)
    val clerk = Clerk(db.dao(), settings)
    /** On the main thread: Room and DataStore are main-safe, and screens' state is written from here. */
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** True while the demo build fills its shop, so the first screen doesn't jump. */
    val seeding = MutableStateFlow(BuildConfig.DEMO)

    /** Fire-and-forget so a write survives the screen being closed right after tapping; the bell is re-aimed after. */
    fun write(block: suspend AppContainer.() -> Unit) {
        scope.launch { block(); Bell.reschedule(app) }
    }
}

class ApothecaryApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Bell.ensureChannel(this)
        container.scope.launch {
            if (BuildConfig.DEMO) try { DemoSeed.seedIfEmpty(this@ApothecaryApp, container) } finally { container.seeding.value = false }
            Bell.reschedule(this@ApothecaryApp)
        }
    }
}

val Context.container: AppContainer get() = (applicationContext as ApothecaryApp).container

val Context.ru: Boolean get() = resources.configuration.locales[0].language == "ru"

/** A context in the app's own language, for notifications built outside an activity. */
fun Context.localized(): Context {
    val tags = AppCompatDelegate.getApplicationLocales()
    if (tags.isEmpty) return this
    val config = Configuration(resources.configuration)
    config.setLocale(tags[0])
    return createConfigurationContext(config)
}
