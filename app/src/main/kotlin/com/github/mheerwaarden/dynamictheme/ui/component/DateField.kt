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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.mheerwaarden.dynamictheme.R
import com.github.mheerwaarden.dynamictheme.util.formatDate
import com.github.mheerwaarden.dynamictheme.util.ofEpochMilli
import com.github.mheerwaarden.dynamictheme.util.toEpochMilli
import java.time.LocalDateTime

data class DateFieldPreferences(
    val isUseKeyboard: Boolean = false,
    val onToggleKeyboard: (Boolean) -> Unit = { _ -> },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    currentDate: LocalDateTime,
    onDateChange: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
    preferences: DateFieldPreferences = DateFieldPreferences(),
) {
    // String value of the date
    var date by rememberSaveable { mutableStateOf("") }
    date = currentDate.formatDate()

    DialogField(
        labelId = R.string.schedule_date,
        value = date,
        trailingIcon = {
            Icon(
                Icons.Filled.EditCalendar,
                contentDescription = stringResource(R.string.show_date_picker),
            )
        },
        modifier = modifier.fillMaxWidth()
    ) { close ->
        // State for managing date picker
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentDate.toEpochMilli(),
            initialDisplayMode = if (preferences.isUseKeyboard) DisplayMode.Input else DisplayMode.Picker
        )
        DatePickerDialog(
            onDismissRequest = { closeDialog(preferences, datePickerState, close) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedUtcMillis = datePickerState.selectedDateMillis
                        if (selectedUtcMillis != null) {
                            val selectedDate = ofEpochMilli(selectedUtcMillis)
                            date = selectedDate.formatDate()
                            onDateChange(selectedDate)
                        }
                        closeDialog(preferences, datePickerState, close)
                    }
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { closeDialog(preferences, datePickerState, close) }
                ) { Text(stringResource(R.string.cancel)) }
            },
        ) { DatePicker(state = datePickerState) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun closeDialog(
    preferences: DateFieldPreferences,
    datePickerState: DatePickerState,
    close: () -> Unit,
) {
    preferences.onToggleKeyboard(datePickerState.displayMode == DisplayMode.Input)
    close()
}