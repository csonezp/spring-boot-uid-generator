package com.csonezp.uidgen.repo;

import com.csonezp.uidgen.entity.WorkerNode;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author : zhangpeng
 * @date : 2019/12/21 16:31
 */
public interface WorkerNodeRepo extends JpaRepository<WorkerNode,Long> {
}
