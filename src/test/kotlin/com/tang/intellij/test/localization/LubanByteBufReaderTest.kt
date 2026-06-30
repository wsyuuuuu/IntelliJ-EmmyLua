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

import com.tang.intellij.lua.localization.LubanByteBufReader
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

class LubanByteBufReaderTest {
    @Test
    fun testReadDictionary() {
        val bytes = buildDictionaryBytes(
            "S_Arena_029" to "竞技场剩余{0}小时",
            "S_Bless_003" to "祝福等级{0}"
        )
        val dict = LubanByteBufReader.readDictionary(bytes)
        assertEquals("竞技场剩余{0}小时", dict["S_Arena_029"])
        assertEquals("祝福等级{0}", dict["S_Bless_003"])
    }

    @Test
    fun testReplaceEscapedNewLine() {
        val bytes = buildDictionaryBytes("K1" to "line1\\nline2")
        val dict = LubanByteBufReader.readDictionary(bytes)
        assertEquals("line1\nline2", dict["K1"])
    }

    private fun buildDictionaryBytes(vararg entries: Pair<String, String>): ByteArray {
        val out = ByteArrayOutputStream()
        writeSize(out, entries.size)
        for ((key, value) in entries) {
            writeString(out, key)
            writeString(out, value)
        }
        return out.toByteArray()
    }

    private fun writeString(out: ByteArrayOutputStream, value: String) {
        val data = value.toByteArray(StandardCharsets.UTF_8)
        writeSize(out, data.size)
        out.write(data)
    }

    private fun writeSize(out: ByteArrayOutputStream, value: Int) {
        writeUint(out, value.toUInt())
    }

    private fun writeUint(out: ByteArrayOutputStream, x: UInt) {
        when {
            x < 0x80u -> out.write(x.toInt())
            x < 0x4000u -> {
                out.write(((x shr 8) or 0x80u).toInt())
                out.write(x.toInt() and 0xff)
            }
            x < 0x200000u -> {
                out.write(((x shr 16) or 0xc0u).toInt())
                out.write(((x shr 8) and 0xffu).toInt())
                out.write(x.toInt() and 0xff)
            }
            x < 0x10000000u -> {
                out.write(((x shr 24) or 0xe0u).toInt())
                out.write(((x shr 16) and 0xffu).toInt())
                out.write(((x shr 8) and 0xffu).toInt())
                out.write(x.toInt() and 0xff)
            }
            else -> {
                out.write(0xf0)
                out.write(((x shr 24) and 0xffu).toInt())
                out.write(((x shr 16) and 0xffu).toInt())
                out.write(((x shr 8) and 0xffu).toInt())
                out.write(x.toInt() and 0xff)
            }
        }
    }
}
