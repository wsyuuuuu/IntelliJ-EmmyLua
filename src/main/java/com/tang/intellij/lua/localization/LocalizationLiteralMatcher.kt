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

import com.tang.intellij.lua.psi.LuaLiteralExpr
import com.tang.intellij.lua.psi.LuaLiteralKind
import com.tang.intellij.lua.psi.kind
import com.tang.intellij.lua.psi.stringValue

object LocalizationLiteralMatcher {
    fun match(literal: LuaLiteralExpr, keyPrefix: String): String? {
        if (keyPrefix.isBlank()) return null
        if (literal.kind != LuaLiteralKind.String) return null
        val value = literal.stringValue
        if (value.isEmpty() || !value.startsWith(keyPrefix)) return null
        return value
    }
}
