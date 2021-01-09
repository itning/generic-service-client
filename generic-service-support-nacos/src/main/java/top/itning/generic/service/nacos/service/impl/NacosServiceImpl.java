package top.itning.generic.service.nacos.service.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.itning.generic.service.nacos.config.NacosProperties;
import top.itning.generic.service.nacos.service.NacosService;
import top.itning.generic.service.support.registry.pojo.ServiceInfo;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author itning
 * @since 2021/1/9 14:13
 */
@Slf4j
@Service("nacos")
public class NacosServiceImpl implements NacosService {

    private static final Cache<String, List<String>> GUAVA_CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    private static final Cache<String, ServiceInfo> SERVICE_INFO_GUAVA_CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    private final Map<String, NacosNamingService> NACOS_NAMING_SERVICE_MAP = new HashMap<>();
    private final NacosProperties nacosProperties;

    @Autowired
    public NacosServiceImpl(NacosProperties nacosProperties) {
        this.nacosProperties = nacosProperties;
    }

    @PostConstruct
    private void init() {
        Map<String, String> nacosList = nacosProperties.getNacosList();
        if (null == nacosList) {
            return;
        }
        nacosList.forEach((env, address) -> {
            Thread thread = new Thread(() -> {
                try {
                    NacosNamingService namingService = new NacosNamingService(address);
                    NACOS_NAMING_SERVICE_MAP.put(env, namingService);
                    log.info("Put Env {} Nacos Instance To Map", env);
                } catch (NacosException e) {
                    log.warn("Init Env {} Nacos Exception", env, e);
                }
            }, env);
            thread.start();
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> NACOS_NAMING_SERVICE_MAP.values().forEach(item -> {
            try {
                item.shutDown();
            } catch (NacosException e) {
                log.error("ShutDown Nacos Exception", e);
            }
        })));
    }

    @Override
    public Collection<String> getAllEnv() {
        return NACOS_NAMING_SERVICE_MAP.keySet();
    }

    @Override
    public ServiceInfo getProviders(String env) {
        ServiceInfo resultFromCache = SERVICE_INFO_GUAVA_CACHE.getIfPresent(env);
        if (null != resultFromCache) {
            return resultFromCache;
        }
        NacosNamingService nacosNamingService = NACOS_NAMING_SERVICE_MAP.get(env);
        if (null == nacosNamingService) {
            return ServiceInfo.failed(env, false);
        }
        try {
            List<String> data = getServicesOfServerFromCache(env, nacosNamingService)
                    .parallelStream()
                    .filter(item -> item.startsWith("providers:"))
                    .map(item -> item.split(":")[1])
                    // 去重，选择接口的时候会让用户选择具体的接口
                    .distinct()
                    .collect(Collectors.toList());
            ServiceInfo serviceInfo = ServiceInfo.success(env, data);
            SERVICE_INFO_GUAVA_CACHE.put(env, serviceInfo);
            return serviceInfo;
        } catch (NacosException e) {
            log.warn("Nacos Exception：{}", e.getMessage());
            return ServiceInfo.failed(env, true);
        }
    }

    @Override
    public ServiceInfo getProvideDetail(String env, String provider) {
        NacosNamingService nacosNamingService = NACOS_NAMING_SERVICE_MAP.get(env);
        if (null == nacosNamingService) {
            return ServiceInfo.failed(env, false);
        }
        try {
            List<String> data = getServicesOfServerFromCache(env, nacosNamingService)
                    .stream()
                    .filter(item -> item.startsWith("providers:"))
                    .filter(item -> item.contains(provider))
                    .flatMap(item -> {
                        try {
                            return nacosNamingService.getAllInstances(item).stream();
                        } catch (NacosException e) {
                            log.warn("Nacos Exception:{}", e.getMessage());
                            return Stream.empty();
                        }
                    })
                    .map(this::instance2String)
                    .map(item -> {
                        try {
                            return URLEncoder.encode(item, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            log.warn("URL Encoder Exception:{}", e.getMessage());
                            return item;
                        }
                    })
                    .collect(Collectors.toList());
            return ServiceInfo.success(env, data);
        } catch (NacosException e) {
            log.warn("Nacos Exception：{}", e.getMessage());
            return ServiceInfo.failed(env, true);
        }
    }

    private List<String> getServicesOfServerFromCache(String env, NacosNamingService nacosNamingService) throws NacosException {
        List<String> list = GUAVA_CACHE.getIfPresent(env);
        if (!CollectionUtils.isEmpty(list)) {
            return list;
        }
        ListView<String> servicesOfServer = nacosNamingService.getServicesOfServer(1, Integer.MAX_VALUE);
        if (CollectionUtils.isEmpty(servicesOfServer.getData())) {
            return Collections.emptyList();
        } else {
            GUAVA_CACHE.put(env, servicesOfServer.getData());
            return servicesOfServer.getData();
        }
    }

    private String instance2String(Instance instance) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> metadata = instance.getMetadata();
        String interfaceName = metadata.getOrDefault("interface", "");
        return sb
                .append(metadata.getOrDefault("protocol", "dubbo"))
                .append("://")
                .append(instance.getIp())
                .append(":")
                .append(instance.getPort())
                .append("/")
                .append(interfaceName)
                .append("?methods=")
                .append(metadata.getOrDefault("methods", ""))
                .append("&group=")
                .append(metadata.getOrDefault("group", ""))
                .append("&version=")
                .append(metadata.getOrDefault("version", ""))
                .append("&interface=")
                .append(interfaceName)
                .append("&path=")
                .append(metadata.getOrDefault("path", ""))
                .toString();
    }
}
