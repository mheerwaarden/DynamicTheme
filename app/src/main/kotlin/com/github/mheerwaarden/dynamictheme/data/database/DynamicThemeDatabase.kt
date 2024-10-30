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

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.mheerwaarden.dynamictheme.APP_TAG
import java.util.concurrent.Executors

private const val SQL_TAG = "${APP_TAG}_SQL"

/**
 * Database class with a singleton Instance object.
 */
@Database(entities = [DynamicTheme::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DynamicThemeDatabase : RoomDatabase() {
    companion object {
        @Volatile
        private var Instance: DynamicThemeDatabase? = null

        fun getDatabase(context: Context): DynamicThemeDatabase {
            return Instance ?: synchronized(this) {
                val isDebuggable =
                        0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
                val dbBuilder = Room.databaseBuilder(
                    context = context,
                    klass = DynamicThemeDatabase::class.java,
                    name = "dynamicTheme_database"
                )
                dbBuilder.fallbackToDestructiveMigration()
                if (isDebuggable) {
                    dbBuilder.setQueryCallback(
                        { sqlQuery, bindArgs ->
                            Log.d(SQL_TAG, "Query: $sqlQuery SQL Args: $bindArgs")
                        }, Executors.newSingleThreadExecutor()
                    )
                }
                dbBuilder.build().also { Instance = it }
            }
        }
    }

    abstract fun dynamicThemeDao(): DynamicThemeDao

}