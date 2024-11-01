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
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    isHorizontalLayout: Boolean,
    navigateHome: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    // Extension of the DynamicThemeViewModel with ID, isHorizontalLayout not initialised
    viewModel: DynamicThemeDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val themeState = viewModel.uiState.collectAsState().value
    DynamicThemeDetailScreen(
        themeState = themeState,
        isHorizontalLayout = isHorizontalLayout,
        isChanged = false,
        onNameChange = viewModel::updateName,
        onSave = viewModel::upsertDynamicTheme,
        navigateHome = navigateHome,
        navigateBack = navigateBack,
        modifier = modifier
    )
}

/** Entry route for a Dynamic Theme detail that has not been saved to the database. */
@Composable
fun DynamicThemeDetailScreen(
    themeState: DynamicThemeUiState,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    navigateHome: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DynamicThemeDetailScreen(
        themeState = themeState,
        isHorizontalLayout = themeState.isHorizontalLayout(),
        isChanged = true,
        onNameChange = onNameChange,
        onSave = onSave,
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
            InputField(
                labelId = R.string.name,
                value = themeState.name,
                singleLine = true,
                onValueChange = {
                    onNameChange(it)
                    mustSave = true
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            onSave()
                            mustSave = false
                        },
                        enabled = mustSave
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = stringResource(R.string.save)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Log.d(TAG, "DynamicThemeDetailScreen: Color ${themeState.sourceColorArgb} Variant ${themeState.uiColorSchemeVariant}")
            ColorAndVariantChoice(
                sourceArgb = themeState.sourceColorArgb,
                colorSchemeVariant = stringResource(themeState.uiColorSchemeVariant.nameResId)
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
@Preview(showBackground = true)
fun DetailScreenVerticalPreview() {
    DynamicThemeDetailScreen(themeState = DynamicThemeUiState(name = "Vertical preview"),
        onNameChange = {},
        onSave = {},
        navigateHome = {},
        navigateBack = {}
    )
}

@Composable
@Preview(showBackground = true)
fun DetailScreenHorizontalPreview() {
    DynamicThemeDetailScreen(themeState = DynamicThemeUiState(name = "Horizontal preview"),
        onNameChange = {},
        onSave = {},
        navigateHome = {},
        navigateBack = {}
    )
}
