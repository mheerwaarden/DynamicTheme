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

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class Zipper(zipFileName: String) : AutoCloseable {

    private val zipStream = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFileName)))

    fun add(fileName: String, data: List<String>) {
        val entry = ZipEntry(fileName.substringAfterLast(File.separator))
        zipStream.putNextEntry(entry)
        data.forEach {
            zipStream.write("$it\n".toByteArray())
        }
    }

    override fun close() {
        zipStream.close()
    }
}