package com.csonezp.uidgen.entry;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * @author : zhangpeng
 * @date : 2019/12/21 16:15
 */
@Data
@Entity
@Table(name = "WORKER_NODE")
public class WorkerNode {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "HOST_NAME")
    String hostName;

    @Column(name = "PORT")
    String port;

    Integer type;

    Date launchDate = new Date();

    Date modified;

    Date created;

}
