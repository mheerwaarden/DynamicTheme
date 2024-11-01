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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mheerwaarden.dynamictheme.APP_TAG
import com.github.mheerwaarden.dynamictheme.DynamicThemeTopAppBar
import com.github.mheerwaarden.dynamictheme.R
import com.github.mheerwaarden.dynamictheme.ui.AppViewModelProvider
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeUiState
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeViewModel
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
    themeViewModel: DynamicThemeViewModel,
    navigateHome: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    // Extension of the DynamicThemeViewModel with ID, isHorizontalLayout not initialised
    detailViewModel: DynamicThemeDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    DynamicThemeDetailScreen(
        themeState = detailViewModel.uiState,
        isHorizontalLayout = themeViewModel.uiState.isHorizontalLayout(),
        isChanged = false,
        onNameChange = detailViewModel::updateName,
        onSave = detailViewModel::upsertDynamicTheme,
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
    val themeState = themeViewModel.uiState
    DynamicThemeDetailScreen(
        themeState = themeState,
        isHorizontalLayout = themeState.isHorizontalLayout(),
        isChanged = true,
        onNameChange = themeViewModel::updateName,
        onSave = themeViewModel::upsertDynamicTheme,
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
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    navigateHome: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var mustSave by rememberSaveable { mutableStateOf(isChanged) }
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
                isHorizontalLayout = isHorizontalLayout,
                onNameChange = {
                    onNameChange(it)
                    mustSave = true
                },
                onSave = {
                    onSave()
                    mustSave = false
                },
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
    isHorizontalLayout: Boolean,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isHorizontalLayout) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            ColorAndVariantChoice(
                sourceArgb = themeState.sourceColorArgb,
                colorSchemeVariant = stringResource(themeState.uiColorSchemeVariant.nameResId),
                isSmall = false,
                modifier = Modifier.weight(1f),
            )
            InputField(
                labelId = R.string.name,
                value = themeState.name,
                singleLine = true,
                onValueChange = {
                    onNameChange(it)
                },
                trailingIcon = {
                    IconButton(onClick = onSave, enabled = mustSave) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = stringResource(R.string.save)
                        )
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
    } else {
        ColorAndVariantChoice(
            sourceArgb = themeState.sourceColorArgb,
            colorSchemeVariant = stringResource(themeState.uiColorSchemeVariant.nameResId)
        )
        InputField(
            labelId = R.string.name,
            value = themeState.name,
            singleLine = true,
            onValueChange = {
                onNameChange(it)
            },
            trailingIcon = {
                IconButton(onClick = onSave, enabled = mustSave) {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = stringResource(R.string.save)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
@Preview(showBackground = true, name = "Portrait")
fun DetailScreenVerticalPreview() {
    DynamicThemeDetailScreen(
        themeState = DynamicThemeUiState(name = "Vertical preview"),
        isHorizontalLayout = false,
        isChanged = false,
        onNameChange = {},
        onSave = {},
        navigateHome = {},
        navigateBack = {})
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
        onNameChange = {},
        onSave = {},
        navigateHome = {},
        navigateBack = {})
}
