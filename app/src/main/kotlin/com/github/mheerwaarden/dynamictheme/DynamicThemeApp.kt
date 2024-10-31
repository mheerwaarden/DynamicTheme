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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.github.mheerwaarden.dynamictheme.ui.AppViewModelProvider
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeUiState
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeViewModel
import com.github.mheerwaarden.dynamictheme.ui.LoadingState
import com.github.mheerwaarden.dynamictheme.ui.ProgressIndicator
import com.github.mheerwaarden.dynamictheme.ui.navigation.DynamicThemeNavHost
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
    when (val result = themeViewModel.loadingState) {
        is LoadingState.Loading -> {
            /* Show progress indicator */
            Log.d(APP_TAG, "DynamicThemeApp: Busy loading theme")
            ProgressScreen(action = stringResource(R.string.loading))
        }

        is LoadingState.Success -> {
            /* UiState is updated successfully, display data */
            Log.d(APP_TAG, "DynamicThemeApp: Success loading theme")

            // Prepare theme view model
            themeViewModel.updateWindowSizeClass(windowSizeClass)
            themeViewModel.onException = { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() }

            val themeState by themeViewModel.uiState.collectAsState()
            Log.d(
                APP_TAG,
                "DynamicThemeApp: Using theme ${themeState.name} from color ${themeState.sourceColorArgb} and variant ${themeState.uiColorSchemeVariant}"
            )

            DynamicThemeAppScreen(themeState = themeState,
                onResetState = { themeViewModel.resetState() },
                onNameChange = { name -> themeViewModel.updateName(name) },
                onColorSchemeChange = { sourceColorArgb, uiColorSchemeVariant ->
                    themeViewModel.updateColorScheme(sourceColorArgb, uiColorSchemeVariant)
                },
                onSave = { themeViewModel.upsertDynamicTheme() },
                modifier = modifier
            )
        }

        is LoadingState.Failure -> {
            /* Handle error */
            Log.d(APP_TAG, "DynamicThemeApp: Error loading theme")
            ErrorScreen(
                message = result.error.message ?: stringResource(R.string.unknown_error),
                retryAction = { themeViewModel.updateFromPreferences() },
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        }
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

@Composable
private fun ProgressScreen(action: String) {
    ProgressIndicator(
        action = action, modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    )
}

@Composable
fun ErrorScreen(message: String, retryAction: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_broken_image),
            contentDescription = stringResource(R.string.error),
            modifier = Modifier.size(dimensionResource(R.dimen.error_image_size))
        )
        Text(text = message, modifier = Modifier.padding(16.dp))
        Button(onClick = retryAction) {
            Text(stringResource(R.string.retry))
        }
    }
}
