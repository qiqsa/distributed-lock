package com.stu.lock.hook;

import com.stu.lock.loader.NewInstanceServiceLoader;

import java.util.Collection;

/**
 * @author Qi.qingshan
 * @date 2020/5/9
 */
public class SPILockHook implements LockHook {

    private final Collection<LockHook> lockHooks = NewInstanceServiceLoader.newServiceInstance(LockHook.class);

    static {
        NewInstanceServiceLoader.register(LockHook.class);
    }

    @Override
    public void start() {
        for (LockHook each : lockHooks) {
            each.start();
        }
    }

    @Override
    public void finishSuccess() {
        for (LockHook each : lockHooks) {
            each.finishSuccess();
        }
    }

    @Override
    public void finishFailure(Exception cause) {
        for (LockHook each : lockHooks) {
            each.finishFailure(cause);
        }
    }
}
