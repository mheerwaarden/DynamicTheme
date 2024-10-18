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

package com.github.mheerwaarden.dynamictheme.ui.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.github.mheerwaarden.dynamictheme.ui.AppViewModelProvider
import com.github.mheerwaarden.dynamictheme.ui.home.HomeDestination
import com.github.mheerwaarden.dynamictheme.ui.home.HomeScreen
import com.github.mheerwaarden.dynamictheme.ui.screen.DynamicThemeViewModel
import com.github.mheerwaarden.dynamictheme.ui.screen.ExamplesDestination
import com.github.mheerwaarden.dynamictheme.ui.screen.ExamplesScreen
import com.github.mheerwaarden.dynamictheme.ui.screen.ImagePickerDestination
import com.github.mheerwaarden.dynamictheme.ui.screen.ImagePickerScreen
import com.github.mheerwaarden.dynamictheme.ui.screen.ColorSchemeVariantDestination
import com.github.mheerwaarden.dynamictheme.ui.screen.ColorSchemeVariantChooserScreen
import com.github.mheerwaarden.dynamictheme.ui.screen.UiColorSchemeVariant

/**
 * Provides Navigation graph for the application.
 */
@Composable
fun DynamicThemeNavHost(
    navController: NavHostController,
    onChangeColorScheme: (Int, UiColorSchemeVariant) -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    startDestination: String = HomeDestination.route,
    themeViewModel: DynamicThemeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val themeState by themeViewModel.uiState.collectAsState()

    NavHost(
        navController = navController, startDestination = startDestination, modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                updateColorScheme = themeViewModel::updateColorScheme,
                navigateToImagePicker = { navController.navigate(ImagePickerDestination.route) },
                navigateToThemeChooser = { navController.navigate(ColorSchemeVariantDestination.route) },
                navigateToExamples = { navController.navigate(ExamplesDestination.route) }
            )
        }
        composable(route = ImagePickerDestination.route) {
            ImagePickerScreen(
                themeState = themeState,
                windowSizeClass = windowSizeClass,
                onUpdateColorScheme = { sourceColorArgb, uiColorSchemeVariant ->
                    themeViewModel.updateColorScheme(sourceColorArgb, uiColorSchemeVariant)
                    onChangeColorScheme(sourceColorArgb, uiColorSchemeVariant)
                },
                navigateToThemeChooser = { navController.navigate(ColorSchemeVariantDestination.route) },
                navigateBack = { navController.popBackStack() }
            )
        }
        composable(route = ColorSchemeVariantDestination.route) {
            ColorSchemeVariantChooserScreen(
                themeState = themeState,
                windowSizeClass = windowSizeClass,
                onUpdateTheme = { sourceColorArgb, uiColorSchemeVariant ->
                    themeViewModel.updateColorScheme(sourceColorArgb, uiColorSchemeVariant)
                    onChangeColorScheme(sourceColorArgb, uiColorSchemeVariant)
                },
                navigateToExamples = { navController.navigate(ExamplesDestination.route) },
                navigateBack = { navController.popBackStack() }
            )
        }
        composable(route = ExamplesDestination.route) {
            ExamplesScreen(
                themeState = themeState,
                windowSizeClass = windowSizeClass,
                navigateHome = { navController.navigate(HomeDestination.route) },
                navigateBack = { navController.popBackStack() }
            )
        }
    }
}
