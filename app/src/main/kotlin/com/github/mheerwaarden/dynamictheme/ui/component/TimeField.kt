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

package com.github.mheerwaarden.dynamictheme.ui.component

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.mheerwaarden.dynamictheme.R
import com.github.mheerwaarden.dynamictheme.util.formatTime
import java.time.LocalDateTime

data class TimeFieldPreferences(
    val is24Hour: Boolean = false,
    val isUseKeyboard: Boolean = false,
    val onToggleKeyboard: (Boolean) -> Unit = { _ -> },
    val isHorizontalLayout: Boolean = false,
) {
    constructor(
        context: Context,
        isUseKeyboard: Boolean = false,
        onToggleKeyboard: (Boolean) -> Unit = { _ -> },
        isHorizontalLayout: Boolean = false,
    ) : this(
        is24Hour = DateFormat.is24HourFormat(context),
        isUseKeyboard = isUseKeyboard,
        onToggleKeyboard = onToggleKeyboard,
        isHorizontalLayout = isHorizontalLayout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(
    currentTime: LocalDateTime,
    onTimeChange: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    preferences: TimeFieldPreferences = TimeFieldPreferences(context),
) {
    // String value of the date
    var time by rememberSaveable { mutableStateOf("") }
    time = currentTime.formatTime()
    DialogField(
        labelId = R.string.schedule_time,
        value = time,
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = stringResource(R.string.show_time_picker),
            )
        },
        modifier = modifier.fillMaxWidth()

    ) { close ->
        ShowTimePickerDialog(
            currentTime = currentTime,
            is24Hour = preferences.is24Hour,
            isUseKeyboard = preferences.isUseKeyboard,
            onSetTime = { hour, minute ->
                time = currentTime.withHour(hour).withMinute(minute).formatTime()
                onTimeChange(hour, minute, 0)
                close()
            },
            onToggleKeyboard = preferences.onToggleKeyboard,
            onDismiss = { close() },
            layoutType = if (preferences.isHorizontalLayout) TimePickerLayoutType.Horizontal else TimePickerLayoutType.Vertical
        )
    }
}
