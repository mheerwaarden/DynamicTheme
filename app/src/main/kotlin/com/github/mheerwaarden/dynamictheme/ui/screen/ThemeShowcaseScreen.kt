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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.github.mheerwaarden.dynamictheme.R
import com.github.mheerwaarden.dynamictheme.material.color.utils.ColorExtractor

// Data class to represent a color with its name
data class ColorItem(val name: String, val color: Color, val onColor: Color? = null)

@Composable
fun ColorSchemeShowcaseScreen(windowSizeClass: WindowSizeClass, modifier: Modifier = Modifier) {
    Column(
        modifier.padding(dimensionResource(R.dimen.padding_small))
    ) {
        Text(stringResource(R.string.scheme_colors), style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        ColorGrid(
            colorsLayout = colorsLayout(),
            isCompactWidth = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact,
        )
    }
}

@Composable
private fun colorsLayout() = listOf(
    listOf(
        ColorItem(
            "Primary", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary
        ), ColorItem(
            "Secondary",
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.onSecondary
        ), ColorItem(
            "Tertiary", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary
        ), ColorItem(
            "Error", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError
        )
    ),
    listOf(
        ColorItem(
            "Primary Container",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        ),
        ColorItem(
            "Secondary Container",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        ),
        ColorItem(
            "Tertiary Container",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        ),
        ColorItem(
            "Error Container",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        ),
    ),
    listOf(
        ColorItem(
            "Background",
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.onBackground
        ),
        ColorItem(
            "Surface", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface
        ),
        ColorItem(
            "Surface Variant",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        ),
        ColorItem(
            "Inverse Surface",
            MaterialTheme.colorScheme.inverseSurface,
            MaterialTheme.colorScheme.inverseOnSurface
        ),
    ),
    listOf(
        ColorItem(
            "Surface Container Lowest", MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        ColorItem(
            "Surface Container Low", MaterialTheme.colorScheme.surfaceContainerLow
        ),
        ColorItem(
            "Surface Container", MaterialTheme.colorScheme.surfaceContainer
        ),
        ColorItem(
            "Surface Container High", MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        ColorItem(
            "Surface Container Highest", MaterialTheme.colorScheme.surfaceContainerHighest
        ),
    ),
    listOf(
        ColorItem(
            "Surface Dim",
            MaterialTheme.colorScheme.surfaceDim,
        ),
        ColorItem(
            "Surface Tint",
            MaterialTheme.colorScheme.surfaceTint,
        ),
        ColorItem(
            "Surface Bright",
            MaterialTheme.colorScheme.surfaceBright,
        ),
    ),
    listOf(
        ColorItem(
            "Outline", MaterialTheme.colorScheme.outline
        ),
        ColorItem(
            "Outline Variant", MaterialTheme.colorScheme.outlineVariant
        ),
        ColorItem(
            "Inverse Primary", MaterialTheme.colorScheme.inversePrimary
        ),
        ColorItem(
            "Scrim", MaterialTheme.colorScheme.scrim
        ),
    ),
)

@Composable
fun ColorGrid(
    colorsLayout: List<List<ColorItem>>,
    isCompactWidth: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        colorsLayout.forEach { itemRow ->
            if (isCompactWidth) {
                item(key = itemRow[0].name) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        itemRow.subList(0, 2)
                            .forEach { colorItem -> ColorItemBox(colorItem, Modifier.weight(1f)) }
                    }
                }
                item(key = itemRow[2].name) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        itemRow.subList(2, itemRow.size)
                            .forEach { colorItem -> ColorItemBox(colorItem, Modifier.weight(1f)) }
                    }
                }
            } else {
                item(key = itemRow[0].name) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        itemRow.forEach { colorItem ->
                            ColorItemBox(colorItem, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorItemBox(
    colorItem: ColorItem,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ColorBox(
            textColor = colorItem.onColor ?: ColorExtractor.getContrastColor(colorItem.color),
            backgroundColor = colorItem.color,
            text = colorItem.name
        )
        if (colorItem.onColor != null) {
            ColorBox(
                textColor = colorItem.color,
                backgroundColor = colorItem.onColor,
                text = stringResource(R.string.on_color, colorItem.name),
                isSmall = true
            )
        }
    }
}

@Composable
private fun ColorBox(
    textColor: Color,
    backgroundColor: Color,
    text: String,
    modifier: Modifier = Modifier,
    isSmall: Boolean = false,
) {
    Box(
        modifier
            .height(
                height = dimensionResource(
                    if (isSmall) R.dimen.colorbox_small_height else R.dimen.colorbox_height
                )
            )
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(dimensionResource(R.dimen.padding_very_small))
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = MaterialTheme.typography.labelSmall.fontSize
        )
    }
}

@Composable
fun ComponentShowcaseScreen() {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_small))
    ) {
        item {
            Text(
                stringResource(R.string.component_showcase),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))

            // Progress Bar
            LinearProgressIndicator(
                Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.padding_small))
            )

            // Buttons
            Row {
                Button(onClick = { /* dummy */ }) { Text("Primary") }
                Spacer(Modifier.width(dimensionResource(R.dimen.padding_small)))
                OutlinedButton(onClick = { /* dummy */ }) { Text("Outlined") }
                Spacer(Modifier.width(dimensionResource(R.dimen.padding_small)))
                TextButton(onClick = { /* dummy */ }) { Text("Text") }
            }

            // List Item
            ListItem(headlineContent = { Text("List Item") },
                leadingContent = { Icon(Icons.Filled.Check, contentDescription = null) })

            // Floating Action Button
            FloatingActionButton(onClick = { /* dummy */ }) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }

            // TODO MH: ... add other components ...
        }
    }
}

@Composable
fun ExpandableSections(
    sections: List<Pair<String, @Composable () -> Unit>>,
    modifier: Modifier = Modifier,
) {
    var expandedSectionIndex by remember { mutableIntStateOf(0) }

    Column(modifier = modifier) {
        sections.forEachIndexed { index, (title, content) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .clickable {
                    expandedSectionIndex = if (expandedSectionIndex == index) -1 else index
                }
                .padding(dimensionResource(R.dimen.padding_small))
            ) {
                Text(title)
                Spacer(Modifier.width(dimensionResource(R.dimen.padding_small)))
                ExpandCollapseIcon(isExpanded = expandedSectionIndex == index)
            }

            AnimatedVisibility(
                visible = expandedSectionIndex == index,
                modifier = Modifier.weight(1f)
            ) {
                content()
            }
        }
    }
}

@Composable
fun ExpandCollapseIcon(isExpanded: Boolean, modifier: Modifier = Modifier) {
    if (isExpanded) {
        // Collapse icon
        Icon(
            imageVector = Icons.Filled.ArrowDropUp,
            contentDescription = stringResource(R.string.collapse),
            modifier = modifier
        )
    } else {
        // Expand icon
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = stringResource(R.string.expand),
            modifier = modifier
        )
    }
}

@Composable
fun ThemeShowcaseScreen(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
) {
    ExpandableSections(
        sections = listOf("Scheme Showcase" to {
            ColorSchemeShowcaseScreen(
                windowSizeClass
            )
        }, "Component Showcase" to { ComponentShowcaseScreen() }),
        modifier = modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
fun ColorShowcaseScreenPreview() {
    val windowSizeClass = WindowSizeClass.calculateFromSize(
        // Compact width, normal mobile phone
        DpSize(
            width = 580.dp, height = 880.dp
        )
    )
    ColorSchemeShowcaseScreen(windowSizeClass)
}

@Preview(showBackground = true)
@Composable
fun ComponentShowcaseScreenPreview() {
    ComponentShowcaseScreen()
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
fun ThemeShowcaseScreenPreview() {
    val windowSizeClass = WindowSizeClass.calculateFromSize(
        // Compact width, normal mobile phone
        DpSize(
            width = 580.dp, height = 880.dp
        )
    )
    ThemeShowcaseScreen(
        windowSizeClass = windowSizeClass,
    )
}