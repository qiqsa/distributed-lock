package com.stu.lock.hook;

import com.stu.lock.redis.lock.RedisLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Qi.qingshan
 * @date 2020/5/9
 */
public class RedisLockHook implements LockHook {

    private static final Logger log = LoggerFactory.getLogger(RedisLock.class);

    @Override
    public void start() {
        log("start lock ");
    }

    @Override
    public void finishSuccess() {
        log("finished lock");
    }

    @Override
    public void finishFailure(Exception cause) {
        log(cause.getMessage(),cause);
    }

    private void log(final Object... args) {
        log.info(String.format("%s", args));
    }
}
