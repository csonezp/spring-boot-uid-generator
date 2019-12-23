package com.csonezp.uidgen.uid;

import lombok.Getter;
import org.springframework.util.Assert;

/**
 * @author : zhangpeng
 * @date : 2019/12/21 16:45
 * SnowFlake的字节操作类
 * 对uid的64bit进行操作
 */

@Getter
public class BitsAllocator {
    /**
     * Total 64 bits
     */
    public static final int TOTAL_BITS = 1 << 6;

    /**
     * Bits for [sign-> second-> workId-> sequence]
     */
    private int signBits = 1;
    private final int timestampBits;
    private final int workerIdBits;
    private final int sequenceBits;

    /**
     * Max value for workId & sequence
     */
    private final long maxDeltaSeconds;
    private final long maxWorkerId;
    private final long maxSequence;

    /**
     * Shift for timestamp & workerId
     */
    /**
     * timestamp需要位移多少位
     */
    private final int timestampShift;
    /**
     * workerId需要位移多少位
     */
    private final int workerIdShift;

    /**
     * Constructor with timestampBits, workerIdBits, sequenceBits<br>
     * The highest bit used for sign, so <code>63</code> bits for timestampBits, workerIdBits, sequenceBits
     */
    public BitsAllocator(int timestampBits, int workerIdBits, int sequenceBits) {
        // make sure allocated 64 bits
        int allocateTotalBits = signBits + timestampBits + workerIdBits + sequenceBits;
        Assert.isTrue(allocateTotalBits == TOTAL_BITS, "allocate not enough 64 bits");

        // initialize bits
        this.timestampBits = timestampBits;
        this.workerIdBits = workerIdBits;
        this.sequenceBits = sequenceBits;

        // initialize max value
        //先将-1左移timestampBits位，得到-10000...（timestampBits-1个零)
        //然后取反，得到1111（timestampBits-1）个1
        //等价于2的timestampBits次方-1
        this.maxDeltaSeconds = ~(-1L << timestampBits);
        this.maxWorkerId = ~(-1L << workerIdBits);
        this.maxSequence = ~(-1L << sequenceBits);

        // initialize shift
        this.timestampShift = workerIdBits + sequenceBits;
        this.workerIdShift = sequenceBits;
    }

    /**
     * Allocate bits for UID according to delta seconds & workerId & sequence<br>
     * <b>Note that: </b>The highest bit will always be 0 for sign
     *
     * 这里就是把不同的字段放到相应的位上
     * id的总体结构是：
     * sign (fixed 1bit) -> deltaSecond -> workerId -> sequence(within the same second)
     * deltaSecond 左移（workerIdBits + sequenceBits）位，workerId左移sequenceBits位，此时就完成了字节的分配
     * @param deltaSeconds
     * @param workerId
     * @param sequence
     * @return
     */
    public long allocate(long deltaSeconds, long workerId, long sequence) {
        return (deltaSeconds << timestampShift) | (workerId << workerIdShift) | sequence;
    }
}
