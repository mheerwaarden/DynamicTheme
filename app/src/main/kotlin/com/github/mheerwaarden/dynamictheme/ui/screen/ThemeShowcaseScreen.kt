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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.github.mheerwaarden.dynamictheme.R
import com.github.mheerwaarden.dynamictheme.material.color.utils.ColorExtractor
import com.github.mheerwaarden.dynamictheme.ui.ColorSchemeState
import com.github.mheerwaarden.dynamictheme.ui.component.DateField
import com.github.mheerwaarden.dynamictheme.ui.component.InputField
import com.github.mheerwaarden.dynamictheme.ui.component.TimeField
import com.github.mheerwaarden.dynamictheme.ui.fromColorSchemeState
import com.github.mheerwaarden.dynamictheme.ui.theme.DynamicThemeTheme
import com.github.mheerwaarden.dynamictheme.ui.theme.getDefaultColorScheme
import com.github.mheerwaarden.dynamictheme.ui.toColorSchemeState
import palettes.TonalPalette
import java.time.LocalDateTime

/**
 * Data class to represent a color with its name.
 * If the [onColor] is not provided, the contrast color will be used. This is white or black,
 * depending on which one gives the best contrast.
 */
data class ColorItem(
    /** The name for the color */
    val name: String,
    /** The color itself */
    val color: Color,
    /** The color to use on top of the [color] */
    val onColor: Color? = null,
)

@Composable
fun ThemeShowcaseScreen(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    lightColorSchemeState: ColorSchemeState = getDefaultColorScheme(
        darkTheme = false, dynamicColor = false
    ).toColorSchemeState(),
    darkColorSchemeState: ColorSchemeState = getDefaultColorScheme(
        darkTheme = true, dynamicColor = false
    ).toColorSchemeState(),
) {
    DynamicThemeTheme(colorScheme = fromColorSchemeState(lightColorSchemeState)) {
        ExpandableSections(
            sections = listOf(
                stringResource(R.string.light_scheme) to {
                    ColorSchemeShowcaseScreen(windowSizeClass)
                },
                stringResource(R.string.dark_scheme) to {
                    DarkColorSchemeShowcaseScreen(darkColorSchemeState, windowSizeClass)
                },
                stringResource(R.string.components) to { ComponentShowcaseScreen() },
                stringResource(R.string.tonal_palettes) to {
                    TonalPaletteShowcaseScreen(windowSizeClass)
                }
            ), modifier = modifier.fillMaxSize()
        )
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
                .padding(dimensionResource(R.dimen.padding_small))) {
                Text(title)
                Spacer(Modifier.width(dimensionResource(R.dimen.padding_small)))
                ExpandCollapseIcon(isExpanded = expandedSectionIndex == index)
            }

            AnimatedVisibility(
                visible = expandedSectionIndex == index, modifier = Modifier.weight(1f)
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
fun ColorSchemeShowcaseScreen(windowSizeClass: WindowSizeClass, modifier: Modifier = Modifier) {
    Column(
        modifier.padding(dimensionResource(R.dimen.padding_small))
    ) {
        ColorGrid(
            colorsLayout = colorsLayout(),
            isCompactWidth = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact,
        )
    }
}

/**
 * Dark theme version of [ColorSchemeShowcaseScreen] by surrounding the call with the
 * [DynamicThemeTheme] set to the dark color scheme.
 */
@Composable
fun DarkColorSchemeShowcaseScreen(
    darkColorScheme: ColorSchemeState,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
) {
    DynamicThemeTheme(colorScheme = fromColorSchemeState(darkColorScheme)) {
        ColorSchemeShowcaseScreen(windowSizeClass, modifier)
    }
}

/**
 * The colors of the color scheme in a list of rows, each row defined by a list of [ColorItem]
 */
@Composable
private fun colorsLayout() = listOf(
    listOf(
        ColorItem(
            "Primary", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary
        ), ColorItem(
            "Secondary", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary
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
            text = text, color = textColor, fontSize = MaterialTheme.typography.labelSmall.fontSize
        )
    }
}

@Composable
fun ComponentShowcaseScreen(
    colorScheme: ColorSchemeState = getDefaultColorScheme(
        darkTheme = false, dynamicColor = false
    ).toColorSchemeState(),
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_small))
    ) {
        item {
            // Progress Bar
            LinearProgressIndicator(
                color = Color(colorScheme.primary),
                trackColor = Color(colorScheme.secondaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.padding_small))
            )
        }
        item {
            // Buttons
            Row {
                Button(onClick = { /* dummy */ }) { Text("Primary") }
                Spacer(Modifier.width(dimensionResource(R.dimen.padding_small)))
                OutlinedButton(onClick = { /* dummy */ }) { Text("Outlined") }
                Spacer(Modifier.width(dimensionResource(R.dimen.padding_small)))
                TextButton(onClick = { /* dummy */ }) { Text("Text") }
            }
        }
        item {
            // List Item
            ListItem(headlineContent = { Text("List Item") },
                leadingContent = { Icon(Icons.Filled.Check, contentDescription = null) })
        }
        item {
            // Floating Action Button
            FloatingActionButton(onClick = { /* dummy */ }) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
        item {
            InputField(
                labelId = R.string.text_input,
                value = "Text input value",
                singleLine = false,
                onValueChange = { /* dummy */ },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            DateField(
                currentDate = LocalDateTime.now(),
                onDateChange = { /* dummy */ },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            TimeField(
                currentTime = LocalDateTime.now(),
                onTimeChange = { _, _, _ -> /* dummy */ },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // TODO MH: ... add other components ... OutlineButton

    }
}

@Composable
fun getTonalPaletteColors() = mapOf(
    stringResource(R.string.primary) to TonalPalette.fromInt(MaterialTheme.colorScheme.primary.toArgb()),
    stringResource(R.string.secondary) to TonalPalette.fromInt(MaterialTheme.colorScheme.secondary.toArgb()),
    stringResource(R.string.tertiary) to TonalPalette.fromInt(MaterialTheme.colorScheme.tertiary.toArgb()),
    stringResource(R.string.neutral) to TonalPalette.fromInt(MaterialTheme.colorScheme.surface.toArgb()),
    stringResource(R.string.neutral_variant) to TonalPalette.fromInt(MaterialTheme.colorScheme.surfaceVariant.toArgb()),
    stringResource(R.string.error) to TonalPalette.fromInt(MaterialTheme.colorScheme.error.toArgb()),
)

@Composable
fun TonalPaletteShowcaseScreen(windowSizeClass: WindowSizeClass, modifier: Modifier = Modifier) {
    val isCompactWidth = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    val tonalPaletteColors = getTonalPaletteColors()
    LazyColumn(modifier = modifier.fillMaxSize()) {
        tonalPaletteColors.forEach { (name, tonalPalette) ->
            item {
                Text(
                    name, style = MaterialTheme.typography.titleSmall
                )
            }
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (tone in 100 downTo 0 step 5) {
                        val color = Color(tonalPalette.tone(tone))
                        if (isCompactWidth) ColorBox(color, color, "", Modifier.weight(1f))
                        else {
                            val onColor = ColorExtractor.getContrastColor(color)
                            ColorBox(onColor, color, tone.toString(), Modifier.weight(1f))
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(dimensionResource(R.dimen.padding_small))) }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
fun ColorShowcaseScreenPreview() {
    val windowSizeClass = WindowSizeClass.calculateFromSize(
        // Compact width, normal mobile phone
        DpSize(width = 580.dp, height = 880.dp)
    )
    ColorSchemeShowcaseScreen(windowSizeClass)
}

@Preview(showBackground = true)
@Composable
fun ComponentShowcaseScreenPreview() {
    DynamicThemeTheme {
        ComponentShowcaseScreen()
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
fun TonalPaletteScreenPreview() {
    TonalPaletteShowcaseScreen(WindowSizeClass.calculateFromSize(DpSize(580.dp, 880.dp)))
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
fun ThemeShowcaseScreenPreview() {
    val windowSizeClass = WindowSizeClass.calculateFromSize(
        // Compact width, normal mobile phone
        DpSize(width = 580.dp, height = 880.dp)
    )
    DynamicThemeTheme {
        ThemeShowcaseScreen(
            lightColorSchemeState = getDefaultColorScheme(
                darkTheme = true, dynamicColor = false
            ).toColorSchemeState(),
            windowSizeClass = windowSizeClass,
        )
    }
}