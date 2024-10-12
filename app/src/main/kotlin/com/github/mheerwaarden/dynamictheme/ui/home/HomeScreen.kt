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

package com.github.mheerwaarden.dynamictheme.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mheerwaarden.dynamictheme.DynamicThemeTopAppBar
import com.github.mheerwaarden.dynamictheme.R
import com.github.mheerwaarden.dynamictheme.ui.AppViewModelProvider
import com.github.mheerwaarden.dynamictheme.ui.PreferencesViewModel
import com.github.mheerwaarden.dynamictheme.ui.navigation.NavigationDestination
import com.github.mheerwaarden.dynamictheme.ui.theme.DynamicThemeTheme

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

/**
 * Entry route for Home screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToImagePicker: () -> Unit,
    navigateToExamples: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            DynamicThemeTopAppBar(
                title = stringResource(HomeDestination.titleRes),
                canNavigateBack = false,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToImagePicker,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.menu)
                )
            }
        },
    ) { innerPadding ->
        HomeBody(
            navigateToImagePicker = navigateToImagePicker,
            navigateToExamples = navigateToExamples,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun HomeBody(
    navigateToImagePicker: () -> Unit,
    navigateToExamples: () -> Unit,
    modifier: Modifier = Modifier,
    preferencesViewModel: PreferencesViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = dimensionResource(id = R.dimen.padding_small), alignment = Alignment.Top
        ),
        modifier = modifier
    ) {
        Text("The current theme colors")
        Box(
            Modifier
                .fillMaxWidth()
                .height(height = dimensionResource(id = R.dimen.padding_large))
                .background(color = Color(preferencesViewModel.preferencesState.collectAsState().value.sourceColor))
        ) { Text("Source color") }
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_minimum))
        ) {
            Box(
                Modifier
                    .background(color = MaterialTheme.colorScheme.primary)
                    .height(height = dimensionResource(id = R.dimen.padding_large))
            ) { Text("Primary") }
            Box(
                Modifier
                    .background(color = MaterialTheme.colorScheme.secondary)
                    .height(height = dimensionResource(id = R.dimen.padding_large))
            ) { Text("Secondary") }
            Box(
                Modifier
                    .background(color = MaterialTheme.colorScheme.tertiary)
                    .height(height = dimensionResource(id = R.dimen.padding_large))
            ) { Text("Tertiary") }
            Box(
                Modifier
                    .background(color = MaterialTheme.colorScheme.surface)
                    .height(height = dimensionResource(id = R.dimen.padding_large))
            ) { Text("Surface") }
            Box(
                Modifier
                    .background(color = MaterialTheme.colorScheme.background)
                    .height(height = dimensionResource(id = R.dimen.padding_large))
            ) { Text("Background") }
        }
        Button(
            onClick = navigateToImagePicker,
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.color_extractor_for_image)) }
        Button(
            onClick = navigateToExamples,
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.show_examples)) }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeBodyPreview() {
    DynamicThemeTheme {
        HomeBody(
            navigateToImagePicker = {},
            navigateToExamples = {}
        )
    }
}