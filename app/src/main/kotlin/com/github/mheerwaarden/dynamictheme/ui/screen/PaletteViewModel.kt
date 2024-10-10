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

package com.github.mheerwaarden.dynamictheme.ui.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.github.mheerwaarden.dynamictheme.material.color.utils.ColorExtractor
import contrast.Contrast
import utils.ColorUtils

class PaletteViewModel : ViewModel() {
    var uiState = mutableStateOf(ImagePickerUiState())
        private set

    fun updateUiState(
        context: Context,
        uri: Uri,
        colorExtractionStrategy: ColorExtractionStrategy,
    ) {
        when (colorExtractionStrategy) {
            ColorExtractionStrategy.Palette -> {
                val bitmap: Bitmap =
                        context.contentResolver.openInputStream(uri).use { inputStream ->
                            BitmapFactory.decodeStream(inputStream)
                        }
                val palette = Palette.from(bitmap).generate()
                uiState.value = ImagePickerUiState(
                    vibrant = palette.vibrantSwatch?.let { Swatch(it) },
                    darkVibrant = palette.darkVibrantSwatch?.let { Swatch(it) },
                    lightVibrant = palette.lightVibrantSwatch?.let { Swatch(it) },
                    lightMuted = palette.lightMutedSwatch?.let { Swatch(it) },
                    muted = palette.mutedSwatch?.let { Swatch(it) },
                    darkMuted = palette.darkMutedSwatch?.let { Swatch(it) },
                )
            }

            ColorExtractionStrategy.ColorExtractor -> {
                val colors = ColorExtractor.extractColorsFromImage(context, uri)
                uiState.value = ImagePickerUiState(
                    vibrant = colors.firstOrNull()?.let { getSwatchForColor(it) },
                    darkVibrant = colors.getOrNull(1)?.let { getSwatchForColor(it) },
                    lightVibrant = colors.getOrNull(2)?.let { getSwatchForColor(it) },
                    lightMuted = colors.getOrNull(3)?.let { getSwatchForColor(it) },
                    muted = colors.getOrNull(4)?.let { getSwatchForColor(it) },
                    darkMuted = colors.getOrNull(5)?.let { getSwatchForColor(it) },
                )
            }
        }

    }

    private fun getSwatchForColor(color: Int): Swatch {
        val contrastColor = getContrastColor(color)
        return Swatch(color, contrastColor, contrastColor)
    }

    private fun getContrastColor(color: Int): Int {
        val tone1 = ColorUtils.lstarFromArgb(color)
        val toneWhite = ColorUtils.lstarFromArgb(Color.White.toArgb())
        val contrastRatioWhite = Contrast.ratioOfTones(tone1, toneWhite)
        val toneBlack = ColorUtils.lstarFromArgb(Color.Black.toArgb())
        val contrastRatioBlack = Contrast.ratioOfTones(tone1, toneBlack)
        return if (contrastRatioWhite > contrastRatioBlack) Color.White.toArgb() else Color.Black.toArgb()
    }

}

data class ImagePickerUiState(
    val vibrant: Swatch? = null,
    val darkVibrant: Swatch? = null,
    val lightVibrant: Swatch? = null,
    val lightMuted: Swatch? = null,
    val muted: Swatch? = null,
    val darkMuted: Swatch? = null,
)

data class Swatch(
    val rgb: Int,
    val titleTextColor: Int,
    val bodyTextColor: Int,
) {
    constructor(palette: Palette.Swatch) : this(
        palette.rgb,
        palette.titleTextColor,
        palette.bodyTextColor
    )
}
