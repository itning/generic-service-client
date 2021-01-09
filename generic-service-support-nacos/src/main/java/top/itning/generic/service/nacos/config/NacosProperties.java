package top.itning.generic.service.nacos.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author itning
 * @since 2020/12/24 14:37
 */
@ConfigurationProperties(prefix = "generic-service-support-nacos")
@Component
@Data
public class NacosProperties {
    private Map<String, String> nacosList;
}
