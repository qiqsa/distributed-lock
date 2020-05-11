package com.stu.lock.hook;

public interface LockHook {

    /**
     * handle lock start
     */
    void start();

    /**
     * handle lock success
     */
    void finishSuccess();

    /**
     * handle lock failure
     *
     * @param cause
     */
    void finishFailure(Exception cause);

}
