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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.github.mheerwaarden.dynamictheme.DynamicThemeTopAppBar
import com.github.mheerwaarden.dynamictheme.R
import com.github.mheerwaarden.dynamictheme.ui.AppViewModelProvider
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeViewModel
import com.github.mheerwaarden.dynamictheme.ui.navigation.NavigationDestination
import com.github.mheerwaarden.dynamictheme.ui.theme.BlackArgb
import com.github.mheerwaarden.dynamictheme.ui.theme.DynamicThemeAppTheme
import com.github.mheerwaarden.dynamictheme.ui.theme.WhiteArgb

object ImagePickerDestination : NavigationDestination {
    override val route = "image_picker"
    override val titleRes = R.string.color_extractor_for_image
}

/**
 * Select an image to extract colors from. Clicking the image will launch the photo picker. The top
 * bar has an icon that will launch the browser to select an image. This allows the user to select
 * images that are not stored in image folders, e.g. the download folder.
 * Colors are extracted in two different ways:
 * 1. Palette colors from the bitmap
 * 2. Color extraction by the material color library
 * Each set of colors has its own column that shows the extracted colors.
 * On the click of a color, the next [ColorSchemeVariantChooserScreen] step is activated to allow the user to
 * select a color scheme based on the selected color.
 *
 * @param navigateToThemeChooser The callback to invoke to go to the next screen.
 * @param navigateBack The callback to invoke when the user clicks the back button.
 * @param modifier Modifier to be applied to the screen.
 * @param themeViewModel The view model containing the color state for the current theme.
 * @param imagePickerViewModel The view model for this screen, containing the swatches for the
 * colors that are extracted from the image.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerScreen(
    themeViewModel: DynamicThemeViewModel,
    navigateToThemeChooser: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    imagePickerViewModel: ImagePickerViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current

    // [GetContent] is an ActivityResultContract that will launch a browser for the filter
    // specified in the launch call
    val browseImageLauncher = rememberLauncherForActivityResult(GetContent()) { uri ->
        if (uri != null) {
            imagePickerViewModel.updateState(context, uri)
        }
    }

    Scaffold(
        topBar = {
            DynamicThemeTopAppBar(
                title = stringResource(ImagePickerDestination.titleRes),
                canNavigateBack = true,
                navigateUp = navigateBack,
                actions = {
                    Icon(
                        imageVector = Icons.Outlined.ImageSearch,
                        contentDescription = stringResource(R.string.image_picker),
                        modifier = Modifier.clickable { browseImageLauncher.launch("image/*") }
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.primary,
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }, modifier = modifier
    ) { innerPadding ->
        val themeState = themeViewModel.uiState
        ImagePickerBody(
            imageState = imagePickerViewModel.uiState,
            isHorizontalLayout = themeState.isHorizontalLayout(),
            onSelectImage = { uri ->
                imagePickerViewModel.updateState(context, uri)
            },
            onSelectColor = { color ->
                themeViewModel.resetState()
                // themeState is now stale, so use the viewModel directly
                themeViewModel.updateColorScheme(color, themeViewModel.uiState.uiColorSchemeVariant)
                navigateToThemeChooser()
            },
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        )
    }
}

@Composable
private fun ImagePickerBody(
    imageState: ImagePickerUiState,
    isHorizontalLayout: Boolean,
    onSelectImage: (Uri) -> Unit,
    onSelectColor: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isHorizontalLayout) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                dimensionResource(id = R.dimen.padding_small),
                alignment = Alignment.Start
            ),
            modifier = modifier.fillMaxSize()
        ) {
            ImagePicker(imageState.imageUri, onSelectImage, modifier = Modifier.weight(1f))
            VerticalDivider()
            Swatches(
                uiState = imageState,
                onSelectColor = onSelectColor,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(id = R.dimen.padding_small),
                alignment = Alignment.Top
            ),
            modifier = modifier.fillMaxSize()
        ) {
            ImagePicker(imageState.imageUri, onSelectImage, modifier = Modifier.weight(1f))
            HorizontalDivider()
            Swatches(
                uiState = imageState,
                onSelectColor = onSelectColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun Swatches(
    uiState: ImagePickerUiState,
    onSelectColor: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        SwatchesColumn(uiState.paletteSwatches, onSelectColor, modifier.weight(1f))
        SwatchesColumn(uiState.colorExtractionSwatches, onSelectColor, modifier.weight(1f))
    }
}

@Composable
fun SwatchesColumn(
    swatches: List<UiSwatch>,
    onSelectColor: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(id = R.dimen.padding_minimum), alignment = Alignment.Top
        ),
        modifier = modifier
    ) {
        items(swatches) { swatch ->
            SwatchButton(swatch = swatch, onSelectColor = onSelectColor)
        }

    }
}

@Composable
fun SwatchButton(
    swatch: UiSwatch,
    onSelectColor: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonText = buildAnnotatedString {
        withStyle(
            style = MaterialTheme.typography.bodySmall.toSpanStyle()
                .copy(color = Color(swatch.titleTextColor))
        ) {
            append(stringResource(swatch.labelResID))
        }

        withStyle(
            style = MaterialTheme.typography.labelSmall.toSpanStyle()
                .copy(color = Color(swatch.bodyTextColor))
        ) {
            append(": ")
            append(swatch.argb.hexString())
        }
    }

    Button(
        onClick = { onSelectColor(swatch.argb) },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(swatch.argb),
            contentColor = Color(swatch.titleTextColor),
            disabledContainerColor = Color(swatch.argb),
            disabledContentColor = Color(swatch.bodyTextColor)
        ),
        modifier = modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
            .fillMaxWidth()
    ) { Text(buttonText) }
}

@Composable
private fun ImagePicker(
    selectedImageUri: Uri,
    onSelectImage: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    /** [PickVisualMedia] is an ActivityResultContract that will launch the photo picker intent */
    val photoPickerLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            onSelectImage(uri)
        }
    }

    if (selectedImageUri == Uri.EMPTY) {
        // Question mark icon
        Image(
            painter = painterResource(id = R.drawable.indeterminate_question_box),
            contentDescription = stringResource(R.string.no_image_selected),
            contentScale = ContentScale.Fit,
            modifier = modifier
                .sizeIn(
                    minWidth = dimensionResource(R.dimen.image_size),
                    minHeight = dimensionResource(R.dimen.image_size)
                )
                .clickable { photoPickerLauncher.launch(PickVisualMediaRequest()) }
        )
    } else {
        AsyncImage(
            model = selectedImageUri,
            contentDescription = stringResource(R.string.selected_image),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .aspectRatio(1f)
                .sizeIn(
                    minWidth = dimensionResource(R.dimen.image_size),
                    minHeight = dimensionResource(R.dimen.image_size)
                )
                .clickable { photoPickerLauncher.launch(PickVisualMediaRequest()) },
        )
    }
}

private fun Int.hexString(): String = String.format("#%06X", (0xFFFFFF and this))

@Preview(showBackground = true)
@Composable
fun ImagePickerScreenPreview() {
    DynamicThemeAppTheme {
        ImagePickerBody(
            imageState = getImagePickerPreviewState(),
            isHorizontalLayout = false,
            onSelectImage = {},
            onSelectColor = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SwatchesPreview() {
     Swatches(uiState = getImagePickerPreviewState(), onSelectColor = {})
}

private fun getImagePickerPreviewState(): ImagePickerUiState {
    val white = WhiteArgb
    val black = BlackArgb
    val blue = Color.Blue.toArgb()
    val red = Color.Red.toArgb()
    val green = Color.Green.toArgb()
    val yellow = Color.Yellow.toArgb()
    return ImagePickerUiState(
        paletteSwatches = listOf(
            UiSwatch(R.string.vibrant, blue, white, white),
            UiSwatch(R.string.dark_vibrant, red, white, white)
        ),
        colorExtractionSwatches = listOf(
            UiSwatch(R.string.first, green, black, black),
            UiSwatch(R.string.second, yellow, black, black)
        )
    )
}

