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
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.github.mheerwaarden.dynamictheme.R
import com.github.mheerwaarden.dynamictheme.material.color.utils.ColorExtractor
import kotlinx.coroutines.launch

class ImagePickerViewModel : ViewModel() {
    var uiState by mutableStateOf(ImagePickerUiState())
        private set

    fun updateImageUri(uri: Uri) {
        uiState = uiState.copy(imageUri = uri)
    }

    fun updateSwatches(
        context: Context,
        uri: Uri,
    ) {
        viewModelScope.launch {
            updatePaletteSwatches(context, uri)
            updateColorExtractionSwatches(context, uri)
        }
    }

    private fun updatePaletteSwatches(context: Context, uri: Uri) {
        val bitmap: Bitmap =
                context.contentResolver.openInputStream(uri).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
        val palette = Palette.from(bitmap).generate()
        val swatches = listOfNotNull(
            palette.vibrantSwatch?.let { Swatch(R.string.vibrant, it) },
            palette.darkVibrantSwatch?.let { Swatch(R.string.dark_vibrant, it) },
            palette.lightVibrantSwatch?.let { Swatch(R.string.light_vibrant, it) },
            palette.lightMutedSwatch?.let { Swatch(R.string.light_muted, it) },
            palette.mutedSwatch?.let { Swatch(R.string.muted, it) },
            palette.darkMutedSwatch?.let { Swatch(R.string.dark_muted, it) },
        )
        uiState = uiState.copy(paletteSwatches = swatches)
    }

    private fun updateColorExtractionSwatches(context: Context, uri: Uri) {
        val colors = ColorExtractor.extractColorsFromImage(context, uri)
        val swatches = colors.mapIndexed { index, color -> getSwatchForColor(index, color) }
        uiState = uiState.copy(colorExtractionSwatches = swatches)
    }

    private fun getSwatchForColor(index: Int, color: Int): Swatch {
        val contrastColor = ColorExtractor.getContrastColor(color)
        return Swatch(COLOR_EXTRACTION_SWATCHES[index], color, contrastColor, contrastColor)
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
    val imageUri: Uri = Uri.EMPTY,
    val paletteSwatches: List<Swatch> = emptyList(),
    val colorExtractionSwatches: List<Swatch> = emptyList(),
)

data class Swatch(
    @StringRes val labelResID: Int,
    val rgb: Int,
    val titleTextColor: Int,
    val bodyTextColor: Int,
) {
    constructor(@StringRes labelResID: Int, palette: Palette.Swatch) : this(
        labelResID,
        palette.rgb,
        palette.titleTextColor,
        palette.bodyTextColor
    )
}
