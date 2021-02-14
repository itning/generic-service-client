package top.itning.generic.service.nexus.service;


import top.itning.generic.service.nexus.entry.Artifact;
import top.itning.generic.service.common.model.RestModel;

import java.util.List;

/**
 * @author itning
 * @since 2021/1/26 15:13
 */
public interface DependencyService {
    List<Artifact> get(String dependencyXml);

    RestModel<?> download(String dependencyXml, String interfaceName, String methodName, String token, String echo);

    void cancel(String token);
}
