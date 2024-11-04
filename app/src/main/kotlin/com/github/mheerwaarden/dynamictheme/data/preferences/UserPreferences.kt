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

package com.github.mheerwaarden.dynamictheme.data.preferences

import com.github.mheerwaarden.dynamictheme.ui.screen.UiColorSchemeVariant
import dynamiccolor.Variant

const val INVALID = -1L
const val NOT_SAVED = 0L

data class UserPreferences(
    val id: Long = INVALID,
    val name: String = "",
    val sourceColor: Int = -0xbd7a0c, // Default to Google Blue
    val dynamicSchemeVariant: Variant = Variant.TONAL_SPOT,
    val uiColorSchemeVariant: UiColorSchemeVariant = UiColorSchemeVariant.fromVariant(
        dynamicSchemeVariant
    ),
)
