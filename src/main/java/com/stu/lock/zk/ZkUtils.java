package com.stu.lock.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Qi.qingshan
 * @date 2020/5/2
 */
public class ZkUtils {

    private static final Logger log = LoggerFactory.getLogger(ZkUtils.class);

    private static volatile CuratorFramework zkClient;

    static {
        zkClient = init();
    }

    private static CuratorFramework init() {
        CuratorFramework framework = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .sessionTimeoutMs(6000)
                .retryPolicy(new RetryUntilElapsed(5, 1000))
                .build();
        framework.start();
        return framework;
    }

    public static boolean createPersistNode(String path) {
        boolean ret;
        try {
            zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
            ret = true;
        } catch (Exception e) {
            log.error(e.getMessage());
            ret = false;
        }
        return ret;
    }

    public static boolean createTempNode(String path, String node, String value) {
        boolean success;
        try {
            zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(mkPath(path, node), value.getBytes());
            success = true;
        } catch (Exception e) {
            log.error(e.getMessage());
            success = false;
        }
        return success;
    }

    public static boolean createTempNode(String path, String node) {
        return createTempNode(path, node, "");
    }


    public static void removeNode(String path, String node) {
        try {
            zkClient.delete().forPath(mkPath(path, node));
        } catch (Exception e) {
        }
    }

    private static String mkPath(String path, String node) {
        return path + "/" + node;
    }
}
