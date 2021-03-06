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
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.devops.artifactory.pojo.bkrepo

import com.tencent.devops.artifactory.pojo.FileInfo
import com.tencent.devops.artifactory.pojo.Property
import com.tencent.devops.artifactory.util.BkRepoUtils
import com.tencent.devops.common.api.util.timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class QueryNodeInfo(
    var createdBy: String,
    var createdDate: String,
    var lastModifiedBy: String,
    var lastModifiedDate: String,
    var folder: Boolean,
    var path: String,
    var name: String,
    var fullPath: String,
    var size: Long,
    var sha256: String? = null,
    var md5: String? = null,
    var projectId: String,
    var repoName: String,
    var metadata: Map<String, String>? = mapOf()
) {
    fun toFileInfo(): FileInfo {
        return FileInfo(
            name = name,
            fullName = name,
            path = "$projectId/$repoName$fullPath",
            fullPath = fullPath,
            size = size,
            folder = folder,
            modifiedTime = LocalDateTime.parse(lastModifiedDate, DateTimeFormatter.ISO_DATE_TIME).timestamp(),
            artifactoryType = BkRepoUtils.parseArtifactoryType(repoName),
            properties = if (metadata == null) {
                listOf()
            } else {
                metadata!!.map { Property(it.key, it.value) }
            },
            appVersion = "",
            shortUrl = ""
        )
    }
}