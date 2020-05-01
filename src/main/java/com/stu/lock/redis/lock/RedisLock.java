package com.stu.lock.redis.lock;

import com.stu.lock.redis.common.JedisUtis;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author Qi.qingshan
 * @date 2020/5/1
 */
public class RedisLock implements Lock {

    private static final String LOCK_KEY = "redisKey";

    /**
     * 建议时间 = 业务消耗时间 * 2
     */
    private static final int DEFAULT_TIME_OUT = 1000;

    private final ThreadLocal<String> LOCAL = new ThreadLocal();

    /**
     * 阻塞加锁
     */
    public void lock() {
        if (tryLock()) {
            return;
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //lock
        lock();

    }


    /**
     * 基于setNx实现非阻塞锁
     *
     * @return
     */
    public boolean tryLock() {
        String uuid = UUID.randomUUID().toString();
        String ret = JedisUtis.set(LOCK_KEY, uuid, DEFAULT_TIME_OUT);
        if ("OK".equals(ret)) {
            //lock success
            LOCAL.set(uuid);
            return true;
        }
        return false;
    }

    public boolean tryLock(long time, TimeUnit unit){
        String uuid = UUID.randomUUID().toString();
        String ret = JedisUtis.set(LOCK_KEY, uuid, unit.toMillis(time));
        if ("ok".equals(ret)) {
            LOCAL.set(uuid);
            //lock success
            return true;
        }
        return false;
    }

    /**
     * unlock
     * lua脚本，保证原子性
     */
    public void unlock() {
        release();
    }

    private void release() {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        JedisUtis.remove(LOCK_KEY, script, LOCAL.get());
    }

    public Condition newCondition() {
        return null;
    }

    public void lockInterruptibly() throws InterruptedException {

    }

}
