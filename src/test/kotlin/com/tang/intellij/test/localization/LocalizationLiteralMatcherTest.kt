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

import com.tang.intellij.lua.localization.LocalizationLiteralMatcher
import com.tang.intellij.lua.psi.LuaLiteralExpr
import com.tang.intellij.test.LuaTestBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocalizationLiteralMatcherTest : LuaTestBase() {
    @Test
    fun testMatchingPrefix() {
        myFixture.configureByText("test.lua", """local x = "S_Arena_029"""")
        val literal = findFirstStringLiteral()
        assertEquals("S_Arena_029", LocalizationLiteralMatcher.match(literal, "S_"))
    }

    @Test
    fun testNonMatchingPrefix() {
        myFixture.configureByText("test.lua", """local x = "hello"""")
        val literal = findFirstStringLiteral()
        assertNull(LocalizationLiteralMatcher.match(literal, "S_"))
    }

    @Test
    fun testEmptyPrefix() {
        myFixture.configureByText("test.lua", """local x = "S_test"""")
        val literal = findFirstStringLiteral()
        assertNull(LocalizationLiteralMatcher.match(literal, ""))
    }

    private fun findFirstStringLiteral(): LuaLiteralExpr {
        val file = myFixture.file
        var result: LuaLiteralExpr? = null
        file.accept(object : com.intellij.psi.PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: com.intellij.psi.PsiElement) {
                if (result == null && element is LuaLiteralExpr) {
                    result = element
                }
                super.visitElement(element)
            }
        })
        return result!!
    }
}
