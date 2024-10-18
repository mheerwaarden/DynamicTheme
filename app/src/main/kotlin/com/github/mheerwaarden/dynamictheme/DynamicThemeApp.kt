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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.mheerwaarden.dynamictheme.material.color.utils.ColorExtractor
import com.github.mheerwaarden.dynamictheme.ui.AppViewModelProvider
import com.github.mheerwaarden.dynamictheme.ui.PreferencesViewModel
import com.github.mheerwaarden.dynamictheme.ui.fromColorSchemeState
import com.github.mheerwaarden.dynamictheme.ui.home.HomeDestination
import com.github.mheerwaarden.dynamictheme.ui.navigation.DynamicThemeNavHost
import com.github.mheerwaarden.dynamictheme.ui.screen.UiColorSchemeVariant
import com.github.mheerwaarden.dynamictheme.ui.theme.DynamicThemeTheme
import com.github.mheerwaarden.dynamictheme.ui.toColorSchemeState

const val APP_TAG = "DynamicTheme"

@Composable
fun DynamicThemeApp(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    preferencesViewModel: PreferencesViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val preferences = preferencesViewModel.preferencesState.collectAsState().value
    val preferenceColorSchemeState = ColorExtractor.createDynamicColorScheme(
        sourceArgb = preferences.sourceColor,
        schemeVariant = preferences.dynamicSchemeVariant,
        isDark = isSystemInDarkTheme()
    ).toColorSchemeState()
    Log.d(APP_TAG, "Using theme ${preferences.dynamicSchemeVariant}")

    DynamicThemeTheme(colorScheme = fromColorSchemeState(preferenceColorSchemeState)) {
        DynamicThemeAppScreen(
            windowSizeClass = windowSizeClass,
            onChangeColorScheme = { sourceColorArgb, uiColorSchemeVariant ->
                Log.d(
                    APP_TAG,
                    "Changing color scheme to source $sourceColorArgb scheme $uiColorSchemeVariant"
                )
                preferencesViewModel.setSourceColorPreference(
                    sourceColorArgb,
                    uiColorSchemeVariant.toVariant()
                )
            },
            modifier = modifier
        )
    }
}

@Composable
fun DynamicThemeAppScreen(
    windowSizeClass: WindowSizeClass,
    onChangeColorScheme: (Int, UiColorSchemeVariant) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    DynamicThemeNavHost(
        navController = navController,
        onChangeColorScheme = onChangeColorScheme,
        windowSizeClass = windowSizeClass,
        startDestination = HomeDestination.route,
        modifier = modifier
    )
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
