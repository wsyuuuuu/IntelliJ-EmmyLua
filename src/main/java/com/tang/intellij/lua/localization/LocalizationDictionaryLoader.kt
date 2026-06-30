/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.localization

import com.intellij.openapi.project.Project
import com.tang.intellij.lua.LuaBundle
import java.io.File

data class LocalizationDictionaryLoadResult(
    val success: Boolean,
    val entryCount: Int = 0,
    val resolvedPath: String = "",
    val errorMessage: String? = null,
    val dictionary: Map<String, String> = emptyMap()
) {
    val statusMessage: String
        get() {
            if (success) {
                return LuaBundle.message(
                    "ui.settings.localization.status.loaded",
                    entryCount,
                    resolvedPath
                )
            }
            return errorMessage ?: LuaBundle.message("ui.settings.localization.status.not_loaded")
        }
}

object LocalizationDictionaryLoader {
    fun load(path: String, project: Project?): LocalizationDictionaryLoadResult {
        if (path.isBlank()) {
            return LocalizationDictionaryLoadResult(
                success = false,
                errorMessage = LuaBundle.message("ui.settings.localization.error.empty_path")
            )
        }

        val resolvedPath = LocalizationPathResolver.resolve(path, project)
        if (resolvedPath.isBlank()) {
            return LocalizationDictionaryLoadResult(
                success = false,
                errorMessage = LuaBundle.message("ui.settings.localization.error.empty_path")
            )
        }

        if (path.contains("\$PROJECT_DIR$") && project == null) {
            return LocalizationDictionaryLoadResult(
                success = false,
                resolvedPath = resolvedPath,
                errorMessage = LuaBundle.message("ui.settings.localization.error.no_project")
            )
        }

        val file = File(resolvedPath)
        if (!file.isFile) {
            return LocalizationDictionaryLoadResult(
                success = false,
                resolvedPath = resolvedPath,
                errorMessage = LuaBundle.message("ui.settings.localization.error.file_not_found", resolvedPath)
            )
        }

        return try {
            val dict = LubanByteBufReader.readDictionary(file.readBytes())
            LocalizationDictionaryLoadResult(
                success = true,
                entryCount = dict.size,
                resolvedPath = resolvedPath,
                dictionary = dict
            )
        } catch (e: Exception) {
            LocalizationDictionaryLoadResult(
                success = false,
                resolvedPath = resolvedPath,
                errorMessage = LuaBundle.message("ui.settings.localization.error.parse_failed", e.message ?: e.javaClass.simpleName)
            )
        }
    }
}
