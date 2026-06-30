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

import java.nio.charset.StandardCharsets

class LubanByteBufReader(private val bytes: ByteArray) {
    private var readerIndex = 0

    fun readSize(): Int = readUint().toInt()

    fun readUint(): UInt {
        ensureRead(1)
        val h = bytes[readerIndex].toInt() and 0xff
        return when {
            h < 0x80 -> {
                readerIndex++
                h.toUInt()
            }
            h < 0xc0 -> {
                ensureRead(2)
                val x = ((h and 0x3f) shl 8) or (bytes[readerIndex + 1].toInt() and 0xff)
                readerIndex += 2
                x.toUInt()
            }
            h < 0xe0 -> {
                ensureRead(3)
                val x = ((h and 0x1f) shl 16) or
                        ((bytes[readerIndex + 1].toInt() and 0xff) shl 8) or
                        (bytes[readerIndex + 2].toInt() and 0xff)
                readerIndex += 3
                x.toUInt()
            }
            h < 0xf0 -> {
                ensureRead(4)
                val x = ((h and 0x0f) shl 24) or
                        ((bytes[readerIndex + 1].toInt() and 0xff) shl 16) or
                        ((bytes[readerIndex + 2].toInt() and 0xff) shl 8) or
                        (bytes[readerIndex + 3].toInt() and 0xff)
                readerIndex += 4
                x.toUInt()
            }
            else -> {
                ensureRead(5)
                val x = ((bytes[readerIndex + 1].toInt() and 0xff) shl 24) or
                        ((bytes[readerIndex + 2].toInt() and 0xff) shl 16) or
                        ((bytes[readerIndex + 3].toInt() and 0xff) shl 8) or
                        (bytes[readerIndex + 4].toInt() and 0xff)
                readerIndex += 5
                x.toUInt()
            }
        }
    }

    fun readString(): String {
        val n = readSize()
        if (n <= 0) return ""
        ensureRead(n)
        val value = String(bytes, readerIndex, n, StandardCharsets.UTF_8)
        readerIndex += n
        return value
    }

    fun readDictionary(): Map<String, String> {
        val count = readSize()
        val dict = HashMap<String, String>(count)
        repeat(count) {
            val key = readString()
            val value = readString().replace("\\n", "\n")
            dict[key] = value
        }
        return dict
    }

    private fun ensureRead(size: Int) {
        if (readerIndex + size > bytes.size) {
            throw IllegalStateException("Unexpected end of byte buffer at index $readerIndex, need $size bytes")
        }
    }

    companion object {
        fun readDictionary(bytes: ByteArray): Map<String, String> {
            return LubanByteBufReader(bytes).readDictionary()
        }
    }
}
