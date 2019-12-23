package com.csonezp.uidgen.uid;

/**
 * @author : zhangpeng
 * @date : 2019/12/21 16:55
 */
public interface WorkerIdAssigner {
    /**
     *
     * 为雪花算法发号器分配workerId
     * {@link com.csonezp.uidgen.uid.impl.SnowFlakeGenerator}
     * @return assigned worker id
     */
    long assignWorkerId();
}
