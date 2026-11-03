package app.apothecary.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.apothecary.R
import app.apothecary.data.Cat

/**
 * A potion shop after dark: walnut shelves, candlelight amber, copper and old glass.
 * Cards are a shade lighter wood; labels are cream paper.
 */
@Immutable
object C {
    val bg = Color(0xFF1A1210)
    val surface = Color(0xFF261A15)
    val surface2 = Color(0xFF33241C)
    val line = Color(0xFF4A3528)
    val ink = Color(0xFFF4E7D0)
    val muted = Color(0xFFC2AD94)
    val faint = Color(0xFF8E7A66)
    val amber = Color(0xFFF0B45A)
    val amberSoft = Color(0xFF3D2C17)
    val onAmber = Color(0xFF241606)
    val copper = Color(0xFFD07A44)
    val moss = Color(0xFF8DBF6A)
    val rose = Color(0xFFE0645A)
    val murky = Color(0xFF6E7556)
    val paper = Color(0xFFF2E3C2)
    val paperDark = Color(0xFFDCC69B)
    val paperInk = Color(0xFF2E1E14)
    val paperMuted = Color(0xFF7A604A)
    val glass = Color(0xFFE9DFC9)

    fun cat(c: Cat) = Color(0xFF000000 or c.color)
}

/** Cormorant: the lettering of an old shop sign and a potion book. */
val Cormorant = FontFamily(
    Font(R.font.cormorant_semibold, FontWeight.SemiBold),
    Font(R.font.cormorant_bold, FontWeight.Bold),
    Font(R.font.cormorant_italic, FontWeight.Normal, FontStyle.Italic),
)

val Sans = FontFamily(
    Font(R.font.alegreya_sans, FontWeight.Normal),
    Font(R.font.alegreya_sans_medium, FontWeight.Medium),
    Font(R.font.alegreya_sans_bold, FontWeight.Bold),
)

object R8 {
    val Xl = RoundedCornerShape(26.dp)
    val Lg = RoundedCornerShape(20.dp)
    val Md = RoundedCornerShape(14.dp)
    val Sm = RoundedCornerShape(10.dp)
}

object T {
    val Display = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 38.sp, color = C.ink)
    val Title = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 30.sp, color = C.ink)
    val Name = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 23.sp, color = C.ink)
    val Italic = TextStyle(fontFamily = Cormorant, fontStyle = FontStyle.Italic, fontSize = 18.sp, lineHeight = 22.sp, color = C.muted)
    val Eyebrow = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.14.em, color = C.faint)
    val Strong = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 21.sp, color = C.ink)
    val Body = TextStyle(fontFamily = Sans, fontSize = 16.sp, lineHeight = 22.sp, color = C.ink)
    val Muted = TextStyle(fontFamily = Sans, fontSize = 14.5.sp, lineHeight = 20.sp, color = C.muted)
    val Small = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 17.sp, color = C.faint)
    val Button = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    val Number = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = C.amber, fontFeatureSettings = "lnum")
    val Label = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 16.sp, color = C.paperInk)
    val Say = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp, color = C.paperInk)
}

@Composable
fun ApothecaryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = C.amber, onPrimary = C.onAmber, background = C.bg, surface = C.surface, onSurface = C.ink,
            onBackground = C.ink, surfaceContainer = C.surface, surfaceContainerHigh = C.surface2, surfaceContainerHighest = C.surface2,
            outline = C.line, error = C.rose, secondary = C.copper, onSurfaceVariant = C.muted,
        ),
        typography = MaterialTheme.typography.copy(bodyLarge = T.Body, bodyMedium = T.Body, bodySmall = T.Small, labelLarge = T.Button, titleLarge = T.Title),
        content = content,
    )
}
