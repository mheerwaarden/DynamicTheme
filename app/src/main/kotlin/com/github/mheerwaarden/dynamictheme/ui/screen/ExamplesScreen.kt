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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.mheerwaarden.dynamictheme.DynamicThemeTopAppBar
import com.github.mheerwaarden.dynamictheme.R
import com.github.mheerwaarden.dynamictheme.ui.navigation.NavigationDestination

object ExamplesDestination : NavigationDestination {
    override val route = "theme_examples"
    override val titleRes = R.string.show_examples
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamplesScreen(
    windowSizeClass: WindowSizeClass,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            DynamicThemeTopAppBar(
                title = stringResource(ExamplesDestination.titleRes),
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        }, modifier = modifier
    ) { innerPadding ->
        ExamplesBody(
            isCompactWidth = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

@Composable
fun ExamplesBody(isCompactWidth: Boolean, modifier: Modifier = Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Text(text = "No Examples Yet")
    }
}
