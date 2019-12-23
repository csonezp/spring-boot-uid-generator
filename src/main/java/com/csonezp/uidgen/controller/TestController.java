package com.csonezp.uidgen.controller;

import java.util.List;
import java.util.Optional;

import com.csonezp.uidgen.entry.WorkerNode;
import com.csonezp.uidgen.repo.WorkerNodeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : zhangpeng
 * @date : 2019/12/21 16:30
 */
@RestController
public class TestController {

    @Autowired
    WorkerNodeRepo repo;

    @GetMapping("/test")
    public Object test(){
        List<WorkerNode> nodes = repo.findAll();
        return nodes;
    }
}
