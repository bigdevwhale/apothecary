package app.apothecary.notify

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.apothecary.MainActivity
import app.apothecary.R
import app.apothecary.container
import app.apothecary.data.Place
import app.apothecary.domain.Shop
import app.apothecary.domain.Words
import app.apothecary.localized
import app.apothecary.ru
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * The bell over the shop door: once a morning it says what loses its strength today and tomorrow,
 * and when something in the cabinet is a month or a week from its date. Silent when nothing is due.
 */
object Bell {
    private const val CHANNEL = "bell_v1"
    private const val ACTION = "app.apothecary.notify.BELL"

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL) != null) return
        val l = context.localized()
        nm.createNotificationChannel(NotificationChannel(CHANNEL, l.getString(R.string.channel_bell), NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = l.getString(R.string.channel_bell_desc)
        })
    }

    private fun pi(context: Context) = PendingIntent.getBroadcast(
        context, 1, Intent(context, BellReceiver::class.java).setAction(ACTION),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun at(day: Long, hour: Int): Long = LocalDate.ofEpochDay(day).atTime(hour, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    suspend fun reschedule(context: Context) {
        val am = context.getSystemService(AlarmManager::class.java)
        val p = pi(context)
        val s = context.container.settings.now()
        if (!s.onboarded || s.bell < 0) { am.cancel(p); return }
        val today = Shop.today()
        val now = System.currentTimeMillis()
        val day = if (s.rung < today && at(today, s.bell) > now - 3 * 3_600_000L) today else today + 1
        // A bell needn't ring to the second; this avoids the exact-alarm permission.
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, maxOf(at(day, s.bell), now + 5_000), p)
    }

    suspend fun ring(context: Context) {
        val c = context.container
        val s = c.settings.now()
        val today = Shop.today()
        if (!s.onboarded || s.rung >= today) return
        c.settings.update { it.copy(rung = today) }
        if (!canNotify(context)) return
        val stock = c.db.dao().stock()
        val l = context.localized()
        val ru = l.ru
        val food = stock.filter { it.place != Place.CABINET && it.cat.food }
        val now = food.filter { Shop.left(it, today) == 0L }.map { it.name }
        val tomorrow = food.filter { Shop.left(it, today) == 1L }.map { it.name }
        val cab = stock.filter { !(it.place != Place.CABINET && it.cat.food) && Shop.left(it, today) in setOf(0L, 7L, 30L) }
        val lines = buildList {
            if (now.isNotEmpty()) add((if (ru) "Сегодня последний день: " else "Last day today: ") + Words.list(now, ru))
            if (tomorrow.isNotEmpty()) add((if (ru) "Завтра: " else "Tomorrow: ") + Words.list(tomorrow, ru))
            for (i in cab) add(if (ru) "В шкафу: ${i.name}, ${Words.left(Shop.left(i, today), true)}" else "In the cabinet: ${i.name}, ${Words.left(Shop.left(i, today), false)}")
        }
        if (lines.isEmpty()) return
        val title = if (now.isNotEmpty()) l.getString(R.string.bell_title_today) else l.getString(R.string.bell_title)
        val text = lines.joinToString("\n")
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.amber))
            .setContentTitle(title)
            .setContentText(lines.first())
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(PendingIntent.getActivity(context, 100, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            .setAutoCancel(true)
            .build()
        try { NotificationManagerCompat.from(context).notify(10, n) } catch (_: SecurityException) {}
    }

    internal const val RING = ACTION
}

private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

class BellReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Bell.RING) return
        val pending = goAsync()
        receiverScope.launch {
            try { Bell.ring(context); Bell.reschedule(context) } finally { pending.finish() }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    private val actions = setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in actions) return
        val pending = goAsync()
        receiverScope.launch {
            try { Bell.reschedule(context) } finally { pending.finish() }
        }
    }
}
