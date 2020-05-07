package com.stu.lock.test.redis;

import com.stu.lock.redis.lock.RedisLock;
import com.stu.lock.zk.ZkLock;

import java.util.concurrent.locks.Lock;

/**
 * 线程不安全示例
 *
 * @author Qi.qingshan
 * @date 2020/5/1
 */
public class SaleTicket implements Runnable {

    private Lock lock = new ZkLock();

    private int tickets = 100;

    public void run() {
        for (; ; ) {
            if (tickets <= 0) break;
            lock.lock();
            try {
                sale();
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 售票
     */
    private void sale() {
        if (tickets > 0) {
            tickets--;
            System.out.println(Thread.currentThread().getName() + " - 在售第" + (100 - tickets) + "票 :: 剩余" + (tickets));
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }
    }
}
