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

package com.tang.intellij.test.localization

import com.tang.intellij.lua.localization.EffectiveLocalizationSettings
import com.tang.intellij.lua.project.LuaProjectLocalizationSettings
import com.tang.intellij.lua.project.LuaSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EffectiveLocalizationSettingsTest {
    @Test
    fun testApplicationDefaultsWhenProjectDoesNotOverride() {
        val app = LuaSettings()
        app.localizationHintsEnabled = true
        app.dictionaryFilePath = "/tmp/app.bytes"
        app.localizationKeyPrefix = "S_"

        val projectSettings = LuaProjectLocalizationSettings()
        projectSettings.overrideApplicationSettings = false

        val effective = mergeForTest(app, projectSettings)
        assertTrue(effective.enabled)
        assertEquals("/tmp/app.bytes", effective.dictionaryFilePath)
        assertEquals("S_", effective.keyPrefix)
    }

    @Test
    fun testProjectOverride() {
        val app = LuaSettings()
        app.localizationHintsEnabled = false
        app.dictionaryFilePath = "/tmp/app.bytes"
        app.localizationKeyPrefix = "S_"

        val projectSettings = LuaProjectLocalizationSettings()
        projectSettings.overrideApplicationSettings = true
        projectSettings.localizationHintsEnabled = true
        projectSettings.dictionaryFilePath = "/tmp/project.bytes"
        projectSettings.localizationKeyPrefix = "L_"

        val effective = mergeForTest(app, projectSettings)
        assertTrue(effective.enabled)
        assertEquals("/tmp/project.bytes", effective.dictionaryFilePath)
        assertEquals("L_", effective.keyPrefix)
    }

    private fun mergeForTest(app: LuaSettings, projectSettings: LuaProjectLocalizationSettings): EffectiveLocalizationSettings {
        return if (!projectSettings.overrideApplicationSettings) {
            EffectiveLocalizationSettings(
                enabled = app.localizationHintsEnabled,
                dictionaryFilePath = app.dictionaryFilePath,
                keyPrefix = app.localizationKeyPrefix
            )
        } else {
            EffectiveLocalizationSettings(
                enabled = projectSettings.localizationHintsEnabled ?: app.localizationHintsEnabled,
                dictionaryFilePath = projectSettings.dictionaryFilePath?.takeIf { it.isNotBlank() }
                    ?: app.dictionaryFilePath,
                keyPrefix = projectSettings.localizationKeyPrefix?.takeIf { it.isNotBlank() }
                    ?: app.localizationKeyPrefix
            )
        }
    }
}
