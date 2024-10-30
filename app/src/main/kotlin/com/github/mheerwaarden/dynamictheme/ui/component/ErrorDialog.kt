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

import android.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.mheerwaarden.dynamictheme.R

@Composable
fun ErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val builder = AlertDialog.Builder(context)
    builder.setTitle(R.string.attention)
        .setMessage(stringResource(R.string.an_error_occurred, errorMessage))
        .setIcon(R.drawable.ic_broken_image)
    builder.setPositiveButton(R.string.ok) { _, _ -> onDismiss() }
    builder.show()
}