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

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mheerwaarden.dynamictheme.APP_TAG
import com.github.mheerwaarden.dynamictheme.DynamicThemeTopAppBar
import com.github.mheerwaarden.dynamictheme.R
import com.github.mheerwaarden.dynamictheme.data.database.DynamicTheme
import com.github.mheerwaarden.dynamictheme.ui.AppViewModelProvider
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeUiState
import com.github.mheerwaarden.dynamictheme.ui.navigation.NavigationDestination
import com.github.mheerwaarden.dynamictheme.ui.screen.ThemeCard
import com.github.mheerwaarden.dynamictheme.ui.theme.DynamicThemeAppTheme

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
    themeState: DynamicThemeUiState,
    navigateToImagePicker: () -> Unit,
    navigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by homeViewModel.uiState.collectAsState()
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
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_theme)
                )
            }
        },
    ) { innerPadding ->
        HomeListScreen(
            themeState = themeState,
            dynamicThemeList = uiState,
            navigateToDetail = navigateToDetail,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun HomeListScreen(
    themeState: DynamicThemeUiState,
    dynamicThemeList: List<DynamicTheme>,
    navigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        val latestTheme = dynamicThemeList.getTheme(themeState.id) ?: themeState.toDynamicTheme()
        item {
            Log.d(APP_TAG, "HomeListScreen: latest theme ${latestTheme.id} - ${latestTheme.name}")
            ThemeCard(dynamicTheme = latestTheme,
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.padding_small))
                    .fillMaxWidth()
                    .clickable { navigateToDetail(latestTheme.id) })
        }
        if (dynamicThemeList.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_dynamic_themes_description),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        } else {
            items(items = dynamicThemeList, key = { it.id }) { theme ->
                if (theme.id != latestTheme.id) {
                    Log.d(APP_TAG, "HomeListScreen: saved theme ${theme.id} - ${theme.name}")
                    ThemeCard(dynamicTheme = theme,
                        modifier = Modifier
                            .padding(dimensionResource(R.dimen.padding_small))
                            .fillMaxWidth()
                            .clickable { navigateToDetail(theme.id) })
                }
            }
        }
    }
}

private fun List<DynamicTheme>.getTheme(id: Long): DynamicTheme? {
    forEach {
        if (it.id == id) {
            return it
        }
    }
    return null
}

@Preview(showBackground = true)
@Composable
fun HomeBodyPreview() {
    DynamicThemeAppTheme {
        HomeListScreen(
            themeState = DynamicThemeUiState(),
            dynamicThemeList = emptyList(),
            navigateToDetail = {},
        )
    }
}
