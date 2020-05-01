package com.stu.lock.redis.common;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;

/**
 * @author Qi.qingshan
 * @date 2020/5/1
 */
public class JedisUtis {

    private static JedisPool pool;

    static {
        pool = newInstance();
    }

    public static JedisPool newInstance() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMinIdle(100);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setMaxWaitMillis(1000);
        JedisPool pool = new JedisPool(poolConfig, "127.0.0.1", 6379, 1000);
        return pool;
    }

    public static String set(String key, String value, long timeout) {
        Jedis jedis = getJedis();
        try {
            String ret = jedis.set(key, value, "NX", "PX", timeout);
            return ret;
        } finally {
            close(jedis);
        }
    }

    public static void remove(String key, String script, String value) {
        Jedis jedis = getJedis();
        try {
            jedis.eval(script, Arrays.asList(key), Arrays.asList(value));
        } finally {
            close(jedis);
        }
    }

    private static Jedis getJedis() {
        return pool.getResource();
    }

    private static void close(Jedis jedis){
        jedis.close();
    }
}
