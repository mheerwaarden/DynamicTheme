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
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.github.mheerwaarden.dynamictheme.R
import com.github.mheerwaarden.dynamictheme.material.color.utils.ColorExtractor

/**
 * View model for the [ImagePickerScreen] containing the URI of the selected image and the swatches
 * for the colors that are extracted from the image.
 */
class ImagePickerViewModel : ViewModel() {
    var uiState by mutableStateOf(ImagePickerUiState())
        private set

    /**
     * Update the state by setting the URI of the selected image and extracting the swatches for
     * the colors in the image.
     */
    fun updateState(context: Context, uri: Uri) {
        uiState = uiState.copy(
            imageUri = uri,
            paletteSwatches = getPaletteSwatches(context, uri),
            colorExtractionSwatches = getColorExtractionSwatches(context, uri)
        )
    }

    private fun getPaletteSwatches(context: Context, uri: Uri): List<UiSwatch> {
        val bitmap: Bitmap =
                context.contentResolver.openInputStream(uri).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
        val palette = Palette.from(bitmap).generate()
        return listOfNotNull(
            palette.vibrantSwatch?.let { UiSwatch(R.string.vibrant, it) },
            palette.darkVibrantSwatch?.let { UiSwatch(R.string.dark_vibrant, it) },
            palette.lightVibrantSwatch?.let { UiSwatch(R.string.light_vibrant, it) },
            palette.lightMutedSwatch?.let { UiSwatch(R.string.light_muted, it) },
            palette.mutedSwatch?.let { UiSwatch(R.string.muted, it) },
            palette.darkMutedSwatch?.let { UiSwatch(R.string.dark_muted, it) },
        )
    }

    private fun getColorExtractionSwatches(context: Context, uri: Uri): List<UiSwatch> {
        val colors = ColorExtractor.extractColorsFromImage(context, uri)
        return colors.mapIndexed { index, color -> getSwatchForColor(index, color) }
    }

    private fun getSwatchForColor(index: Int, color: Int): UiSwatch {
        val contrastColor = ColorExtractor.getContrastColorArgb(color)
        return UiSwatch(
            labelResID = COLOR_EXTRACTION_SWATCHES[index],
            argb = color,
            titleTextColor = contrastColor,
            bodyTextColor = contrastColor
        )
    }

}

private val COLOR_EXTRACTION_SWATCHES = listOf(
    R.string.first,
    R.string.second,
    R.string.third,
    R.string.fourth,
    R.string.fifth,
    R.string.sixth
)

data class ImagePickerUiState(
    /** The URI of the selected image. */
    val imageUri: Uri = Uri.EMPTY,
    /** The swatches for the colors that are extracted from the image using the Palette. */
    val paletteSwatches: List<UiSwatch> = emptyList(),
    /** The swatches for the colors that are extracted from the image using the ColorExtractor. */
    val colorExtractionSwatches: List<UiSwatch> = emptyList(),
)

/**
 * Data class representing a swatch for a color.
 */
data class UiSwatch(
    /** The string resource ID for the label of the swatch. */
    @StringRes val labelResID: Int,
    /** The ARGB value of the swatch. */
    val argb: Int,
    /** The title text color of the swatch. This color has sufficient contrast with [argb]. */
    val titleTextColor: Int,
    /** The body text color of the swatch. This color has sufficient contrast with [argb]. */
    val bodyTextColor: Int,
) {
    /** Create a swatch from a [Palette.Swatch] that was generated from a bitmap. */
    constructor(@StringRes labelResID: Int, palette: Palette.Swatch) : this(
        labelResID,
        palette.rgb,
        palette.titleTextColor,
        palette.bodyTextColor
    )
}
