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

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.github.mheerwaarden.dynamictheme.APP_TAG
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeViewModel
import com.github.mheerwaarden.dynamictheme.ui.home.HomeDestination
import com.github.mheerwaarden.dynamictheme.ui.home.HomeScreen
import com.github.mheerwaarden.dynamictheme.ui.screen.ColorSchemeVariantChooserScreen
import com.github.mheerwaarden.dynamictheme.ui.screen.ColorSchemeVariantDestination
import com.github.mheerwaarden.dynamictheme.ui.screen.DynamicThemeDetailDestination
import com.github.mheerwaarden.dynamictheme.ui.screen.DynamicThemeDetailScreen
import com.github.mheerwaarden.dynamictheme.ui.screen.ImagePickerDestination
import com.github.mheerwaarden.dynamictheme.ui.screen.ImagePickerScreen
import com.github.mheerwaarden.dynamictheme.ui.screen.LatestDetailScreen

/**
 * Provides Navigation graph for the application.
 */
@Composable
fun DynamicThemeNavHost(
    navController: NavHostController,
    themeViewModel: DynamicThemeViewModel,
    modifier: Modifier = Modifier,
    startDestination: String = HomeDestination.route,
) {

    NavHost(
        navController = navController, startDestination = startDestination, modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                navigateToImagePicker = { navController.navigate(ImagePickerDestination.route) },
                navigateToDetail = {
                    Log.d(APP_TAG + "_Route", "Home -> Detail for ID: $it")
                    if (it <= 0) {
                        navController.navigate(DynamicThemeDetailDestination.route)
                    } else {
                        navController.navigate("${DynamicThemeDetailDestination.route}/$it")
                    }
                },
                themeViewModel = themeViewModel
            )
        }
        composable(route = ImagePickerDestination.route) {
            ImagePickerScreen(
                navigateToThemeChooser = { navController.navigate(ColorSchemeVariantDestination.route) },
                navigateBack = { navController.popBackStack() },
                themeViewModel = themeViewModel
            )
        }
        composable(route = ColorSchemeVariantDestination.route) {
            ColorSchemeVariantChooserScreen(
                navigateToExamples = { navController.navigate(DynamicThemeDetailDestination.route) },
                navigateBack = { navController.popBackStack() },
                themeViewModel = themeViewModel
            )
        }
        composable(route = DynamicThemeDetailDestination.route) {
            // Latest detail that has not been saved to the database
            LatestDetailScreen(
                navigateHome = { navController.navigate(HomeDestination.route) },
                navigateBack = { navController.popBackStack() },
                themeViewModel = themeViewModel
            )
        }
        composable(
            route = DynamicThemeDetailDestination.routeWithArgs,
            arguments = listOf(navArgument(DynamicThemeDetailDestination.themeIdArg) {
                type = NavType.LongType
            })
        ) {
            // Existing theme in database, no update of preferences
            DynamicThemeDetailScreen(navigateHome = { navController.navigate(HomeDestination.route) },
                navigateBack = { navController.popBackStack() },
                themeViewModel = themeViewModel
            )
        }

    }
}
