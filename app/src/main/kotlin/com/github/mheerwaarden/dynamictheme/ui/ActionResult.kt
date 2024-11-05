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

package com.github.mheerwaarden.dynamictheme.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.github.mheerwaarden.dynamictheme.APP_TAG

sealed interface ActionResultState {
    data object None : ActionResultState
    data class Busy(val data: String) : ActionResultState
    data class Success(val data: String) : ActionResultState
    data class Failure(val data : String, val error: Throwable) : ActionResultState
}

object ActionResult{
    val None: ActionResultState.None = ActionResultState.None
    val Busy: ActionResultState.Busy = ActionResultState.Busy("")
    val Success: ActionResultState.Success = ActionResultState.Success("")
    fun Failure(error: Throwable): ActionResultState.Failure = ActionResultState.Failure("", error)
}

object DeleteResult {
    val None: ActionResultState.None = ActionResultState.None
    fun Busy(id: Long): ActionResultState.Busy = ActionResultState.Busy(id.toString())
    fun Success(id: Long): ActionResultState.Success =
            ActionResultState.Success(id.toString())
    fun Failure(id: Long, error: Throwable): ActionResultState.Failure =
            ActionResultState.Failure(id.toString(), error)
}

/**
 * Show the final result of an action.
 */
@Composable
fun ShowActionResult(
    action: String,
    actionResult: ActionResultState,
    onSuccess: () -> Unit = {},
) {
    Log.d(APP_TAG, "ShowActionResult: $actionResult")

    val context = LocalContext.current
    LaunchedEffect(actionResult) {
        if (actionResult is ActionResultState.Success) {
            onSuccess()
            Log.d(APP_TAG, "$action successful")
            // Show success message using Toast
            Toast.makeText(context, "$action successful", Toast.LENGTH_SHORT).show()
        } else if (actionResult is ActionResultState.Failure) {
            val errorMessage = actionResult.error.message
            Log.d(APP_TAG, "$action failed: $errorMessage")
            // Show error message using Toast
            Toast.makeText(context, "$action failed: $errorMessage", Toast.LENGTH_LONG).show()
        }
    }
}