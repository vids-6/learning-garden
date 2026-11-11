package org.rural.learn.feature.design
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.*
import org.rural.learn.R
import androidx.compose.ui.graphics.Color
@Composable fun GardenTheme(content: @Composable () -> Unit) {
 val family=FontFamily(Font(R.font.noto_devanagari_regular,FontWeight.Normal),Font(R.font.noto_devanagari_bold,FontWeight.Bold))
 val base=Typography()
 val type=base.copy(displayLarge=base.displayLarge.copy(fontFamily=family),displayMedium=base.displayMedium.copy(fontFamily=family),displaySmall=base.displaySmall.copy(fontFamily=family),headlineLarge=base.headlineLarge.copy(fontFamily=family),headlineMedium=base.headlineMedium.copy(fontFamily=family),headlineSmall=base.headlineSmall.copy(fontFamily=family),titleLarge=base.titleLarge.copy(fontFamily=family),titleMedium=base.titleMedium.copy(fontFamily=family),titleSmall=base.titleSmall.copy(fontFamily=family),bodyLarge=base.bodyLarge.copy(fontFamily=family),bodyMedium=base.bodyMedium.copy(fontFamily=family),bodySmall=base.bodySmall.copy(fontFamily=family),labelLarge=base.labelLarge.copy(fontFamily=family),labelMedium=base.labelMedium.copy(fontFamily=family),labelSmall=base.labelSmall.copy(fontFamily=family))
 MaterialTheme(typography=type,colorScheme=lightColorScheme(primary=Color(0xFF245D42),onPrimary=Color.White,secondary=Color(0xFF75501A),background=Color(0xFFF6F7EE),surface=Color(0xFFF6F7EE),onSurface=Color(0xFF182A20),surfaceVariant=Color(0xFFE3EBDD),onSurfaceVariant=Color(0xFF263D2D),error=Color(0xFF9B2424)),content=content)
}
