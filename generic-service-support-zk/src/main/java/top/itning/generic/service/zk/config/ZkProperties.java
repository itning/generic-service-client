package top.itning.generic.service.zk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author itning
 * @since 2020/12/24 14:37
 */
@ConfigurationProperties(prefix = "generic-service-support-zk")
@Component
@Data
public class ZkProperties {
    private Map<String, String> zkList;
}
