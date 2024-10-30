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

package com.github.mheerwaarden.dynamictheme

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.github.mheerwaarden.dynamictheme.material.color.utils.ColorExtractor
import com.github.mheerwaarden.dynamictheme.ui.AppViewModelProvider
import com.github.mheerwaarden.dynamictheme.ui.PreferencesViewModel
import com.github.mheerwaarden.dynamictheme.ui.fromColorSchemeState
import com.github.mheerwaarden.dynamictheme.ui.navigation.DynamicThemeNavHost
import com.github.mheerwaarden.dynamictheme.ui.screen.DynamicThemeViewModel
import com.github.mheerwaarden.dynamictheme.ui.theme.DynamicThemeTheme
import com.github.mheerwaarden.dynamictheme.ui.toColorSchemeState

const val APP_TAG = "DynamicTheme"

@Composable
fun DynamicThemeApp(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    themeViewModel: DynamicThemeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    preferencesViewModel: PreferencesViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current
    val preferences by preferencesViewModel.preferencesState.collectAsState()
    val preferenceColorSchemeState = ColorExtractor.createDynamicColorScheme(
        sourceArgb = preferences.sourceColor,
        schemeVariant = preferences.dynamicSchemeVariant,
        isDark = isSystemInDarkTheme()
    ).toColorSchemeState()
    Log.d(
        APP_TAG,
        "DynamicThemeApp: Using theme ${preferences.id} ${preferences.name}/${preferences.dynamicSchemeVariant}"
    )

    // Prepare theme view model
    themeViewModel.updateName(preferences.name)
    themeViewModel.updateColorScheme(preferences.sourceColor, preferences.uiColorSchemeVariant)
    themeViewModel.updateWindowSizeClass(windowSizeClass)
    themeViewModel.onException = { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() }

    DynamicThemeTheme(colorScheme = fromColorSchemeState(preferenceColorSchemeState)) {
        val themeState by themeViewModel.uiState.collectAsState()
        val lastId by rememberSaveable { mutableLongStateOf(themeState.id) }
        val navController = rememberNavController()

        // Once the save thread is finished, there is an ID; update the preference for this
        if (lastId != themeState.id) {
            preferencesViewModel.setIdPreference(themeState.id)
        }

        DynamicThemeNavHost(
            navController = navController,
            themeState = themeState,
            onResetPreferences = { preferencesViewModel.setIdPreference(-1L) },
            onNameChange = { name ->
                themeViewModel.updateName(name)
                preferencesViewModel.setNamePreference(name)
            },
            onColorSchemeChange = { sourceColorArgb, uiColorSchemeVariant ->
                themeViewModel.updateColorScheme(sourceColorArgb, uiColorSchemeVariant)
                preferencesViewModel.setSourceColorPreference(
                    sourceColorArgb, uiColorSchemeVariant.toVariant()
                )
            },
            onSave = {
                themeViewModel.upsertDynamicTheme()
            },
            modifier = modifier
        )
    }
}

/**
 * App bar to display title and conditionally display the back navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicThemeTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
    navigateUp: () -> Unit = {},
    actions: @Composable (RowScope.() -> Unit) = {},
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        },
        actions = actions,
        colors = colors
    )
}
