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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.mheerwaarden.dynamictheme.material.color.utils.ColorExtractor
import com.github.mheerwaarden.dynamictheme.ui.AppViewModelProvider
import com.github.mheerwaarden.dynamictheme.ui.ColorSchemeStateSaver
import com.github.mheerwaarden.dynamictheme.ui.PreferencesViewModel
import com.github.mheerwaarden.dynamictheme.ui.fromColorSchemeState
import com.github.mheerwaarden.dynamictheme.ui.home.HomeDestination
import com.github.mheerwaarden.dynamictheme.ui.navigation.DynamicThemeNavHost
import com.github.mheerwaarden.dynamictheme.ui.theme.DynamicThemeTheme
import com.github.mheerwaarden.dynamictheme.ui.theme.getDefaultColorScheme
import com.github.mheerwaarden.dynamictheme.ui.toColorSchemeState
import dynamiccolor.DynamicScheme

const val APP_TAG = "DynamicTheme"

@Composable
fun DynamicThemeApp(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    preferencesViewModel: PreferencesViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val preferences = preferencesViewModel.preferencesState.collectAsState().value
    val defaultColorSchemeState = getDefaultColorScheme().toColorSchemeState()
    var sourceColor: Int by rememberSaveable { mutableIntStateOf(0) }
    var colorSchemeState by rememberSaveable(stateSaver = ColorSchemeStateSaver) {
        mutableStateOf(defaultColorSchemeState)
    }
    DynamicThemeTheme(colorScheme = fromColorSchemeState(colorSchemeState)) {
        // Force an initial change of the colorSchemeState
        if (sourceColor == 0) {
            sourceColor = preferences.sourceColor
            Log.d(APP_TAG, "Initial: Using source color preference: $sourceColor")
            if (sourceColor != 0) {
                colorSchemeState =
                        ColorExtractor.createDynamicColorScheme(sourceColor).toColorSchemeState()
            }
        }
        Log.d(APP_TAG, "Using theme based on primary color ${colorSchemeState.primary}")
        DynamicThemeAppScreen(
            windowSizeClass = windowSizeClass,
            onChangeColorScheme = {
                Log.d(
                    APP_TAG,
                    "Changing color scheme to source ${it.sourceColorArgb} primary ${it.primary}"
                )
                colorSchemeState = it.toColorSchemeState()
                preferencesViewModel.setSourceColorPreference(it.sourceColorArgb)
            },
            modifier = modifier
        )
    }
}

@Composable
fun DynamicThemeAppScreen(
    windowSizeClass: WindowSizeClass,
    onChangeColorScheme: (DynamicScheme) -> Unit,
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
    actions: @Composable() (RowScope.() -> Unit) = {},
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
