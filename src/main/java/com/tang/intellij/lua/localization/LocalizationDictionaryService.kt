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

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.tang.intellij.lua.LuaBundle
import com.tang.intellij.lua.project.LuaProjectLocalizationSettings
import com.tang.intellij.lua.project.LuaSettings
import java.util.concurrent.atomic.AtomicReference

@Service(Service.Level.PROJECT)
class LocalizationDictionaryService(private val project: Project) : Disposable {
    private val dictionaryRef = AtomicReference<Map<String, String>>(emptyMap())
    private var loadedPath: String? = null
    private val connections = mutableListOf<com.intellij.util.messages.MessageBusConnection>()

    init {
        project.messageBus.connect(this).also { connection ->
            connections.add(connection)
            connection.subscribe(LuaProjectLocalizationSettings.TOPIC, LocalizationSettingsListener {
                reloadFromEffectiveSettings()
            })
        }
        ApplicationManager.getApplication().messageBus.connect(this).also { connection ->
            connections.add(connection)
            connection.subscribe(LuaSettings.LOCALIZATION_SETTINGS_TOPIC, LocalizationSettingsListener {
                reloadFromEffectiveSettings()
            })
        }
        reloadFromEffectiveSettings()
    }

    fun getTranslation(key: String): String {
        return dictionaryRef.get()[key] ?: "[Missing] $key"
    }

    fun lookup(key: String): String? = dictionaryRef.get()[key]

    fun getEntryCount(): Int = dictionaryRef.get().size

    fun getLoadedPath(): String? = loadedPath

    fun isEnabled(): Boolean {
        return EffectiveLocalizationSettings.forProject(project).enabled
    }

    fun getLoadStatus(): LocalizationDictionaryLoadResult {
        val dict = dictionaryRef.get()
        val path = loadedPath
        if (dict.isNotEmpty() && !path.isNullOrBlank()) {
            return LocalizationDictionaryLoadResult(
                success = true,
                entryCount = dict.size,
                resolvedPath = path,
                dictionary = dict
            )
        }
        if (!path.isNullOrBlank()) {
            return LocalizationDictionaryLoadResult(
                success = false,
                resolvedPath = path,
                errorMessage = LuaBundle.message("ui.settings.localization.error.file_not_found", path)
            )
        }
        return LocalizationDictionaryLoadResult(success = false)
    }

    fun reloadFromEffectiveSettings(): LocalizationDictionaryLoadResult {
        val settings = EffectiveLocalizationSettings.forProject(project)
        if (!settings.enabled || settings.dictionaryFilePath.isBlank()) {
            dictionaryRef.set(emptyMap())
            loadedPath = null
            return LocalizationDictionaryLoadResult(success = false)
        }
        return reloadFromPath(settings.dictionaryFilePath)
    }

    fun reloadFromPath(path: String): LocalizationDictionaryLoadResult {
        val result = ApplicationManager.getApplication().runReadAction<LocalizationDictionaryLoadResult> {
            LocalizationDictionaryLoader.load(path, project)
        }
        if (result.success) {
            dictionaryRef.set(result.dictionary)
            loadedPath = result.resolvedPath
        } else {
            dictionaryRef.set(emptyMap())
            loadedPath = result.resolvedPath.takeIf { it.isNotBlank() }
        }
        return result
    }

    override fun dispose() {
        connections.forEach { it.disconnect() }
        connections.clear()
    }

    companion object {
        fun getInstance(project: Project): LocalizationDictionaryService {
            return project.getService(LocalizationDictionaryService::class.java)
        }
    }
}

fun interface LocalizationSettingsListener : Runnable
