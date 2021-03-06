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

package com.tencent.bkrepo.repository.service.impl

import com.tencent.bkrepo.repository.dao.FileReferenceDao
import com.tencent.bkrepo.repository.model.TFileReference
import com.tencent.bkrepo.repository.model.TNode
import com.tencent.bkrepo.repository.model.TRepository
import com.tencent.bkrepo.repository.service.FileReferenceService
import com.tencent.bkrepo.repository.service.RepositoryService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service

/**
 * 文件引用服务实现类
 */
@Service
class FileReferenceServiceImpl : FileReferenceService {

    @Autowired
    private lateinit var fileReferenceDao: FileReferenceDao

    @Autowired
    private lateinit var repositoryService: RepositoryService

    override fun increment(node: TNode, repository: TRepository?): Boolean {
        return if (validateParameter(node)) {
            val repo = repository ?: repositoryService.queryRepository(node.projectId, node.repoName)
            if (repo == null) {
                logger.error("Failed to decrement reference of node [$node], repository not found.")
                return false
            }
            return increment(node.sha256!!, repo.credentialsKey)
        } else false
    }

    override fun decrement(node: TNode, repository: TRepository?): Boolean {
        return if (validateParameter(node)) {
            val repo = repository ?: repositoryService.queryRepository(node.projectId, node.repoName)
            if (repo == null) {
                logger.error("Failed to decrement reference of node [$node], repository not found.")
                return false
            }
            return decrement(node.sha256!!, repo.credentialsKey)
        } else false
    }

    override fun increment(sha256: String, credentialsKey: String?): Boolean {
        val query = Query.query(
            Criteria.where(TFileReference::sha256.name).`is`(sha256)
                .and(TFileReference::credentialsKey.name).`is`(credentialsKey)
        )
        val update = Update().inc(TFileReference::count.name, 1)
        try {
            fileReferenceDao.upsert(query, update)
        } catch (exception: DuplicateKeyException) {
            // retry because upsert operation is not atomic
            fileReferenceDao.upsert(query, update)
        }
        logger.info("Increment reference of file [$sha256] on credentialsKey [$credentialsKey].")
        return true
    }

    override fun decrement(sha256: String, credentialsKey: String?): Boolean {
        val query = Query.query(
            Criteria.where(TFileReference::sha256.name).`is`(sha256)
                .and(TFileReference::credentialsKey.name).`is`(credentialsKey)
        )

        val fileReference = fileReferenceDao.findOne(query) ?: run {
            logger.error("Failed to decrement reference of file [$sha256] on credentialsKey [$credentialsKey]: $sha256 reference not found, create new one.")
            return false
        }

        return if (fileReference.count >= 1) {
            val update = Update().apply { inc(TFileReference::count.name, -1) }
            fileReferenceDao.upsert(query, update)
            logger.info("Decrement references of file [$sha256] on credentialsKey [$credentialsKey].")
            true
        } else {
            logger.error("Failed to decrement reference of file [$sha256] on credentialsKey [$credentialsKey]: reference count is 0.")
            false
        }
    }

    override fun count(sha256: String, credentialsKey: String?): Long {
        val query = Query.query(
            Criteria.where(TFileReference::sha256.name).`is`(sha256)
                .and(TFileReference::credentialsKey.name).`is`(credentialsKey)
        )
        return fileReferenceDao.findOne(query)?.count ?: 0
    }

    private fun validateParameter(node: TNode): Boolean {
        if (node.folder) return false
        if (node.sha256.isNullOrBlank()) {
            logger.warn("Failed to change file reference, node[$node] sha256 is null or blank.")
            return false
        }
        return true
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FileReferenceServiceImpl::class.java)
    }
}
