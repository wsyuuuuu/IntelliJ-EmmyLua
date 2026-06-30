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

object LocalizationHintUtil {
    const val INLAY_PREFIX = "@LOC@"
    private const val MAX_HINT_LENGTH = 50

    fun formatHintText(text: String): String {
        val singleLine = text.replace('\n', ' ').replace('\r', ' ')
        return if (singleLine.length <= MAX_HINT_LENGTH) {
            singleLine
        } else {
            singleLine.substring(0, MAX_HINT_LENGTH - 1) + "…"
        }
    }

    fun wrapInlayPayload(text: String): String = INLAY_PREFIX + text
}
