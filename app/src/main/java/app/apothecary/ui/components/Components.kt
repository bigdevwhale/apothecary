package app.apothecary.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.apothecary.ui.LocalRu
import app.apothecary.ui.theme.C
import app.apothecary.ui.theme.R8
import app.apothecary.ui.theme.T

/** Russian or English, by the phone's (or the app's) language. */
@Composable
fun t(ru: String, en: String): String = if (LocalRu.current) ru else en

@Composable
fun Card(modifier: Modifier = Modifier, fill: Color = C.surface, onClick: (() -> Unit)? = null, padding: PaddingValues = PaddingValues(16.dp), content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.clip(R8.Lg).background(fill).border(1.dp, C.line, R8.Lg)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(padding),
        content = content,
    )
}

enum class Kind { AMBER, COPPER, SOFT, GHOST, DANGER }

@Composable
fun Btn(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, kind: Kind = Kind.AMBER, enabled: Boolean = true, small: Boolean = false) {
    val (fill, ink) = when (kind) {
        Kind.AMBER -> C.amber to C.onAmber
        Kind.COPPER -> C.copper to C.onAmber
        Kind.SOFT -> C.surface2 to C.ink
        Kind.GHOST -> Color.Transparent to C.ink
        Kind.DANGER -> Color.Transparent to C.rose
    }
    val a = if (enabled) 1f else .4f
    Box(
        modifier.clip(CircleShape).background(fill.copy(alpha = fill.alpha * a))
            .then(if (kind == Kind.GHOST || kind == Kind.DANGER) Modifier.border(1.dp, if (kind == Kind.DANGER) C.rose.copy(alpha = .5f) else C.line, CircleShape) else Modifier)
            .clickable(enabled = enabled, onClick = onClick)
            .heightIn(min = if (small) 36.dp else 48.dp)
            .padding(horizontal = if (small) 14.dp else 20.dp, vertical = if (small) 7.dp else 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = (if (small) T.Button.copy(fontSize = T.Button.fontSize * .9f) else T.Button).copy(color = ink.copy(alpha = a)), textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
fun Chip(text: String, on: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color? = null) {
    val fill = if (on) color ?: C.amber else C.surface2
    val ink = if (on) C.onAmber else C.muted
    Text(text, style = T.Small.copy(color = ink, fontSize = T.Small.fontSize * 1.1f), maxLines = 1,
        modifier = modifier.clip(CircleShape).background(fill).clickable(onClick = onClick).padding(horizontal = 13.dp, vertical = 8.dp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Flow(modifier: Modifier = Modifier, gap: Dp = 6.dp, content: @Composable FlowRowScope.() -> Unit) =
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(gap), verticalArrangement = Arrangement.spacedBy(gap), content = content)

@Composable
fun Field(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, style: TextStyle = T.Body, single: Boolean = true, number: Boolean = false) {
    BasicTextField(value, onChange, modifier.fillMaxWidth().clip(R8.Md).background(C.surface2).border(1.dp, C.line, R8.Md), textStyle = style, singleLine = single, cursorBrush = SolidColor(C.amber),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = if (number) KeyboardType.Number else KeyboardType.Text, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(),
        decorationBox = { inner -> Box(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) { if (value.isEmpty()) Text(hint, style = style.copy(color = C.faint)); inner() } })
}

/** A sheet from the bottom over a dim layer; tap outside, swipe down or back closes it. */
@Composable
fun Sheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    BackHandler(onBack = onDismiss)
    val close = t("Закрыть", "Close")
    Box(Modifier.fillMaxSize().background(Color(0xC00A0604)).clickable(remember { MutableInteractionSource() }, null, onClick = onDismiss).imePadding(), contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier.fillMaxWidth().widthIn(max = 560.dp).heightIn(max = 800.dp)
                .clip(R8.Xl).background(C.surface)
                .clickable(remember { MutableInteractionSource() }, null) {}
                .pointerInput(Unit) { var dy = 0f; detectVerticalDragGestures(onDragStart = { dy = 0f }, onDragEnd = { if (dy > 120) onDismiss() }) { _, d -> dy += d } }
                .navigationBarsPadding(),
        ) {
            Box(Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Box(Modifier.align(Alignment.TopCenter).width(40.dp).height(4.dp).clip(CircleShape).background(C.line))
                Box(Modifier.align(Alignment.TopEnd).padding(end = 8.dp).size(40.dp).clip(CircleShape).clickable(onClick = onDismiss).semantics { contentDescription = close }, contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(14.dp)) {
                        val w = 2.dp.toPx()
                        drawLine(C.muted, Offset.Zero, Offset(size.width, size.height), w, StrokeCap.Round)
                        drawLine(C.muted, Offset(size.width, 0f), Offset(0f, size.height), w, StrokeCap.Round)
                    }
                }
            }
            Column(Modifier.verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
        }
    }
}

/** A full-screen page over the tabs with a back arrow. */
@Composable
fun Page(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize().background(C.bg).clickable(remember { MutableInteractionSource() }, null) {}.imePadding()) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 6.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(18.dp)) {
                    val w = 2.2.dp.toPx(); val m = size.height / 2
                    drawLine(C.ink, Offset(size.width * .65f, 0f), Offset(size.width * .2f, m), w, StrokeCap.Round)
                    drawLine(C.ink, Offset(size.width * .2f, m), Offset(size.width * .65f, size.height), w, StrokeCap.Round)
                }
            }
            Spacer(Modifier.width(4.dp))
            Text(title, style = T.Title, modifier = Modifier.weight(1f), maxLines = 1)
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 8.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
    }
}

@Composable
fun Gap(h: Dp = 8.dp) = Spacer(Modifier.height(h))

/** A section header in small amber capitals, an optional note on the right. */
@Composable
fun Section(text: String, note: String? = null, modifier: Modifier = Modifier, onNote: (() -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(top = 10.dp, bottom = 2.dp), verticalAlignment = Alignment.Bottom) {
        Text(text.uppercase(), style = T.Eyebrow.copy(color = C.amber), modifier = Modifier.weight(1f))
        if (note != null) Text(note, style = T.Small.copy(color = if (onNote != null) C.amber else C.faint), modifier = if (onNote != null) Modifier.clip(CircleShape).clickable(onClick = onNote).padding(horizontal = 6.dp, vertical = 2.dp) else Modifier)
    }
}

/** A note on cream paper: a customer's words, the cat's remark. */
@Composable
fun Note(text: String, who: String, modifier: Modifier = Modifier, edge: Color = C.copper) {
    Column(modifier.fillMaxWidth().clip(R8.Md).background(C.paper).border(2.dp, edge, R8.Md).padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(who.uppercase(), style = T.Eyebrow.copy(color = edge))
        Text(text, style = T.Say)
    }
}

/** A thin progress bar. */
@Composable
fun Bar(p: Float, modifier: Modifier = Modifier, color: Color = C.amber) {
    Box(modifier.height(6.dp).clip(CircleShape).background(C.surface2)) {
        Box(Modifier.fillMaxWidth(p.coerceIn(0f, 1f)).height(6.dp).clip(CircleShape).background(color))
    }
}

/** A short coloured label: a category, a state. */
@Composable
fun Tag(text: String, color: Color, modifier: Modifier = Modifier, ink: Color = C.onAmber) {
    Text(text, style = T.Small.copy(color = ink), maxLines = 1, modifier = modifier.clip(CircleShape).background(color).padding(horizontal = 10.dp, vertical = 3.dp))
}
