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

package com.tang.intellij.test.inspections

import com.tang.intellij.lua.codeInsight.inspection.LuaDeprecationInspection

class LuaDeprecationInspectionTest : LuaInspectionsTestBase(LuaDeprecationInspection()) {

    fun `test deprecated function call`() = checkByText("""
        ---@deprecated
        local function oldFunc()
        end

        <warning>oldFunc</warning>()
    """)

    fun `test deprecated method call`() = checkByText("""
        ---@class Foo
        local Foo = {}

        ---@deprecated
        function Foo:oldMethod()
        end

        ---@type Foo
        local foo = {}
        foo:<warning>oldMethod</warning>()
    """)

    fun `test deprecated doc field access`() = checkByText("""
        ---@class Foo
        ---@deprecated
        ---@field bar number

        ---@type Foo
        local foo = {}
        local v = foo.<warning>bar</warning>
    """)

    fun `test non deprecated member`() = checkByText("""
        ---@class Foo
        ---@field bar number

        ---@type Foo
        local foo = {}
        local v = foo.bar
    """)
}
