package com.csonezp.uidgen.uid.impl;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.csonezp.uidgen.exception.UidGenerateException;
import com.csonezp.uidgen.uid.BitsAllocator;
import com.csonezp.uidgen.uid.UidGenerator;
import com.csonezp.uidgen.uid.WorkerIdAssigner;
import com.csonezp.uidgen.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author : zhangpeng
 * @date : 2019/12/21 16:46
 */
@Component(value = "snowFlakeGenerator")
@Slf4j
public class SnowFlakeGenerator implements UidGenerator {

    @Value("${snowflake.timeBits}")
    protected int timeBits = 28;

    @Value("${snowflake.workerBits}")
    protected int workerBits = 22;

    @Value("${snowflake.seqBits}")
    protected int seqBits = 13;

    @Value("${snowflake.epochStr}")
    /** Customer epoch, unit as second. For example 2016-05-20 (ms: 1463673600000)*/
    protected String epochStr = "2016-05-20";
    protected long epochSeconds = TimeUnit.MILLISECONDS.toSeconds(1463673600000L);

    @Autowired
    @Qualifier(value = "dbWorkerIdAssigner")
    protected WorkerIdAssigner workerIdAssigner;

    /** Stable fields after spring bean initializing */
    protected BitsAllocator bitsAllocator;


    protected long workerId;


    /** Volatile fields caused by nextId() */
    protected long sequence = 0L;
    protected long lastSecond = -1L;


    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        bitsAllocator = new BitsAllocator(timeBits,workerBits,seqBits);
        // initialize worker id
        workerId = workerIdAssigner.assignWorkerId();

        if(workerId > bitsAllocator.getMaxWorkerId()){
            throw new RuntimeException("Worker id " + workerId + " exceeds the max " + bitsAllocator.getMaxWorkerId());
        }

        if (StringUtils.isNotBlank(epochStr)) {
            this.epochSeconds = TimeUnit.MILLISECONDS.toSeconds(DateUtils.parseByDayPattern(epochStr).getTime());
        }

        log.info("Initialized bits(1, {}, {}, {}) for workerID:{}", timeBits, workerBits, seqBits, workerId);
    }
    @Override
    public Long gen() {
        try {
            return nextId();
        } catch (Exception e) {
            log.error("Generate unique id exception. ", e);
            throw new UidGenerateException(e);
        }
    }

    /**
     * Get UID
     *
     * @return UID
     * @throws UidGenerateException in the case: Clock moved backwards; Exceeds the max timestamp
     */
    protected synchronized long nextId() {
        long currentSecond = getCurrentSecond();

        // Clock moved backwards, refuse to generate uid
        //todo 时钟回拨问题待解决
        if (currentSecond < lastSecond) {
            long refusedSeconds = lastSecond - currentSecond;
            throw new UidGenerateException("Clock moved backwards. Refusing for %d seconds", refusedSeconds);
        }

        // At the same second, increase sequence
        //同一秒内的，seq加一
        if (currentSecond == lastSecond) {
            //seq 加一，如果大于MaxSequence，就变成0
            //如果大于MaxSequence 就是seq能取到的最大值，二进制（seqBits -1）位全是1
            sequence = (sequence + 1) & bitsAllocator.getMaxSequence();
            // Exceed the max sequence, we wait the next second to generate uid
            //号发完了，等到下一秒
            if (sequence == 0) {
                currentSecond = getNextSecond(lastSecond);
            }

            // At the different second, sequence restart from zero
        } else {
            //新的一秒，重新开始发号
            sequence = 0L;
        }
        lastSecond = currentSecond;
        // Allocate bits for UID
        return bitsAllocator.allocate(currentSecond - epochSeconds, workerId, sequence);

    }

    private long getCurrentSecond() {
        long currentSecond = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        if (currentSecond - epochSeconds > bitsAllocator.getMaxDeltaSeconds()) {
            throw new UidGenerateException("Timestamp bits is exhausted. Refusing UID generate. Now: " + currentSecond);
        }
        return currentSecond;
    }

    /**
     * Get next millisecond
     * 因为调用这里时seq已经到达上限，lastTimestamp这一秒内号已经发完了，所以要循环等待到下一秒
     */
    private long getNextSecond(long lastTimestamp) {
        long timestamp = getCurrentSecond();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentSecond();
        }

        return timestamp;
    }

}
