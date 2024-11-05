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

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mheerwaarden.dynamictheme.APP_TAG
import com.github.mheerwaarden.dynamictheme.DynamicThemeTopAppBar
import com.github.mheerwaarden.dynamictheme.R
import com.github.mheerwaarden.dynamictheme.ui.ActionResult
import com.github.mheerwaarden.dynamictheme.ui.ActionResultState
import com.github.mheerwaarden.dynamictheme.ui.AppViewModelProvider
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeUiState
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeViewModel
import com.github.mheerwaarden.dynamictheme.ui.ShowActionResult
import com.github.mheerwaarden.dynamictheme.ui.component.InputField
import com.github.mheerwaarden.dynamictheme.ui.navigation.NavigationDestination

private const val TAG = APP_TAG + "_DynamicThemeDetail"

object DynamicThemeDetailDestination : NavigationDestination {
    override val route = "dynamic_theme_details"
    override val titleRes = R.string.theme_details
    const val themeIdArg = "themeId"
    val routeWithArgs = "$route/{$themeIdArg}"
}

/**
 * Entry route for a saved Dynamic Theme detail.
 * This has its own view model with an ID that is set through a SavedStateHandle. The orientation
 * is passed in from the global [DynamicThemeViewModel].
 * Changes in the name are not saved to the preferences.
 */
@Composable
fun DynamicThemeDetailScreen(
    // The preferences view model in which isHorizontalLayout is initialized
    themeViewModel: DynamicThemeViewModel,
    navigateHome: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    // Extension of the DynamicThemeViewModel with ID that is currently requested.
    // However, isHorizontalLayout not initialised
    detailViewModel: DynamicThemeDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current
    val saveResult by detailViewModel.saveResult.collectAsStateWithLifecycle(
        initialValue = ActionResult.None
    )
    val exportResult by detailViewModel.exportResult.collectAsStateWithLifecycle(
        initialValue = ActionResult.None
    )
    DynamicThemeDetailScreen(
        themeState = detailViewModel.uiState,
        isHorizontalLayout = themeViewModel.uiState.isHorizontalLayout(),
        isChanged = false,
        saveResult = saveResult,
        exportResult = exportResult,
        onNameChange = detailViewModel::updateName,
        onSave = detailViewModel::upsertDynamicTheme,
        onExport = { detailViewModel.exportDynamicTheme(context = context) },
        navigateHome = navigateHome,
        navigateBack = navigateBack,
        modifier = modifier
    )
}

/** Entry route for a Dynamic Theme detail that has not been saved to the database. */
@Composable
fun LatestDetailScreen(
    themeViewModel: DynamicThemeViewModel,
    navigateHome: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val themeState = themeViewModel.uiState
    val saveResult by themeViewModel.saveResult.collectAsStateWithLifecycle(
        initialValue = ActionResult.None
    )
    val exportResult by themeViewModel.exportResult.collectAsStateWithLifecycle(
        initialValue = ActionResult.None
    )
    DynamicThemeDetailScreen(
        themeState = themeState,
        isHorizontalLayout = themeState.isHorizontalLayout(),
        isChanged = true,
        saveResult = saveResult,
        exportResult = exportResult,
        onNameChange = themeViewModel::updateName,
        onSave = themeViewModel::upsertDynamicTheme,
        onExport = { themeViewModel.exportDynamicTheme(context = context) },
        navigateHome = navigateHome,
        navigateBack = navigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DynamicThemeDetailScreen(
    themeState: DynamicThemeUiState,
    isHorizontalLayout: Boolean,
    isChanged: Boolean,
    saveResult: ActionResultState,
    exportResult: ActionResultState,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onExport: () -> Unit,
    navigateHome: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var mustSave by rememberSaveable { mutableStateOf(isChanged) }

    // Update synchronously whenever the actionResult state changes
    val isSaveBusy: Boolean by remember { derivedStateOf { saveResult is ActionResultState.Busy } }
    val isExportBusy: Boolean by remember { derivedStateOf { exportResult is ActionResultState.Busy } }
    // Handle final action result asynchronously
    ShowActionResult(
        action = stringResource(R.string.save),
        actionResult = saveResult,
        onSuccess = { mustSave = false }
    )
    ShowActionResult(
        action = stringResource(R.string.export),
        actionResult = exportResult,
    )

    Scaffold(
        topBar = {
            DynamicThemeTopAppBar(
                title = stringResource(DynamicThemeDetailDestination.titleRes),
                canNavigateBack = true,
                navigateUp = navigateBack,
                actions = {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = stringResource(R.string.home),
                        modifier = Modifier.clickable(onClick = navigateHome)
                    )
                },
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Log.d(TAG, "DynamicThemeDetailScreen: name ${themeState.name} changed $isChanged")
        Column(modifier = Modifier.padding(innerPadding)) {
            Log.d(TAG, "DynamicThemeDetailScreen: Color ${themeState.sourceColorArgb} Variant ${themeState.uiColorSchemeVariant}")
            ThemeDescription(
                themeState = themeState,
                mustSave = mustSave,
                isSaveBusy = isSaveBusy,
                isExportBusy = isExportBusy,
                isHorizontalLayout = isHorizontalLayout,
                onNameChange = {
                    onNameChange(it)
                    mustSave = true
                },
                onSave = onSave,
                onExport = onExport,
                modifier = Modifier.fillMaxWidth()
            )
            ThemeShowcaseScreen(
                isHorizontalLayout = isHorizontalLayout,
                lightColorSchemeState = themeState.lightColorSchemeState,
                darkColorSchemeState = themeState.darkColorSchemeState,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeDescription(
    themeState: DynamicThemeUiState,
    mustSave: Boolean,
    isSaveBusy: Boolean,
    isExportBusy: Boolean,
    isHorizontalLayout: Boolean,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!isHorizontalLayout) {
        ColorAndVariantChoice(
            sourceArgb = themeState.sourceColorArgb,
            colorSchemeVariant = stringResource(themeState.uiColorSchemeVariant.nameResId)
        )
    }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (isHorizontalLayout) {
            ColorAndVariantChoice(
                sourceArgb = themeState.sourceColorArgb,
                colorSchemeVariant = stringResource(themeState.uiColorSchemeVariant.nameResId),
                isSmall = false,
                modifier = Modifier.weight(1f),
            )
        }
        InputField(
            labelId = R.string.name,
            value = themeState.name,
            singleLine = true,
            onValueChange = { onNameChange(it) },
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onExport) {
            if (isExportBusy) {
                CircularProgressIndicator(modifier = Modifier.size(dimensionResource(R.dimen.icon_size)))
            } else {
                Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = stringResource(R.string.export),
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_size))
                )
            }
        }
        IconButton(onClick = onSave, enabled = mustSave) {
            if (isSaveBusy) {
                CircularProgressIndicator(modifier = Modifier.size(dimensionResource(R.dimen.icon_size)))
            } else {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = stringResource(R.string.save),
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_size))
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, name = "Portrait")
fun DetailScreenVerticalPreview() {
    DynamicThemeDetailScreen(
        themeState = DynamicThemeUiState(name = "Vertical preview"),
        isHorizontalLayout = false,
        isChanged = false,
        saveResult = ActionResult.Busy,
        exportResult = ActionResult.None,
        onNameChange = {},
        onSave = {},
        onExport = {},
        navigateHome = {},
        navigateBack = {}
    )
}

@Composable
@Preview(
    showBackground = true, name = "Landscape",
    widthDp = 880,
    heightDp = 580,
)
fun DetailScreenHorizontalPreview() {
    DynamicThemeDetailScreen(
        themeState = DynamicThemeUiState(name = "Horizontal preview"),
        isHorizontalLayout = true,
        isChanged = false,
        saveResult = ActionResult.None,
        exportResult = ActionResult.None,
        onNameChange = {},
        onSave = {},
        onExport = {},
        navigateHome = {},
        navigateBack = {}
    )
}
