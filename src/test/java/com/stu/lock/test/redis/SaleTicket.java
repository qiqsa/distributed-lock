package com.stu.lock.test.redis;

import com.stu.lock.redis.lock.RedisLock;

import java.util.concurrent.TimeUnit;

/**
 * 线程不安全示例
 *
 * @author Qi.qingshan
 * @date 2020/5/1
 */
public class SaleTicket implements Runnable {

    private RedisLock lock = new RedisLock();

    private int tickets = 100;

    public void run() {
        for (; ; ) {
            lock.lock();
            try {
                sale();
                if (tickets < 0) break;
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
            Thread.sleep(100);
        } catch (InterruptedException e) {

        }
    }
}
