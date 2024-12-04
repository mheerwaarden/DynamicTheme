/*
 * Copyright (c) 2024. Marcel van Heerwaarden
 *
 * Copyright (C) 2019 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mheerwaarden.dynamictheme.export

import androidx.compose.ui.text.decapitalize
import androidx.compose.ui.text.intl.Locale
import com.github.mheerwaarden.dynamictheme.material.color.utils.ColorExtractor
import com.github.mheerwaarden.dynamictheme.material.color.utils.ContrastLevel
import dynamiccolor.Variant
import palettes.TonalPalette

fun exportColorKotlin(name: String, sourceColorArgb: Int, schemeVariant: Variant): List<String> {
    val result = mutableListOf(
        "package com.example.dynamictheme",
        "",
        "import androidx.compose.ui.graphics.Color",
        "",
    )
    result.addAll(getColorScheme(
        objectName = "${name}Light",
        sourceColorArgb = sourceColorArgb,
        schemeVariant = schemeVariant,
        isDark = false,
        contrastLevel = ContrastLevel.Normal
    ))
    result.addAll(getColorScheme(
        objectName = "${name}LightMediumContrast",
        sourceColorArgb = sourceColorArgb,
        schemeVariant = schemeVariant,
        isDark = false,
        contrastLevel = ContrastLevel.Medium
    ))
    result.addAll(getColorScheme(
        objectName = "${name}LightHighContrast",
        sourceColorArgb = sourceColorArgb,
        schemeVariant = schemeVariant,
        isDark = false,
        contrastLevel = ContrastLevel.High
    ))
    result.addAll(getColorScheme(
        objectName = "${name}Dark",
        sourceColorArgb = sourceColorArgb,
        schemeVariant = schemeVariant,
        isDark = true,
        contrastLevel = ContrastLevel.Normal
    ))
    result.addAll(getColorScheme(
        objectName = "${name}DarkMediumContrast",
        sourceColorArgb = sourceColorArgb,
        schemeVariant = schemeVariant,
        isDark = true,
        contrastLevel = ContrastLevel.Medium
    ))
    result.addAll(getColorScheme(
        objectName = "${name}DarkHighContrast",
        sourceColorArgb = sourceColorArgb,
        schemeVariant = schemeVariant,
        isDark = true,
        contrastLevel = ContrastLevel.High
    ))
    return result
}

fun getColorScheme(
    objectName: String,
    sourceColorArgb: Int,
    schemeVariant: Variant,
    isDark: Boolean,
    contrastLevel: ContrastLevel,
): List<String> {
    val scheme = ColorExtractor.createDynamicColorScheme(
        sourceArgb = sourceColorArgb,
        schemeVariant = schemeVariant,
        isDark = isDark,
        contrastLevel = contrastLevel
    )
    val palette = TonalPalette.fromInt(scheme.primary)
    val result = mutableListOf(
        "val ${objectName.decapitalize(Locale.current)}ColorScheme by lazy { ColorScheme(",
        "    primary = Color(${scheme.primary}),",
        "    onPrimary = Color(${scheme.onPrimary}),",
        "    primaryContainer = Color(${scheme.primaryContainer}),",
        "    onPrimaryContainer = Color(${scheme.onPrimaryContainer}),",
        "    secondary = Color(${scheme.secondary}),",
        "    onSecondary = Color(${scheme.onSecondary}),",
        "    secondaryContainer = Color(${scheme.secondaryContainer}),",
        "    onSecondaryContainer = Color(${scheme.onSecondaryContainer}),",
        "    tertiary = Color(${scheme.tertiary}),",
        "    onTertiary = Color(${scheme.onTertiary}),",
        "    tertiaryContainer = Color(${scheme.tertiaryContainer}),",
        "    onTertiaryContainer = Color(${scheme.onTertiaryContainer}),",
        "    error = Color(${scheme.error}),",
        "    onError = Color(${scheme.onError}),",
        "    errorContainer = Color(${scheme.errorContainer}),",
        "    onErrorContainer = Color(${scheme.onErrorContainer}),",
        "    background = Color(${scheme.background}),",
        "    onBackground = Color(${scheme.onBackground}),",
        "    surface = Color(${scheme.surface}),",
        "    onSurface = Color(${scheme.onSurface}),",
        "    surfaceVariant = Color(${scheme.surfaceVariant}),",
        "    onSurfaceVariant = Color(${scheme.onSurfaceVariant}),",
        "    surfaceTint = Color(${scheme.surfaceTint}),",
        "    outline = Color(${scheme.outline}),",
        "    outlineVariant = Color(${scheme.outlineVariant}),",
        "    scrim = Color(${scheme.scrim}),",
        "    inverseSurface = Color(${scheme.inverseSurface}),",
        "    inverseOnSurface = Color(${scheme.inverseOnSurface}),",
        "    inversePrimary = Color(${scheme.inversePrimary}),",
        "    surfaceDim = Color(${scheme.surfaceDim}),",
        "    surfaceBright = Color(${scheme.surfaceBright}),",
        "    surfaceContainerLowest = Color(${scheme.surfaceContainerLowest}),",
        "    surfaceContainerLow = Color(${scheme.surfaceContainerLow}),",
        "    surfaceContainer = Color(${scheme.surfaceContainer}),",
        "    surfaceContainerHigh = Color(${scheme.surfaceContainerHigh}),",
        "    surfaceContainerHighest = Color(${scheme.surfaceContainerHighest}),",
        ")}",
        "",
        "object ${objectName}Palette {",
    )
    for (tone in 100 downTo 0 step 5) {
        val color = palette.tone(tone)
        result.add("    val Primary$tone by lazy { Color(${color}) }")
    }
    result.add("}")
    result.add("")
    return result
}
