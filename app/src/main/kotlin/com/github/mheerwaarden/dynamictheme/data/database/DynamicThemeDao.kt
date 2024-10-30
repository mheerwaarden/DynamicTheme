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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DynamicThemeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dynamicTheme: DynamicTheme): Long

    @Update
    suspend fun update(dynamicTheme: DynamicTheme)

    @Delete
    suspend fun delete(dynamicTheme: DynamicTheme)

    @Delete(entity = DynamicTheme::class)
    suspend fun deleteDynamicThemeById(id: Id)

    @Query("select * from dynamicTheme where id = :id")
    fun getDynamicThemeStream(id: Long): Flow<DynamicTheme>

    @Query("select * from dynamicTheme where id = :id")
    suspend fun getDynamicTheme(id: Long): DynamicTheme?

    /** Get alle dynamic themes sorted by latest update first */
    @Query("select * from dynamicTheme order by timestamp desc")
    fun getAllDynamicThemes(): Flow<List<DynamicTheme>>

    @Query("select * from dynamicTheme")
    fun getAll(): List<DynamicTheme>

}