package com.platform.boot.commons;

import java.io.Serializable;

/**
 * Snowflake 算法生成分布式唯一 ID
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public class Snowflake implements Serializable {

    private static final long EPOCH = 1609459200000L; // 设置起始时间戳，例如：2021-01-01 00:00:00
    private final long dataCenterId;
    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    /**
     * 构造Snowflake对象。
     * 使用Java 17进行优化和重写的构造方法。
     *
     * @param dataCenterId 数据中心ID
     * @param workerId     工作节点ID
     */
    public Snowflake(long dataCenterId, long workerId) {
        this.dataCenterId = dataCenterId;
        this.workerId = workerId;
    }

    /**
     * 生成下一个唯一ID的字符串表示。
     * 使用Java 17进行优化和重写的方法。
     * 调用nextId()方法生成唯一ID，并将其转换为字符串。
     *
     * @return 生成的唯一ID的字符串表示
     */
    public String nextIdStr() {
        return String.valueOf(nextId());
    }

    /**
     * 生成下一个唯一ID。
     * 使用Java 17进行优化和重写的方法。
     * 通过获取当前时间戳，检查时钟的有效性，以及更新序列号和时间戳来生成ID。
     *
     * @return 生成的唯一ID
     * @throws RuntimeException 如果系统时钟无效，则抛出运行时异常
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        // 检查系统时钟的有效性
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Invalid system clock");
        }
        // 如果当前时间戳与上一个时间戳相同，则更新序列号
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & 4095;
            // 如果序列号达到上限，则等待下一个时间戳
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 如果当前时间戳与上一个时间戳不同，则重置序列号
            sequence = 0L;
        }
        // 更新上一个时间戳
        lastTimestamp = timestamp;
        // 生成唯一ID并返回
        return ((timestamp - EPOCH) << 22) | (dataCenterId << 17) | (workerId << 12) | sequence;
    }

    /**
     * 生成下一个时间戳，确保它大于上一个时间戳。
     * 通过循环调用System.currentTimeMillis()来获取当前时间戳，直到当前时间戳大于上一个时间戳为止。
     *
     * @param lastTimestamp 上一个时间戳
     * @return 生成的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}