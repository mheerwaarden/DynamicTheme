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
import androidx.compose.material.icons.outlined.Add
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mheerwaarden.dynamictheme.APP_TAG
import com.github.mheerwaarden.dynamictheme.DynamicThemeTopAppBar
import com.github.mheerwaarden.dynamictheme.R
import com.github.mheerwaarden.dynamictheme.data.database.DynamicTheme
import com.github.mheerwaarden.dynamictheme.data.preferences.INVALID
import com.github.mheerwaarden.dynamictheme.ui.ActionResult
import com.github.mheerwaarden.dynamictheme.ui.ActionResultState
import com.github.mheerwaarden.dynamictheme.ui.AppViewModelProvider
import com.github.mheerwaarden.dynamictheme.ui.DeleteResult
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeUiState
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeViewModel
import com.github.mheerwaarden.dynamictheme.ui.ShowActionResult
import com.github.mheerwaarden.dynamictheme.ui.navigation.NavigationDestination
import com.github.mheerwaarden.dynamictheme.ui.screen.LoadingScreen
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
    themeViewModel: DynamicThemeViewModel,
    navigateToImagePicker: () -> Unit,
    navigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
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
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.add_theme)
                )
            }
        },
    ) { innerPadding ->
        LoadingScreen(loadingViewModel = homeViewModel, modifier = Modifier.padding(innerPadding)) {
            val homeState by homeViewModel.homeState.collectAsState()
            val deleteResult by themeViewModel.deleteResult.collectAsStateWithLifecycle(
                initialValue = DeleteResult.None
            )

            HomeListScreen(
                themeState = themeViewModel.uiState,
                deleteResult = deleteResult,
                dynamicThemeList = homeState,
                onDelete = themeViewModel::deleteDynamicTheme,
                navigateToDetail = navigateToDetail,
                modifier = Modifier.padding(innerPadding)
            )
        }

    }
}

@Composable
private fun HomeListScreen(
    themeState: DynamicThemeUiState,
    deleteResult: ActionResultState,
    dynamicThemeList: List<DynamicTheme>,
    onDelete: (Long) -> Unit,
    navigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    ShowActionResult(
        action = stringResource(R.string.delete),
        actionResult = deleteResult,
    )
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {

        val latestTheme = dynamicThemeList.getTheme(themeState.id) ?: themeState.toDynamicTheme()
        Log.d(
            APP_TAG, "HomeListScreen: latest theme ${latestTheme.id} - ${latestTheme.name} theme count ${dynamicThemeList.size}"
        )
        if (themeState.id != INVALID) {
            item {
                ThemeCard(dynamicTheme = latestTheme,
                    onDelete = onDelete,
                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.padding_small))
                        .fillMaxWidth()
                        .clickable { navigateToDetail(latestTheme.id) })
            }
        } else if (dynamicThemeList.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_dynamic_themes_description),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        items(items = dynamicThemeList, key = { it.id }) { theme ->
            if (theme.id != latestTheme.id) {
                Log.d(APP_TAG, "HomeListScreen: saved theme ${theme.id} - ${theme.name}")
                ThemeCard(dynamicTheme = theme,
                    onDelete = onDelete,
                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.padding_small))
                        .fillMaxWidth()
                        .clickable { navigateToDetail(theme.id) })
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
fun HomeBodyEmptyPreview() {
    DynamicThemeAppTheme {
        HomeListScreen(
            themeState = DynamicThemeUiState(),
            deleteResult = ActionResult.None,
            dynamicThemeList = emptyList(),
            onDelete = {},
            navigateToDetail = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeBodyPreview() {
    DynamicThemeAppTheme {
        val themeState = DynamicThemeUiState(id = 1, name = "Preview")
        HomeListScreen(
            themeState = themeState,
            deleteResult = ActionResult.None,
            dynamicThemeList = listOf(themeState.toDynamicTheme()),
            onDelete = {},
            navigateToDetail = {},
        )
    }
}
