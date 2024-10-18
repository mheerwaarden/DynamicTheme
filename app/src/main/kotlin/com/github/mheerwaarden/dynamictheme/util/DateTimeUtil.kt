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

package com.github.mheerwaarden.dynamictheme.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Now in UTC milliseconds */
fun nowMillis(): Long = LocalDateTime.now().toEpochMilli()

/** Format UTC milliseconds as date and time according to the short system default format */
fun formatUtcMillis(utcMillis: Long?): String {
    if (utcMillis == null) return ""
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM)
    return ofEpochMilli(utcMillis).format(formatter)
}

/** Convert UTC milliseconds to LocalDateTime in the default time zone */
fun ofEpochMilli(utcMillis: Long): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(utcMillis), ZoneId.systemDefault())

/** Convert LocalDateTime to UTC milliseconds */
fun LocalDateTime.toEpochMilli(): Long =
        toInstant(ZoneId.systemDefault().rules.getOffset(this)).toEpochMilli()

/** Format a LocalDateTime according to the short system default format */
fun LocalDateTime.formatDateTime(): String = format(
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM)
)

/** Format a LocalDateTime as a date according to the short system default format */
fun LocalDateTime.formatDate(): String =
        toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))

/** Format a LocalDateTime as a time according to the short system default format */
fun LocalDateTime.formatTime(): String =
        toLocalTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM))