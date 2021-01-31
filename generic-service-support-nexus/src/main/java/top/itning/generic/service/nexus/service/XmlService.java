package top.itning.generic.service.nexus.service;


import top.itning.generic.service.nexus.entry.Artifact;

import java.util.Optional;

/**
 * @author itning
 * @since 2021/1/23 16:32
 */
public interface XmlService {
    Optional<Artifact> parseArtifactInfo(String xml);
}
