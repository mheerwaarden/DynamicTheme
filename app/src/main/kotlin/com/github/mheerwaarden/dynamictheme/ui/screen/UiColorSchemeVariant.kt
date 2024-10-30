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

import com.github.mheerwaarden.dynamictheme.R
import dynamiccolor.Variant

/**
 * Dynamic color scheme variants as supplied by the material dynamic color library, paired with a
 * string resource id for use in the UI.
 * Must be in the same order as enum [Variant] in dynamic color library .
 */
enum class UiColorSchemeVariant(val nameResId: Int) {
    Monochrome(R.string.monochrome),
    Neutral(R.string.neutral),
    TonalSpot(R.string.tonal_spot),
    Vibrant(R.string.vibrant),
    Expressive(R.string.expressive),
    Fidelity(R.string.fidelity),
    Content(R.string.content),
    Rainbow(R.string.rainbow),
    FruitSalad(R.string.fruit_salad);

    fun toVariant(): Variant = Variant.entries[ordinal]

    companion object {
        fun fromVariant(variant: Variant): UiColorSchemeVariant = entries[variant.ordinal]
    }
}