package top.itning.generic.service.zk.service;

import top.itning.generic.service.support.registry.service.RegistryService;
import top.itning.generic.service.zk.dto.ZkInfo;

import java.util.Set;

/**
 * @author itning
 * @since 2020/12/24 14:30
 */
public interface ZkInfoService extends RegistryService {
    ZkInfo getChildNodeName(String env, String nodeName);

    ZkInfo getChildNodeNameWithCache(String env, String nodeName);

    Set<String> getEnv();
}
