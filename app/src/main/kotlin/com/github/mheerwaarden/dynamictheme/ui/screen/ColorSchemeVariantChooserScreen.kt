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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.github.mheerwaarden.dynamictheme.DynamicThemeTopAppBar
import com.github.mheerwaarden.dynamictheme.R
import com.github.mheerwaarden.dynamictheme.material.color.utils.ColorExtractor
import com.github.mheerwaarden.dynamictheme.ui.navigation.NavigationDestination

object ColorSchemeVariantDestination : NavigationDestination {
    override val route = "color_scheme_variant_chooser"
    override val titleRes = R.string.color_scheme_variant_chooser
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ColorSchemeVariantChooserScreen(
    themeState: DynamicThemeUiState,
    windowSizeClass: WindowSizeClass,
    onUpdateTheme: (Int, UiColorSchemeVariant) -> Unit,
    navigateToExamples: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val uiColorSchemeVariant = themeState.uiColorSchemeVariant
    val sourceColor = Color(themeState.sourceColorArgb)
    val onSourceColor = ColorExtractor.getContrastColor(sourceColor)
    val buttonContainerColor =
            if (isDarkTheme) themeState.darkColorSchemeState.primary else themeState.lightColorSchemeState.primary
    val buttonContentColor =
            if (isDarkTheme) themeState.darkColorSchemeState.onPrimary else themeState.lightColorSchemeState.onPrimary

    Scaffold(
        topBar = {
            DynamicThemeTopAppBar(
                title = stringResource(ColorSchemeVariantDestination.titleRes),
                canNavigateBack = true,
                navigateUp = navigateBack,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors().copy(
                    containerColor = sourceColor,
                    scrolledContainerColor = sourceColor,
                    navigationIconContentColor = onSourceColor,
                    titleContentColor = onSourceColor,
                    actionIconContentColor = onSourceColor
                )
            )
        }, modifier = modifier
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            ListItem(
                headlineContent = { Text("Theme type") },
                supportingContent = {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                        overflow = FlowRowOverflow.Visible,
                    ) {
                        UiColorSchemeVariant.entries.forEach { entry ->
                            FilterChip(
                                selected = uiColorSchemeVariant == entry,
                                onClick = { onUpdateTheme(themeState.sourceColorArgb, entry) },
                                label = { Text(stringResource(entry.nameResId)) },
                                leadingIcon = if (uiColorSchemeVariant == entry) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Done,
                                            contentDescription = "Selected theme",
                                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                                        )
                                    }
                                } else {
                                    null
                                },
                            )
                        }
                        Button(
                            onClick = navigateToExamples,
                            colors = ButtonColors(
                                containerColor = Color(buttonContainerColor),
                                contentColor = Color(buttonContentColor),
                                disabledContainerColor = ButtonDefaults.buttonColors().disabledContainerColor,
                                disabledContentColor = ButtonDefaults.buttonColors().disabledContentColor
                            )
                        ) {
                            Text(stringResource(R.string.select_theme))
                        }
                    }
                },
            )
            HorizontalDivider()
            ThemeShowcaseScreen(
                windowSizeClass,
                lightColorSchemeState = themeState.lightColorSchemeState,
                darkColorSchemeState = themeState.darkColorSchemeState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
@Preview(showBackground = true)
fun ThemeChooserScreenPreview() {
    ColorSchemeVariantChooserScreen(
        themeState = DynamicThemeUiState(),
        windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(580.dp, 880.dp)),
        onUpdateTheme = { _, _ -> },
        navigateToExamples = {},
        navigateBack = {}
    )
}