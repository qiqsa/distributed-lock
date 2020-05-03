## 背景

今天是五一，决定不出去，在家里撸代码，今天学习redis，于是准备写个基于redis的分布式锁。由于本人属于菜鸟级别，在写的过程中遇到各种问题，功夫不负有心人，终于搞定，如果发现实现有问题，欢迎指导，感谢.  

在单机时代，虽然不需要分布式锁，但也面临过类似的问题，只不过在单机的情况下，如果有多个线程要同时访问某个共享资源的时候，我们可以采用线程间加锁的机制，即当某个线程获取到这个资源后，就立即对这个资源进行加锁，当使用完资源之后，再释放锁，其它线程就可以接着使用了。JAVA中已提供相关工具类。但是到了分布式系统的时代，这种线程或者进程之间的锁机制，就可能没作用了，系统可能会有多份并且部署在不同的机器上，这些资源已经不是在线程之间共享了，而是属于进程之间共享的资源。因此，为了解决这个问题，我们就必须引入「分布式锁」。

分布式锁，是指在分布式的部署环境下，通过锁机制来让多客户端互斥的对共享资源进行访问。

一般分布式锁要满足一下几点要求：

- 排他性：在同一时间只会有一个客户端能获取到锁，其它客户端无法同时获取
- 避免死锁：这把锁在一段有限的时间之后，一定会被释放（正常释放或异常释放）
- 高可用：获取或释放锁的机制必须高可用且性能佳

## 分布式锁实现方式

目前主流的分布式锁实现主要有以下几种

- 基于redis实现
- 基于数据库实现
- 基于zookeeper的实现

今天主要将基于redis实现分布式锁

## redis分布式锁实现

### redis分布式锁基础知识

- 缓存过期

  缓存可以设置过期时间，redis根据时间自动进行清理。

- setNx命令

```
将 key 的值设为 value ，当且仅当 key 不存在。
若给定的 key 已经存在，则 SETNX 不做任何动作。
SETNX 是『SET if Not eXists』(如果不存在，则 SET)的简写。
```

- lua脚本

  脚本语言，用于支持redis原子操作。      

熟悉以上redis知识，实现redis分布式锁比较容易了。  

### 关注点

- 缓存过期

  最好给加锁的key设置缓存过期时间，可以有效的防止死锁，比如某个进程加锁后没来得及释放锁，宕机，说来负责释放锁？

- set值

  加锁时，在redis中保存在各节点中唯一的值，防止不同进程误解锁

![](https://imgkr.cn-bj.ufileos.com/f036dd1d-d8ad-4289-af39-ca789641abb2.jpg)


比如serviceA已经在redis中加锁lock，一般serviceA执行时间为1秒，则设置缓存过期时间2秒，某天由于机器原因serviceA执行了3秒，那么对应的锁已经失效，此期间B去加锁，并加锁成功， serviceA执行完会释放锁，导致serviceA会将B加的锁释放，所以产生误删锁，采用唯一值，避免这种情况产生。删锁会检查值，如果加锁与解锁的值不相同则不允许解锁。 

- 加锁与失效时间必须要原子性

### 核心逻辑

1、利用SETNX命令加锁

```java
    public static String set(String key, String value, long timeout) {
        Jedis jedis = getJedis();
        try {
            String ret = jedis.set(key, value, "NX", "PX", timeout);
            return ret;
        } finally {
            close(jedis);
        }
    }
```

2、实现阻塞加锁和非阻塞加锁

```java
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
```

3、解锁

解锁的同时需要去检查值是否与加锁的值相同，不相同则不允许解锁，这里是通过ThreadLocal传加锁产生的uuid

```java
    /**
     * unlock
     * 执行lua脚本，保证原子性
     */
    public void unlock() {
        release();
    }

    private void release() {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        JedisUtis.remove(LOCK_KEY, script, LOCAL.get());
    }
```

### 场景验证

12306售票是并发学习中经典案例，还是拿这个举例，比如有100 tickets，有多个售票窗口同时售票，怎么保证不被重复卖

```java
/**
 * 线程不安全示例
 *
 * @author Qi.qingshan
 * @date 2020/5/1
 */
public class SaleTicket implements Runnable {

    private int tickets = 100;

    public void run() {
        for (; ; ) {
          sale();
          if (tickets < 0) break;
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
```

测试类

```java
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
```

![](https://imgkr.cn-bj.ufileos.com/81f4ecff-2478-4eb5-a096-fd2eb1b7439a.jpg)


存在重复售票情况，改用redisLock,调整核心代码

```java
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
```

执行结果如下
![](https://imgkr.cn-bj.ufileos.com/fa26d233-3b84-49da-98ba-2b9ac90e5b21.jpg)


完整代码已上传<https://github.com/qiqsa/distributed-lock>