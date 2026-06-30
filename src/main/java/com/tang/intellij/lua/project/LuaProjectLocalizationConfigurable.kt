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

package com.tang.intellij.lua.project

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

class LuaProjectLocalizationConfigurable(private val project: Project) : SearchableConfigurable {
    private var editor: LocalizationSettingsEditor? = null

    override fun getId(): String = "preferences.Lua.Localization"

    @Nls
    override fun getDisplayName(): String = "Localization"

    override fun createComponent(): JComponent {
        editor = LocalizationSettingsEditor.createProjectEditor(project)
        return editor!!.panel
    }

    override fun isModified(): Boolean = editor?.isModified() == true

    override fun apply() {
        editor?.applyProjectSettings(LuaProjectLocalizationSettings.getInstance(project))
        DaemonCodeAnalyzer.getInstance(project).restart()
    }

    override fun reset() {
        editor?.resetProjectSettings(LuaSettings.instance, LuaProjectLocalizationSettings.getInstance(project))
    }
}
