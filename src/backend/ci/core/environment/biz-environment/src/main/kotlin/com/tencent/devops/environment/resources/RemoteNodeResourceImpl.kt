package com.tencent.devops.environment.resources

import com.tencent.devops.common.api.pojo.Page
import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.web.RestResource
import com.tencent.devops.environment.api.RemoteNodeResource
import com.tencent.devops.environment.pojo.NodeBaseInfo
import com.tencent.devops.environment.service.EnvService
import com.tencent.devops.environment.service.NodeService
import org.springframework.beans.factory.annotation.Autowired

@RestResource
class RemoteNodeResourceImpl @Autowired constructor(
    private val nodeService: NodeService,
    private val envService: EnvService
) : RemoteNodeResource {

    override fun listNodeForAuth(projectId: String, offset: Int?, limit: Int?): Result<Page<NodeBaseInfo>> {
        return Result(nodeService.listByPage(projectId, offset, limit))
    }

    override fun getNodeInfos(nodeHashIds: List<String>): Result<List<NodeBaseInfo>> {
        return Result(nodeService.listRawServerNodeByIds(nodeHashIds))
    }

    override fun searchByName(projectId: String, offset: Int?, limit: Int?, displayName: String): Result<Page<NodeBaseInfo>> {
        return Result(nodeService.searchByDisplayName(
                projectId = projectId,
                limit = limit,
                offset = offset,
                displayName = displayName
        ))
    }
}