package app.apothecary.ui.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import app.apothecary.BuildConfig
import app.apothecary.container
import app.apothecary.data.Need
import app.apothecary.domain.Shop
import app.apothecary.domain.Words
import app.apothecary.notify.Bell
import app.apothecary.ui.LocalRu
import app.apothecary.ui.Nav
import app.apothecary.ui.components.Btn
import app.apothecary.ui.components.Card
import app.apothecary.ui.components.Chip
import app.apothecary.ui.components.Flow
import app.apothecary.ui.components.Page
import app.apothecary.ui.components.Section
import app.apothecary.ui.components.t
import app.apothecary.ui.theme.C
import app.apothecary.ui.theme.T

@Composable
fun SettingsPage(shop: Shop, nav: Nav) {
    val ru = LocalRu.current
    val ctx = LocalContext.current
    val c = ctx.container
    val s = shop.s
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    Page(t("Настройки", "Settings"), nav::close) {
        Section(t("Утренний колокольчик", "Morning bell"))
        Card {
            Text(t("Раз в день колокольчик скажет, у чего сегодня и завтра последний день, а в шкафу — что истекает через месяц или неделю. Если ничего не горит, он молчит.",
                "Once a day the bell says what's on its last day today and tomorrow, and what in the cabinet expires in a month or a week. When nothing is due, it stays quiet."), style = T.Muted)
            Flow(Modifier.padding(top = 8.dp)) {
                Chip(t("выключен", "off"), s.bell < 0, { c.write { settings.update { it.copy(bell = -1) } } })
                for (h in listOf(7, 8, 9, 10, 12, 18, 20)) Chip("$h:00", s.bell == h, { c.write { settings.update { it.copy(bell = h, rung = -1) } } })
            }
            if (s.bell >= 0 && !Bell.canNotify(ctx) && Build.VERSION.SDK_INT >= 33)
                Btn(t("Разрешить уведомления", "Allow notifications"), { ask.launch(Manifest.permission.POST_NOTIFICATIONS) }, Modifier.padding(top = 8.dp), small = true)
        }

        Section(t("Набор гильдии для аптечки", "The guild's kit for the medicine box"))
        Card {
            Text(t("Что должно быть в шкафу всегда. Гильдия проверяет, есть ли это и не истёк ли срок.", "What the cabinet should always hold. The guild checks it's there and in date."), style = T.Muted)
            Flow(Modifier.padding(top = 8.dp)) {
                for (n in Need.entries) Chip(Words.need(n, ru), n in s.needs, { c.write { settings.update { it.copy(needs = if (n in it.needs) it.needs - n else it.needs + n) } } })
            }
        }

        Section(t("Язык", "Language"))
        Flow {
            val cur = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            Chip(t("как в телефоне", "as the phone"), cur.isEmpty(), { AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList()) })
            Chip("Русский", cur.startsWith("ru"), { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ru")) })
            Chip("English", cur.startsWith("en"), { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en")) })
        }

        Section(t("Сообщество", "Community"))
        Card {
            fun open(u: String) = runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(u)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            for ((label, url) in listOf(
                t("Чат StillCraft в Telegram", "StillCraft chat on Telegram") to "https://t.me/+Q4gFEDk8O842OGVi",
                t("Сообщить об ошибке", "Report a bug") to "https://t.me/ArcaneWhisperBot?start=bug_apothecary",
                t("Предложить идею", "Suggest an idea") to "https://t.me/ArcaneWhisperBot?start=wish_apothecary",
                t("Следить за обновлениями", "Follow updates") to "https://t.me/ArcaneWhisperBot?start=f_apothecary",
                t("Исходный код", "Source code") to "https://github.com/bigdevwhale/apothecary",
            )) Text(label, style = T.Body.copy(color = C.amber), modifier = Modifier.fillMaxWidth().clickable { open(url) }.padding(vertical = 8.dp))
        }

        Section(t("Как это устроено", "How it works"))
        Card {
            Text(t("Срок ингредиента — дата на упаковке или «вскрыто + сколько дней годно открытым», что наступит раньше. В ледяном ларе часы идут заново и медленнее. Сроки в справочнике — обычные бытовые рекомендации; на упаковке всегда виднее. Всё хранится только на телефоне, интернет не нужен.",
                "An ingredient's deadline is the package date or “opened + days it keeps open”, whichever comes first. In the ice chest the clock restarts, slower. The guide's numbers are ordinary household advice; the package always knows better. Everything stays on the phone; no internet needed."), style = T.Muted)
        }
        Text("${t("Зельевар", "Apothecary")} ${BuildConfig.VERSION_NAME}", style = T.Small, modifier = Modifier.padding(bottom = 20.dp))
    }
}
