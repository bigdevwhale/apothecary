package app.apothecary.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.apothecary.container
import app.apothecary.data.Cat
import app.apothecary.domain.Glow
import app.apothecary.ui.art.Flask
import app.apothecary.ui.components.Btn
import app.apothecary.ui.components.t
import app.apothecary.ui.theme.C
import app.apothecary.ui.theme.T

@Composable
fun Onboarding() {
    val c = LocalContext.current.container
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(C.bg, C.surface, C.bg))).statusBarsPadding().navigationBarsPadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
            Flask(C.cat(Cat.BERRY), .8f, Glow.BRIGHT, 70.dp, 3, 1)
            Flask(C.cat(Cat.FRUIT), .95f, Glow.BRIGHT, 110.dp, 0, 2)
            Flask(C.cat(Cat.GREENS), .5f, Glow.SOON, 84.dp, 1, 3)
        }
        Text(t("Зельевар", "Apothecary"), style = T.Display.copy(fontSize = T.Display.fontSize * 1.3f, color = C.amber))
        Text(t("Ваш холодильник — маленькая лавка зелий.", "Your fridge is a small potion shop."), style = T.Italic, textAlign = TextAlign.Center)
        Box(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Point(t("Каждый продукт — колба с эссенцией. Она светится, пока продукт свеж, и тускнеет к концу срока.", "Every product is a bottle of essence. It glows while fresh and dims as its date nears."))
                Point(t("Срок — дата на упаковке или «вскрыто + сколько дней годно», что раньше. Морозилка — ледяной ларь, там время застывает.", "The deadline is the package date or “opened + days it keeps”, whichever is first. The freezer is the ice chest, where time stands still."))
                Point(t("Приготовили еду — сварили зелье. Сочетания открывают рецепты в книге, покупатели приходят за тем, что вот-вот пропадёт.", "Cook a meal and you brew a potion. Combinations unlock recipes, and customers come for what's about to be lost."))
                Point(t("Аптечка — шкаф за прилавком: лекарства, витамины и набор, который должен быть всегда.", "The medicine box is the cabinet behind the counter: medicines, vitamins and a kit that should always be there."))
            }
        }
        Btn(t("Открыть лавку", "Open the shop"), {
            if (Build.VERSION.SDK_INT >= 33) ask.launch(Manifest.permission.POST_NOTIFICATIONS)
            c.write { settings.update { it.copy(onboarded = true) } }
        }, Modifier.fillMaxWidth().padding(top = 8.dp))
    }
}

@Composable
private fun Point(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("✦", style = T.Body.copy(color = C.amber))
        Text(text, style = T.Body)
    }
}
