package top.itning.generic.service.zk.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author itning
 * @since 2020/12/24 14:33
 */
@Configuration
public class BeanConfig {

    private final ZkProperties zkProperties;

    @Autowired
    public BeanConfig(ZkProperties zkProperties) {
        this.zkProperties = zkProperties;
    }

    @Bean
    public Map<String, CuratorFramework> curatorFramework() {

        Map<String, CuratorFramework> map = new HashMap<>();
        if (null != zkProperties.getZkList()) {
            zkProperties.getZkList().forEach((key, value) -> {
                RetryPolicy retryPolicy = new RetryForever(2000);
                CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                        .connectString(value)
                        .sessionTimeoutMs(5000)
                        .connectionTimeoutMs(5000)
                        .retryPolicy(retryPolicy)
                        .build();
                map.put(key, curatorFramework);
            });
        }

        return map;
    }
}
