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
import com.tang.intellij.lua.project.LuaProjectLocalizationSettings
import com.tang.intellij.lua.project.LuaSettings

data class EffectiveLocalizationSettings(
    val enabled: Boolean,
    val dictionaryFilePath: String,
    val keyPrefix: String
) {
    companion object {
        fun forProject(project: Project): EffectiveLocalizationSettings {
            val app = LuaSettings.instance
            val projectSettings = LuaProjectLocalizationSettings.getInstance(project)
            if (!projectSettings.overrideApplicationSettings) {
                return EffectiveLocalizationSettings(
                    enabled = app.localizationHintsEnabled,
                    dictionaryFilePath = app.dictionaryFilePath,
                    keyPrefix = app.localizationKeyPrefix
                )
            }
            return EffectiveLocalizationSettings(
                enabled = projectSettings.localizationHintsEnabled ?: app.localizationHintsEnabled,
                dictionaryFilePath = projectSettings.dictionaryFilePath?.takeIf { it.isNotBlank() }
                    ?: app.dictionaryFilePath,
                keyPrefix = projectSettings.localizationKeyPrefix?.takeIf { it.isNotBlank() }
                    ?: app.localizationKeyPrefix
            )
        }
    }
}
