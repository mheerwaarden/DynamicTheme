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

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.github.mheerwaarden.dynamictheme.DynamicThemeTopAppBar
import com.github.mheerwaarden.dynamictheme.R
import com.github.mheerwaarden.dynamictheme.material.color.utils.ColorExtractor.createDynamicColorScheme
import com.github.mheerwaarden.dynamictheme.ui.AppViewModelProvider
import com.github.mheerwaarden.dynamictheme.ui.navigation.NavigationDestination
import com.github.mheerwaarden.dynamictheme.ui.theme.DynamicThemeTheme
import dynamiccolor.DynamicScheme

object PaletteDestination : NavigationDestination {
    override val route = "palette"
    override val titleRes = R.string.palette_for_image
}

object ColorExtractorDestination : NavigationDestination {
    override val route = "color_extractor"
    override val titleRes = R.string.color_extractor_for_image
}

enum class ColorExtractionStrategy {
    Palette, ColorExtractor
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaletteScreen(
    windowSizeClass: WindowSizeClass,
    @StringRes titleResId: Int,
    colorExtractionStrategy: ColorExtractionStrategy,
    onChangeColorScheme: (DynamicScheme) -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaletteViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState.value

    Scaffold(
        topBar = {
            DynamicThemeTopAppBar(
                title = stringResource(titleResId),
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        }, modifier = modifier
    ) { innerPadding ->
        PaletteBody(
            uiState = uiState,
            isCompactWidth = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact,
            onSelectImage = { uri ->
                viewModel.updateUiState(context, uri, colorExtractionStrategy)
            },
            onChangeColorScheme = onChangeColorScheme,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
private fun PaletteBody(
    uiState: ImagePickerUiState,
    isCompactWidth: Boolean,
    onSelectImage: (Uri) -> Unit,
    onChangeColorScheme: (DynamicScheme) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isCompactWidth) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(id = R.dimen.padding_small),
                alignment = Alignment.Top
            ),
            modifier = modifier.fillMaxSize()
        ) {
            ImagePicker(onSelectImage, modifier = Modifier.weight(1f))
            HorizontalDivider()
            Swatches(
                uiState = uiState,
                onChangeColorScheme = onChangeColorScheme,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                dimensionResource(id = R.dimen.padding_small),
                alignment = Alignment.Start
            ),
            modifier = modifier.fillMaxSize()
        ) {
            ImagePicker(onSelectImage, modifier = Modifier.weight(1f))
            VerticalDivider()
            Swatches(
                uiState = uiState,
                onChangeColorScheme = onChangeColorScheme,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun Swatches(
    uiState: ImagePickerUiState,
    onChangeColorScheme: (DynamicScheme) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(id = R.dimen.padding_minimum),
            alignment = Alignment.Top
        ),
        modifier = modifier
    ) {
        item {
            SwatchButton(
                labelResID = R.string.vibrant,
                swatch = uiState.vibrant,
                onChangeColorScheme = onChangeColorScheme
            )
        }
        item {
            SwatchButton(
                labelResID = R.string.dark_muted,
                swatch = uiState.darkMuted,
                onChangeColorScheme = onChangeColorScheme
            )
        }
        item {
            SwatchButton(
                labelResID = R.string.dark_vibrant,
                swatch = uiState.darkVibrant,
                onChangeColorScheme = onChangeColorScheme
            )
        }
        item {
            SwatchButton(
                labelResID = R.string.light_vibrant,
                swatch = uiState.lightVibrant,
                onChangeColorScheme = onChangeColorScheme
            )
        }
        item {
            SwatchButton(
                labelResID = R.string.light_muted,
                swatch = uiState.lightMuted,
                onChangeColorScheme = onChangeColorScheme
            )
        }
        item {
            SwatchButton(
                labelResID = R.string.muted,
                swatch = uiState.muted,
                onChangeColorScheme = onChangeColorScheme
            )
        }
    }
}

@Composable
fun SwatchButton(
    swatch: Swatch?,
    @StringRes labelResID: Int,
    onChangeColorScheme: (DynamicScheme) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (swatch == null) {
        Button(
            onClick = { /* Do nothing */ },
            enabled = false,
            modifier = modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(
                    R.string.no_color,
                    stringResource(labelResID, "")
                ), modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        val buttonText = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = Color(swatch.titleTextColor),
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(stringResource(labelResID, ""))
            }

            withStyle(style = SpanStyle(color = Color(swatch.bodyTextColor))) {
                append(": ")
                append(swatch.rgb.hexString())
            }
        }

        Button(
            onClick = {
                val colorScheme = createDynamicColorScheme(swatch.rgb)
                onChangeColorScheme(colorScheme)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(swatch.rgb)),
            modifier = modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
                .fillMaxWidth()
        ) { Text(buttonText) }
    }
}

@Composable
private fun ImagePicker(onSelectImage: (Uri) -> Unit, modifier: Modifier = Modifier) {
    var selectedImageUri by rememberSaveable { mutableStateOf(Uri.EMPTY) }

    /** [PickVisualMedia] is an ActivityResultContract that will launch the photo picker intent */
    val photoPickerLauncher =
            rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
                if (uri != null) {
                    selectedImageUri = uri
                    onSelectImage(uri)
                }
            }

    /** [GetContent] is an ActivityResultContract that will launch a browser for the given filter */
    val browseImageLauncher =
            rememberLauncherForActivityResult(GetContent()) { uri ->
                if (uri != null) {
                    selectedImageUri = uri
                    onSelectImage(uri)
                }
            }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(horizontalArrangement = Arrangement.SpaceAround) {
            Button(
                onClick = { photoPickerLauncher.launch(PickVisualMediaRequest()) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = dimensionResource(R.dimen.padding_small))
            ) {
                Icon(
                    imageVector = Icons.Default.Photo,
                    contentDescription = stringResource(R.string.image_picker),
                )
                Text(stringResource(R.string.photo_picker))
            }
            Button(
                onClick = { browseImageLauncher.launch("image/*") },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = dimensionResource(R.dimen.padding_small))
            ) {
                Icon(
                    imageVector = Icons.Default.ImageSearch,
                    contentDescription = stringResource(R.string.image_picker),
                )
                Text(stringResource(R.string.browse))
            }
        }
        if (selectedImageUri == Uri.EMPTY) {
            // Question mark icon
            Image(
                painter = painterResource(id = R.drawable.indeterminate_question_box),
                contentDescription = stringResource(R.string.no_image_selected),
                contentScale = ContentScale.Fit,
                modifier = modifier.sizeIn(minWidth = 250.dp, minHeight = 250.dp)
            )
        } else {
            AsyncImage(
                model = selectedImageUri,
                contentDescription = stringResource(R.string.selected_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(1f)
                    .sizeIn(minWidth = 250.dp, minHeight = 250.dp),
            )
        }
    }
}

private fun Int.hexString(): String {
    return String.format("#%06X", (0xFFFFFF and this))
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
fun ImagePickerScreenPreview() {
    DynamicThemeTheme {
        val windowSizeClass = WindowSizeClass.calculateFromSize(
            // Compact width, normal mobile phone
            DpSize(
                width = 580.dp,
                height = 880.dp
            )
        )
        PaletteScreen(
            windowSizeClass = windowSizeClass,
            titleResId = PaletteDestination.titleRes,
            colorExtractionStrategy = ColorExtractionStrategy.Palette,
            onChangeColorScheme = {},
            navigateBack = {}
        )
    }
}
