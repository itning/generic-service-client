package top.itning.generic.service.zk.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.springframework.stereotype.Service;
import top.itning.generic.service.zk.dto.ZkInfo;
import top.itning.generic.service.zk.service.ZkInfoService;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author itning
 * @since 2020/12/24 14:32
 */
@Service
@Slf4j
public class ZkInfoServiceImpl implements ZkInfoService {

    private static final Map<String, ZkInfo> CACHE = new ConcurrentHashMap<>();

    private static final Map<String, Boolean> IS_CONNECTED = new ConcurrentHashMap<>();

    private static final Cache<String, ZkInfo> GUAVA_CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    private final Map<String, CuratorFramework> curatorFrameworkMap;


    public ZkInfoServiceImpl(Map<String, CuratorFramework> curatorFrameworkMap) {
        this.curatorFrameworkMap = curatorFrameworkMap;
    }

    @PostConstruct
    private void init() {

        Set<String> keys = this.curatorFrameworkMap.keySet();

        for (String env : keys) {
            Thread thread = new Thread(() -> {
                CuratorFramework curatorFramework = curatorFrameworkMap.get(env);
                curatorFramework.start();
                curatorFramework.getConnectionStateListenable().addListener(new ConnectionStateListenerImpl(env));
                //addWatch(curatorFramework, env);
            }, env);
            thread.start();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> curatorFrameworkMap.values().forEach(CuratorFramework::close)));
    }

    private void addWatch(CuratorFramework curatorFramework, String env) {
        try {
            Watcher w = new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    log.info("Node Change:{}", watchedEvent);
                    try {
                        List<String> list = curatorFramework.getChildren().usingWatcher(this).forPath("/clientTest");
                        CACHE.put(getCacheKey(env, "/dubbo"), ZkInfo.withSuccess(env, list));
                    } catch (Exception e) {
                        log.error("Watch {} Error", env, e);
                    }
                }
            };

            List<String> list = curatorFramework.getChildren().usingWatcher(w).forPath("/dubbo");
            CACHE.put(getCacheKey(env, "/dubbo"), ZkInfo.withSuccess(env, list));

        } catch (Exception e) {
            log.error("Watch {} Error", env, e);
        }
    }

    @Override
    public ZkInfo getChildNodeName(String env, String nodeName) {
        CuratorFramework curatorFramework = curatorFrameworkMap.get(env);
        if (null == curatorFramework) {
            return ZkInfo.withFailed(env);
        }
        if (curatorFramework.getState() == CuratorFrameworkState.LATENT) {
            return ZkInfo.withFailed(env);
        }
        if (!IS_CONNECTED.getOrDefault(env, false)) {
            return ZkInfo.withFailed(env, false);
        }
        try {
            ZkInfo zkInfo = new ZkInfo();
            zkInfo.setZkConnected(true);
            zkInfo.setEnv(env);
            if (null != curatorFramework.checkExists().forPath(nodeName)) {
                List<String> list = curatorFramework.getChildren().forPath(nodeName);
                zkInfo.setNodes(list);
                zkInfo.setSuccess(true);
            } else {
                zkInfo.setSuccess(false);
            }
            zkInfo.setUpdateTime(new Date());
            return zkInfo;
        } catch (Exception e) {
            log.error("Get Zk Child Node Error,Env:{},NodeName:{}", env, nodeName, e);
        }
        return ZkInfo.withFailed(env);
    }

    public ZkInfo getChildNodeNameWithCache(String env, String nodeName) {

        String cacheKey = getCacheKey(env, nodeName);
        ZkInfo zkInfo = GUAVA_CACHE.getIfPresent(cacheKey);
        if (null != zkInfo && zkInfo.isSuccess()) {
            return zkInfo;
        }
        ZkInfo newZkInfo = getChildNodeName(env, nodeName);
        if (newZkInfo.isSuccess()) {
            GUAVA_CACHE.put(cacheKey, newZkInfo);
        }
        return newZkInfo;
    }

    private String getCacheKey(String... params) {
        StringBuilder sb = new StringBuilder();
        for (String item : params) {
            sb.append(item).append("-");
        }
        return sb.toString();
    }

    @Override
    public Set<String> getEnv() {
        return curatorFrameworkMap.keySet();
    }

    static class ConnectionStateListenerImpl implements ConnectionStateListener {
        private final String env;

        ConnectionStateListenerImpl(String env) {
            this.env = env;
        }

        @Override
        public void stateChanged(CuratorFramework client, ConnectionState newState) {
            IS_CONNECTED.put(env, newState.isConnected());
        }
    }
}
