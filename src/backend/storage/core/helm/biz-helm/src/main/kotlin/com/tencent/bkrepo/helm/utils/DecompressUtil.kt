/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.  
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package com.tencent.bkrepo.helm.utils

import com.tencent.bkrepo.helm.constants.CHART_YAML
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.util.zip.GZIPInputStream

object DecompressUtil {
    private val logger = LoggerFactory.getLogger(DecompressUtil::class.java)
    private const val BUFFER_SIZE = 2048
    private const val FILE_NAME = CHART_YAML

    @Throws(Exception::class)
    fun InputStream.getArchivesContent(format: String): String {
        return when (format) {
            "tar" -> {
                getTarArchiversContent(this)
            }
            "zip" -> {
                getZipArchiversContent(this)
            }
            "tar.gz" -> {
                getTgzArchiversContent(this)
            }
            "tgz" -> {
                getTgzArchiversContent(this)
            }
            else -> {
                "can not support compress format!"
            }
        }
    }

    @Throws(Exception::class)
    fun getZipArchiversContent(inputStream: InputStream): String {
        return getArchiversContent(ZipArchiveInputStream(inputStream))
    }

    @Throws(Exception::class)
    fun getTgzArchiversContent(inputStream: InputStream): String {
        return getArchiversContent(TarArchiveInputStream(GZIPInputStream(inputStream)))
    }

    @Throws(Exception::class)
    fun getTarArchiversContent(inputStream: InputStream): String {
        return getArchiversContent(TarArchiveInputStream(inputStream))
    }

    private fun getArchiversContent(archiveInputStream: ArchiveInputStream): String {
        val stringBuilder = StringBuffer()
        archiveInputStream.use { it ->
            try {
                val nextEntry = it.nextEntry
                while (nextEntry != null) {
                    nextEntry.let {
                        if ((!it.isDirectory) && it.name.split("/").last() == FILE_NAME) {
                            var length: Int
                            val bytes = ByteArray(BUFFER_SIZE)
                            while ((archiveInputStream.read(bytes).also { length = it }) != -1) {
                                stringBuilder.append(String(bytes, 0, length))
                            }
                            return stringBuilder.toString()
                        }
                    }
                }
            } catch (ise: IllegalStateException) {
                logger.error("get archivers content error : ${ise.message}")
            }
        }
        return stringBuilder.toString()
    }
}
