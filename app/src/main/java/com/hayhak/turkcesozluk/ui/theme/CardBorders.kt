package com.hayhak.turkcesozluk.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Mor → mavi gradyan çerçeve; WifiGuard'daki kartların imza görünümü. */
fun appCardBorder(
    start: Color = BrandPurple,
    end: Color = BrandBlue,
): BorderStroke = BorderStroke(
    width = 1.5.dp,
    brush = Brush.linearGradient(listOf(start, end))
)
