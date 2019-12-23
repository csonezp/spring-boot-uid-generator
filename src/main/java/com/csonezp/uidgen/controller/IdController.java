package com.csonezp.uidgen.controller;

import com.csonezp.uidgen.uid.UidGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : zhangpeng
 * @date : 2019/12/23 15:35
 */
@RestController
public class IdController {
    @Autowired
    @Qualifier("snowFlakeGenerator")
    UidGenerator uidGenerator;

    @GetMapping("/id")
    public Object getId(){
        return uidGenerator.gen();
    }
}
