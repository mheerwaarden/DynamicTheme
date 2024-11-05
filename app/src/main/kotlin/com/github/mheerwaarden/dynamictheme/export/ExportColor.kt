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

import com.github.mheerwaarden.dynamictheme.material.color.utils.ColorExtractor
import com.github.mheerwaarden.dynamictheme.material.color.utils.ContrastLevel
import dynamiccolor.Variant

fun exportColorKotlin(sourceColorArgb: Int, schemeVariant: Variant): List<String> {
    val result = mutableListOf(
        "package com.example.dynamictheme",
        "",
        "import androidx.compose.ui.graphics.Color",
        "",
    )
    result.addAll(getColorScheme(
        sourceColorArgb = sourceColorArgb,
        schemeVariant = schemeVariant,
        isDark = false,
        contrastLevel = ContrastLevel.Normal,
        colorNamePostfix = "Light"
    ))
    result.addAll(getColorScheme(
        sourceColorArgb = sourceColorArgb,
        schemeVariant = schemeVariant,
        isDark = false,
        contrastLevel = ContrastLevel.Medium,
        colorNamePostfix = "LightMediumContrast"
    ))
    result.addAll(getColorScheme(
        sourceColorArgb = sourceColorArgb,
        schemeVariant = schemeVariant,
        isDark = false,
        contrastLevel = ContrastLevel.High,
        colorNamePostfix = "LightHighContrast"
    ))
    result.addAll(getColorScheme(
        sourceColorArgb = sourceColorArgb,
        schemeVariant = schemeVariant,
        isDark = true,
        contrastLevel = ContrastLevel.Normal,
        colorNamePostfix = "Dark"
    ))
    result.addAll(getColorScheme(
        sourceColorArgb = sourceColorArgb,
        schemeVariant = schemeVariant,
        isDark = true,
        contrastLevel = ContrastLevel.Medium,
        colorNamePostfix = "DarkMediumContrast"
    ))
    result.addAll(getColorScheme(
        sourceColorArgb = sourceColorArgb,
        schemeVariant = schemeVariant,
        isDark = true,
        contrastLevel = ContrastLevel.High,
        colorNamePostfix = "DarkHighContrast"
    ))
    return result
}

fun getColorScheme(
    sourceColorArgb: Int,
    schemeVariant: Variant,
    isDark: Boolean,
    contrastLevel: ContrastLevel,
    colorNamePostfix: String,
): List<String> {
    val scheme = ColorExtractor.createDynamicColorScheme(
        sourceArgb = sourceColorArgb,
        schemeVariant = schemeVariant,
        isDark = isDark,
        contrastLevel = contrastLevel
    )
    return listOf(
        "val primary$colorNamePostfix = Color(${scheme.primary})",
        "val onPrimary$colorNamePostfix = Color(${scheme.onPrimary})",
        "val primaryContainer$colorNamePostfix = Color(${scheme.primaryContainer})",
        "val onPrimaryContainer$colorNamePostfix = Color(${scheme.onPrimaryContainer})",
        "val secondary$colorNamePostfix = Color(${scheme.secondary})",
        "val onSecondary$colorNamePostfix = Color(${scheme.onSecondary})",
        "val secondaryContainer$colorNamePostfix = Color(${scheme.secondaryContainer})",
        "val onSecondaryContainer$colorNamePostfix = Color(${scheme.onSecondaryContainer})",
        "val tertiary$colorNamePostfix = Color(${scheme.tertiary})",
        "val onTertiary$colorNamePostfix = Color(${scheme.onTertiary})",
        "val tertiaryContainer$colorNamePostfix = Color(${scheme.tertiaryContainer})",
        "val onTertiaryContainer$colorNamePostfix = Color(${scheme.onTertiaryContainer})",
        "val error$colorNamePostfix = Color(${scheme.error})",
        "val onError$colorNamePostfix = Color(${scheme.onError})",
        "val errorContainer$colorNamePostfix = Color(${scheme.errorContainer})",
        "val onErrorContainer$colorNamePostfix = Color(${scheme.onErrorContainer})",
        "val background$colorNamePostfix = Color(${scheme.background})",
        "val onBackground$colorNamePostfix = Color(${scheme.onBackground})",
        "val surface$colorNamePostfix = Color(${scheme.surface})",
        "val onSurface$colorNamePostfix = Color(${scheme.onSurface})",
        "val surfaceVariant$colorNamePostfix = Color(${scheme.surfaceVariant})",
        "val onSurfaceVariant$colorNamePostfix = Color(${scheme.onSurfaceVariant})",
        "val outline$colorNamePostfix = Color(${scheme.outline})",
        "val outlineVariant$colorNamePostfix = Color(${scheme.outlineVariant})",
        "val scrim$colorNamePostfix = Color(${scheme.scrim})",
        "val inverseSurface$colorNamePostfix = Color(${scheme.inverseSurface})",
        "val inverseOnSurface$colorNamePostfix = Color(${scheme.inverseOnSurface})",
        "val inversePrimary$colorNamePostfix = Color(${scheme.inversePrimary})",
        "val surfaceDim$colorNamePostfix = Color(${scheme.surfaceDim})",
        "val surfaceBright$colorNamePostfix = Color(${scheme.surfaceBright})",
        "val surfaceContainerLowest$colorNamePostfix = Color(${scheme.surfaceContainerLowest})",
        "val surfaceContainerLow$colorNamePostfix = Color(${scheme.surfaceContainerLow})",
        "val surfaceContainer$colorNamePostfix = Color(${scheme.surfaceContainer})",
        "val surfaceContainerHigh$colorNamePostfix = Color(${scheme.surfaceContainerHigh})",
        "val surfaceContainerHighest$colorNamePostfix = Color(${scheme.surfaceContainerHighest})",
        "",
    )
}
