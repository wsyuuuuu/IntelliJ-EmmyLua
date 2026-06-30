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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import com.tang.intellij.lua.LuaBundle
import com.tang.intellij.lua.localization.LocalizationDictionaryLoader
import com.tang.intellij.lua.localization.LocalizationDictionaryLoadResult
import com.tang.intellij.lua.localization.LocalizationDictionaryService
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class LocalizationSettingsEditor private constructor(
    private val mode: Mode,
    private val project: Project?
) {
    enum class Mode { APPLICATION, PROJECT }

    private val rootPanel = JPanel(BorderLayout())
    private val overrideCheckBox = JBCheckBox(LuaBundle.message("ui.settings.localization.override"))
    private val enabledCheckBox = JBCheckBox(LuaBundle.message("ui.settings.localization.enable"))
    private val dictionaryPathField = TextFieldWithBrowseButton()
    private val reloadButton = JButton(LuaBundle.message("ui.settings.localization.reload"))
    private val statusLabel = JBLabel(LuaBundle.message("ui.settings.localization.status.not_loaded"))
    private val keyPrefixField = JBTextField()
    private val keySearchField = JBTextField()
    private val lookupValueLabel = JBLabel(" ")
    private val hintLabel = JBLabel(LuaBundle.message("ui.settings.localization.project_hint"))
    private var previewDictionary: Map<String, String> = emptyMap()
    private var initialSnapshot: Snapshot? = null

    data class Snapshot(
        val overrideApplicationSettings: Boolean = false,
        val enabled: Boolean = false,
        val dictionaryFilePath: String = "",
        val keyPrefix: String = ""
    )

    val panel: JComponent
        get() = rootPanel

    init {
        dictionaryPathField.addBrowseFolderListener(
            LuaBundle.message("ui.settings.localization.choose_dictionary"),
            null,
            project,
            FileChooserDescriptorFactory.createSingleFileDescriptor("bytes")
        )
        reloadButton.addActionListener { reloadDictionary() }
        keySearchField.columns = 24
        keySearchField.addActionListener { lookupKey() }
        keyPrefixField.columns = 16

        val content = JPanel()
        content.layout = BoxLayout(content, BoxLayout.Y_AXIS)
        content.border = JBUI.Borders.emptyTop(8)

        if (mode == Mode.PROJECT) {
            content.add(overrideCheckBox)
            content.add(Box.createVerticalStrut(4))
            content.add(hintLabel)
            content.add(Box.createVerticalStrut(8))
        }

        content.add(enabledCheckBox)
        content.add(Box.createVerticalStrut(8))

        val pathPanel = JPanel(BorderLayout(4, 0))
        pathPanel.add(JBLabel(LuaBundle.message("ui.settings.localization.dictionary_path")), BorderLayout.WEST)
        val pathInputPanel = JPanel(BorderLayout(4, 0))
        pathInputPanel.add(dictionaryPathField, BorderLayout.CENTER)
        pathInputPanel.add(reloadButton, BorderLayout.EAST)
        pathPanel.add(pathInputPanel, BorderLayout.CENTER)
        content.add(pathPanel)
        content.add(Box.createVerticalStrut(4))
        content.add(statusLabel)
        content.add(Box.createVerticalStrut(8))

        val prefixPanel = JPanel(FlowLayout(FlowLayout.LEFT, 4, 0))
        prefixPanel.add(JBLabel(LuaBundle.message("ui.settings.localization.key_prefix")))
        prefixPanel.add(keyPrefixField)
        prefixPanel.add(JBLabel(LuaBundle.message("ui.settings.localization.key_prefix_hint")))
        content.add(prefixPanel)
        content.add(Box.createVerticalStrut(8))

        val lookupPanel = JPanel(FlowLayout(FlowLayout.LEFT, 4, 0))
        lookupPanel.add(JBLabel(LuaBundle.message("ui.settings.localization.lookup_key")))
        lookupPanel.add(keySearchField)
        val lookupButton = JButton(LuaBundle.message("ui.settings.localization.lookup"))
        lookupButton.addActionListener { lookupKey() }
        lookupPanel.add(lookupButton)
        lookupPanel.add(JBLabel(LuaBundle.message("ui.settings.localization.lookup_value")))
        lookupPanel.add(lookupValueLabel)
        content.add(lookupPanel)

        rootPanel.add(content, BorderLayout.NORTH)

        if (mode == Mode.PROJECT) {
            overrideCheckBox.addActionListener { updateEditableState() }
        }
        updateEditableState()
        refreshStatusFromService()
    }

    private fun reloadDictionary() {
        val path = dictionaryPathField.text.trim()
        val loadProject = resolveLoadProject()
        val result = LocalizationDictionaryLoader.load(path, loadProject)
        previewDictionary = result.dictionary
        updateStatusLabel(result)
        lookupValueLabel.text = " "

        if (loadProject != null && !loadProject.isDefault) {
            LocalizationDictionaryService.getInstance(loadProject).reloadFromPath(path)
        }
    }

    private fun lookupKey() {
        val key = keySearchField.text.trim()
        if (key.isEmpty()) {
            lookupValueLabel.text = LuaBundle.message("ui.settings.localization.lookup_empty")
            return
        }
        val value = previewDictionary[key]
        lookupValueLabel.text = if (value != null) {
            value.replace('\n', ' ')
        } else {
            LuaBundle.message("ui.settings.localization.lookup_not_found", key)
        }
    }

    private fun resolveLoadProject(): Project? {
        if (project != null) return project
        return ProjectManager.getInstance().openProjects.firstOrNull { !it.isDefault }
    }

    private fun refreshStatusFromService() {
        val loadProject = resolveLoadProject() ?: return
        val status = LocalizationDictionaryService.getInstance(loadProject).getLoadStatus()
        previewDictionary = status.dictionary
        if (status.success || status.resolvedPath.isNotBlank()) {
            updateStatusLabel(status)
        }
    }

    private fun updateStatusLabel(result: LocalizationDictionaryLoadResult) {
        statusLabel.text = result.statusMessage
    }

    private fun updateEditableState() {
        val editable = mode == Mode.APPLICATION || overrideCheckBox.isSelected
        enabledCheckBox.isEnabled = editable
        dictionaryPathField.isEnabled = editable
        reloadButton.isEnabled = editable
        keyPrefixField.isEnabled = editable
        keySearchField.isEnabled = editable
        hintLabel.isEnabled = mode == Mode.PROJECT
    }

    fun loadApplicationDefaults(settings: LuaSettings) {
        initialSnapshot = Snapshot(
            enabled = settings.localizationHintsEnabled,
            dictionaryFilePath = settings.dictionaryFilePath,
            keyPrefix = settings.localizationKeyPrefix
        )
        enabledCheckBox.isSelected = settings.localizationHintsEnabled
        dictionaryPathField.text = settings.dictionaryFilePath
        keyPrefixField.text = settings.localizationKeyPrefix
        refreshStatusFromService()
    }

    fun loadProjectSettings(appSettings: LuaSettings, projectSettings: LuaProjectLocalizationSettings) {
        initialSnapshot = Snapshot(
            overrideApplicationSettings = projectSettings.overrideApplicationSettings,
            enabled = projectSettings.localizationHintsEnabled ?: appSettings.localizationHintsEnabled,
            dictionaryFilePath = projectSettings.dictionaryFilePath ?: appSettings.dictionaryFilePath,
            keyPrefix = projectSettings.localizationKeyPrefix ?: appSettings.localizationKeyPrefix
        )
        overrideCheckBox.isSelected = projectSettings.overrideApplicationSettings
        enabledCheckBox.isSelected = projectSettings.localizationHintsEnabled ?: appSettings.localizationHintsEnabled
        dictionaryPathField.text = projectSettings.dictionaryFilePath ?: appSettings.dictionaryFilePath
        keyPrefixField.text = projectSettings.localizationKeyPrefix ?: appSettings.localizationKeyPrefix
        updateEditableState()
        refreshStatusFromService()
    }

    fun isModified(): Boolean {
        val snapshot = initialSnapshot ?: return false
        return currentSnapshot() != snapshot
    }

    fun applyApplicationSettings(settings: LuaSettings) {
        val snapshot = currentSnapshot()
        settings.localizationHintsEnabled = snapshot.enabled
        settings.dictionaryFilePath = snapshot.dictionaryFilePath
        settings.localizationKeyPrefix = snapshot.keyPrefix
        initialSnapshot = snapshot
        ApplicationManager.getApplication().messageBus
            .syncPublisher(LuaSettings.LOCALIZATION_SETTINGS_TOPIC)
            .run()
    }

    fun applyProjectSettings(projectSettings: LuaProjectLocalizationSettings) {
        val snapshot = currentSnapshot()
        projectSettings.overrideApplicationSettings = snapshot.overrideApplicationSettings
        if (snapshot.overrideApplicationSettings) {
            projectSettings.localizationHintsEnabled = snapshot.enabled
            projectSettings.dictionaryFilePath = snapshot.dictionaryFilePath
            projectSettings.localizationKeyPrefix = snapshot.keyPrefix
        } else {
            projectSettings.localizationHintsEnabled = null
            projectSettings.dictionaryFilePath = null
            projectSettings.localizationKeyPrefix = null
        }
        initialSnapshot = snapshot
        project?.let { projectSettings.notifyChanged(it) }
    }

    fun resetApplicationSettings(settings: LuaSettings) {
        loadApplicationDefaults(settings)
    }

    fun resetProjectSettings(appSettings: LuaSettings, projectSettings: LuaProjectLocalizationSettings) {
        loadProjectSettings(appSettings, projectSettings)
    }

    private fun currentSnapshot(): Snapshot {
        return Snapshot(
            overrideApplicationSettings = overrideCheckBox.isSelected,
            enabled = enabledCheckBox.isSelected,
            dictionaryFilePath = dictionaryPathField.text.trim(),
            keyPrefix = keyPrefixField.text
        )
    }

    companion object {
        fun createApplicationEditor(): LocalizationSettingsEditor {
            val editor = LocalizationSettingsEditor(Mode.APPLICATION, null)
            editor.loadApplicationDefaults(LuaSettings.instance)
            return editor
        }

        fun createProjectEditor(project: Project): LocalizationSettingsEditor {
            val editor = LocalizationSettingsEditor(Mode.PROJECT, project)
            editor.loadProjectSettings(LuaSettings.instance, LuaProjectLocalizationSettings.getInstance(project))
            return editor
        }
    }
}
