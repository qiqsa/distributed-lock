package com.stu.lock.zk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author Qi.qingshan
 * @date 2020/5/2
 */
public class ZkLock implements Lock {

    private static final Logger log = LoggerFactory.getLogger(ZkLock.class);

    private static final String ZK_LOCK_PATH = "/lock/zkLock";

    private static final String LOCK = ".lock";

    public ZkLock() {
        ZkUtils.createPersistNode(ZK_LOCK_PATH);
    }

    @Override
    public void lock() {
        if (tryLock()) {
            return;
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        lock();
    }

    @Override
    public boolean tryLock() {
        String salerName = Thread.currentThread().getName();
        boolean success = ZkUtils.createTempNode(ZK_LOCK_PATH, LOCK,salerName);
        if (success) {
            return true;
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        ZkUtils.removeNode(ZK_LOCK_PATH, LOCK);
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

}
