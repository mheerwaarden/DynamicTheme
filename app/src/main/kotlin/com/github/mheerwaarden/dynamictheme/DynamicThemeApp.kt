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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.github.mheerwaarden.dynamictheme.ui.AppViewModelProvider
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeUiState
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeViewModel
import com.github.mheerwaarden.dynamictheme.ui.navigation.DynamicThemeNavHost
import com.github.mheerwaarden.dynamictheme.ui.screen.LoadingScreen
import com.github.mheerwaarden.dynamictheme.ui.screen.UiColorSchemeVariant
import com.github.mheerwaarden.dynamictheme.ui.theme.DynamicThemeAppTheme

const val APP_TAG = "DynamicTheme"

@Composable
fun DynamicThemeApp(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    themeViewModel: DynamicThemeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current
    LoadingScreen(loadingViewModel = themeViewModel, modifier = modifier) {
        // Prepare theme view model
        themeViewModel.updateWindowSizeClass(windowSizeClass)
        themeViewModel.onException =
                { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() }

        val themeState by themeViewModel.uiState.collectAsState()
        Log.d(
            APP_TAG,
            "DynamicThemeApp: Using theme ${themeState.name} from color ${themeState.sourceColorArgb} and variant ${themeState.uiColorSchemeVariant}"
        )

        DynamicThemeAppScreen(
            themeState = themeState,
            onResetState = { themeViewModel.resetState() },
            onNameChange = { name -> themeViewModel.updateName(name) },
            onColorSchemeChange = { sourceColorArgb, uiColorSchemeVariant ->
                themeViewModel.updateColorScheme(sourceColorArgb, uiColorSchemeVariant)
            },
            onSave = { themeViewModel.upsertDynamicTheme() },
            modifier = modifier
        )
    }
}

@Composable
fun DynamicThemeAppScreen(
    themeState: DynamicThemeUiState,
    onResetState: () -> Unit,
    onNameChange: (String) -> Unit,
    onColorSchemeChange: (Int, UiColorSchemeVariant) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DynamicThemeAppTheme() {
        val navController = rememberNavController()

        DynamicThemeNavHost(
            navController = navController,
            themeState = themeState,
            onResetState = onResetState,
            onNameChange = onNameChange,
            onColorSchemeChange = onColorSchemeChange,
            onSave = onSave,
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

