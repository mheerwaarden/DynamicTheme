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

package com.github.mheerwaarden.dynamictheme.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import dynamiccolor.Variant
import java.time.LocalDateTime

/**
 * Entity DynamicTheme contains the data to generate the theme.
 * The source RGB color and the scheme variant can be used to generate the color scheme.
 * Some colors from the light color scheme are cached to provide a preview on the overview screen
 * without the need to generate the color scheme.
 */
@Entity(tableName = "dynamicTheme")
data class DynamicTheme(
    /**
     * Unique ID for the reminder, doubles as request code for PendingIntent
     */
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    /**
     * Title or name of the theme
     */
    val name: String = "",
    /**
     * The source color for the color scheme generation
     */
    val sourceArgb: Int = 0,
    /**
     * The variant of the color scheme.
     */
    val colorSchemeVariant: Variant = Variant.TONAL_SPOT,
    /**
     * The generated Primary color of the generated color scheme.
     */
    val primaryArgb: Int = 0,
    /**
     * The generated onPrimary color of the generated color scheme.
     */
    val onPrimaryArgb: Int = 0,
    /**
     * The generated Secondary color of the generated color scheme.
     */
    val secondaryArgb: Int = 0,
    /**
     * The generated onSecondary color of the generated color scheme.
     */
    val onSecondaryArgb: Int = 0,
    /**
     * The generated Tertiary color of the generated color scheme.
     */
    val tertiaryArgb: Int = 0,
    /**
     * The generated onTertiary color of the generated color scheme.
     */
    val onTertiaryArgb: Int = 0,
    /**
     * The generated Surface color of the generated color scheme.
     */
    val surfaceArgb: Int = 0,
    /**
     * The generated onSurface color of the generated color scheme.
     */
    val onSurfaceArgb: Int = 0,
    /**
     * The generated SurfaceVariant color of the generated color scheme.
     */
    val surfaceVariantArgb: Int = 0,
    /**
     * The generated onSurfaceVariant color of the generated color scheme.
     */
    val onSurfaceVariantArgb: Int = 0,
    /**
     * The generated Error color of the generated color scheme.
     */
    val errorArgb: Int = 0,
    /**
     * The generated onError color of the generated color scheme.
     */
    val onErrorArgb: Int = 0,
    /**
     * Timestamp of the last update
     */
    val timestamp: LocalDateTime = LocalDateTime.now(),
)

