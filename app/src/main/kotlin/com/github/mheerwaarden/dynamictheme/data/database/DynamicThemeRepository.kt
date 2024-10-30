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

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update, delete, and retrieve of [DynamicTheme] from a given data source.
 */
interface DynamicThemeRepository {
    /**
     * Retrieve all the dynamicThemes from the given data source.
     */
    fun getAllDynamicThemesStream(): Flow<List<DynamicTheme>>

    /**
     * Retrieve an dynamicTheme from the given data source that matches with the [id].
     */
    fun getDynamicThemeStream(id: Long): Flow<DynamicTheme?>

    /**
     * Retrieve an dynamicTheme from the given data source that matches with the [id].
     */
    suspend fun getDynamicTheme(id: Long): DynamicTheme?

    /**
     * Insert dynamicTheme in the data source
     */
    suspend fun insertDynamicTheme(dynamicTheme: DynamicTheme): Long

    /**
     * Delete dynamicTheme from the data source
     */
    suspend fun deleteDynamicTheme(dynamicTheme: DynamicTheme)

    /**
     * Delete dynamicTheme from the data source
     */
    suspend fun deleteDynamicThemeById(id: Id)

    /**
     * Update dynamicTheme in the data source
     */
    suspend fun updateDynamicTheme(dynamicTheme: DynamicTheme)

    /**
     * Get all dynamicThemes from the data source as List
     */
    suspend fun getAll(): List<DynamicTheme>

}
