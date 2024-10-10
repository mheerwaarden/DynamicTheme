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
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.github.mheerwaarden.dynamictheme.ui.home.HomeDestination
import com.github.mheerwaarden.dynamictheme.ui.home.HomeScreen
import com.github.mheerwaarden.dynamictheme.ui.screen.ColorExtractionStrategy
import com.github.mheerwaarden.dynamictheme.ui.screen.ColorExtractorDestination
import com.github.mheerwaarden.dynamictheme.ui.screen.ExamplesDestination
import com.github.mheerwaarden.dynamictheme.ui.screen.ExamplesScreen
import com.github.mheerwaarden.dynamictheme.ui.screen.PaletteDestination
import com.github.mheerwaarden.dynamictheme.ui.screen.PaletteScreen
import dynamiccolor.DynamicScheme

/**
 * Provides Navigation graph for the application.
 */
@Composable
fun DynamicThemeNavHost(
    navController: NavHostController,
    onChangeColorScheme: (DynamicScheme) -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    startDestination: String = HomeDestination.route,
) {
    NavHost(
        navController = navController, startDestination = startDestination, modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                navigateToPalette = { navController.navigate(PaletteDestination.route) },
                navigateToColorExtractor = { navController.navigate(ColorExtractorDestination.route) },
                navigateToExamples = { navController.navigate(ExamplesDestination.route) }
            )
        }
        composable(route = PaletteDestination.route) {
            PaletteScreen(
                windowSizeClass = windowSizeClass,
                titleResId = PaletteDestination.titleRes,
                colorExtractionStrategy = ColorExtractionStrategy.Palette,
                onChangeColorScheme = onChangeColorScheme,
                navigateBack = { navController.popBackStack() })
        }
        composable(route = ColorExtractorDestination.route) {
            PaletteScreen(
                windowSizeClass = windowSizeClass,
                titleResId = ColorExtractorDestination.titleRes,
                colorExtractionStrategy = ColorExtractionStrategy.ColorExtractor,
                onChangeColorScheme = onChangeColorScheme,
                navigateBack = { navController.popBackStack() })
        }
        composable(route = ExamplesDestination.route) {
            ExamplesScreen(
                windowSizeClass = windowSizeClass,
                navigateBack = { navController.popBackStack() })
        }
    }
}
