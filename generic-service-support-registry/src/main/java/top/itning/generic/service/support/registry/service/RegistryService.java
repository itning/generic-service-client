package top.itning.generic.service.support.registry.service;

import top.itning.generic.service.support.registry.pojo.ServiceInfo;

import java.util.Collection;

/**
 * @author itning
 * @since 2021/1/9 15:41
 */
public interface RegistryService {
    Collection<String> getAllEnv();

    ServiceInfo getProviders(String env);

    ServiceInfo getProvideDetail(String env, String provider);
}
