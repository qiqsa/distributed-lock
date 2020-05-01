package com.stu.lock.test.redis;

import com.stu.lock.redis.lock.RedisLock;
import org.junit.Test;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Qi.qingshan
 * @date 2020/5/1
 */
public class SaleTicketTest {

    BlockingDeque queue = new LinkedBlockingDeque(100);

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 50, 100, TimeUnit.SECONDS, queue);

    @Test
    public void testSaleTickets() throws IOException {
        SaleTicket saleTicket = new SaleTicket();
        executor.execute(new Thread(saleTicket, "售票员001"));
        executor.execute(new Thread(saleTicket, "售票员002"));
        executor.execute(new Thread(saleTicket, "售票员003"));
        executor.execute(new Thread(saleTicket, "售票员004"));
        System.in.read();
    }
}
