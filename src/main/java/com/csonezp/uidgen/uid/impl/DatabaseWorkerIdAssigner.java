package com.csonezp.uidgen.uid.impl;

import com.csonezp.uidgen.entity.WorkerNode;
import com.csonezp.uidgen.enums.WorkerNodeType;
import com.csonezp.uidgen.repo.WorkerNodeRepo;
import com.csonezp.uidgen.uid.WorkerIdAssigner;
import com.csonezp.uidgen.utils.DockerUtils;
import com.csonezp.uidgen.utils.NetUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : zhangpeng
 * @date : 2019/12/21 16:55
 * 基于DB的workId分配器
 */
@Component("dbWorkerIdAssigner")
@Slf4j
public class DatabaseWorkerIdAssigner implements WorkerIdAssigner {

    @Autowired
    WorkerNodeRepo workerNodeRepo;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public long assignWorkerId() {
        WorkerNode workerNode = buildWorkerNode();
        workerNode = workerNodeRepo.save(workerNode);
        log.info("Add worker node:" + workerNode);
        return workerNode.getId();
    }

    private WorkerNode buildWorkerNode() {
        WorkerNode workerNodeEntity = new WorkerNode();
        if (DockerUtils.isDocker()) {
            workerNodeEntity.setType(WorkerNodeType.CONTAINER.value());
            workerNodeEntity.setHostName(DockerUtils.getDockerHost());
            workerNodeEntity.setPort(DockerUtils.getDockerPort());

        } else {
            workerNodeEntity.setType(WorkerNodeType.ACTUAL.value());
            workerNodeEntity.setHostName(NetUtils.getLocalAddress());
            workerNodeEntity.setPort(System.currentTimeMillis() + "-" + RandomUtils.nextInt(0, 100000));
        }

        return workerNodeEntity;
    }
}
