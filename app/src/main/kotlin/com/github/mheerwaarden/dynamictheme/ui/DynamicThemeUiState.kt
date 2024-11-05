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

package com.github.mheerwaarden.dynamictheme.ui

import android.util.Log
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.github.mheerwaarden.dynamictheme.APP_TAG
import com.github.mheerwaarden.dynamictheme.data.database.DynamicTheme
import com.github.mheerwaarden.dynamictheme.data.preferences.INVALID
import com.github.mheerwaarden.dynamictheme.material.color.utils.ColorExtractor
import com.github.mheerwaarden.dynamictheme.ui.screen.UiColorSchemeVariant
import com.github.mheerwaarden.dynamictheme.ui.theme.DarkColorScheme
import com.github.mheerwaarden.dynamictheme.ui.theme.LightColorScheme
import com.github.mheerwaarden.dynamictheme.ui.theme.WhiteArgb
import dynamiccolor.Variant
import java.time.LocalDateTime

private const val TAG = APP_TAG + "DynamicThemeUiState"

data class DynamicThemeUiState(
    val id: Long = INVALID,
    val name: String = "",
    val sourceColorArgb: Int = WhiteArgb,
    val uiColorSchemeVariant: UiColorSchemeVariant = UiColorSchemeVariant.TonalSpot,
    val lightColorSchemeState: ColorSchemeState = LightColorScheme.toColorSchemeState(),
    val darkColorSchemeState: ColorSchemeState = DarkColorScheme.toColorSchemeState(),

    val windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
) {

    fun toDynamicTheme(): DynamicTheme = DynamicTheme(
        id = if (id < 0) 0 else id,
        name = name,
        sourceArgb = sourceColorArgb,
        colorSchemeVariant = uiColorSchemeVariant.toVariant(),
        primaryArgb = lightColorSchemeState.primary,
        onPrimaryArgb = lightColorSchemeState.onPrimary,
        secondaryArgb = lightColorSchemeState.secondary,
        onSecondaryArgb = lightColorSchemeState.onSecondary,
        tertiaryArgb = lightColorSchemeState.tertiary,
        onTertiaryArgb = lightColorSchemeState.onTertiary,
        surfaceArgb = lightColorSchemeState.surface,
        onSurfaceArgb = lightColorSchemeState.onSurface,
        surfaceVariantArgb = lightColorSchemeState.surfaceVariant,
        onSurfaceVariantArgb = lightColorSchemeState.onSurfaceVariant,
        errorArgb = lightColorSchemeState.error,
        onErrorArgb = lightColorSchemeState.onError,
        timestamp = LocalDateTime.now()
    )

    fun isHorizontalLayout(): Boolean = windowWidthSizeClass != WindowWidthSizeClass.Compact

    companion object {
        fun fromDynamicTheme(
            dynamicTheme: DynamicTheme,
            windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
        ): DynamicThemeUiState = createDynamicThemeUiState(
            id = dynamicTheme.id,
            name = dynamicTheme.name,
            sourceColorArgb = dynamicTheme.sourceArgb,
            uiColorSchemeVariant = UiColorSchemeVariant.fromVariant(dynamicTheme.colorSchemeVariant),
            windowWidthSizeClass = windowWidthSizeClass
        )

        fun createDynamicThemeUiState(
            sourceColorArgb: Int,
            windowWidthSizeClass: WindowWidthSizeClass,
            id: Long = 0L,
            name: String = "",
            uiColorSchemeVariant: UiColorSchemeVariant = UiColorSchemeVariant.TonalSpot,
        ): DynamicThemeUiState {
            Log.d(
                TAG, "Create State: $id - $name Color $sourceColorArgb, Theme $uiColorSchemeVariant"
            )
            val schemeVariant = Variant.entries[uiColorSchemeVariant.ordinal]
            return DynamicThemeUiState(
                id = id,
                name = name,
                sourceColorArgb = sourceColorArgb,
                uiColorSchemeVariant = uiColorSchemeVariant,
                lightColorSchemeState = ColorExtractor.createDynamicColorScheme(
                    sourceArgb = sourceColorArgb, schemeVariant = schemeVariant, isDark = false
                ).toColorSchemeState(),
                darkColorSchemeState = ColorExtractor.createDynamicColorScheme(
                    sourceArgb = sourceColorArgb, schemeVariant = schemeVariant, isDark = true
                ).toColorSchemeState(),
                windowWidthSizeClass = windowWidthSizeClass
            )
        }
    }
}