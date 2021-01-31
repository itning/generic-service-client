package top.itning.generic.service.nexus.util;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import top.itning.generic.service.nexus.entry.Artifact;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author itning
 * @since 2021/1/23 16:20
 */
@Slf4j
public class XmlUtils {

    public static Artifact parseArtifactInfo(final Document document) {

        Element rootElement = document.getRootElement();
        if (!"dependency".equals(rootElement.getName())) {
            throw new IllegalArgumentException("根节点非dependency");
        }
        Artifact artifact = new Artifact();
        Element groupId = rootElement.element("groupId");
        if (null == groupId) {
            throw new IllegalArgumentException("groupId节点不存在");
        }
        Element artifactId = rootElement.element("artifactId");
        if (null == artifactId) {
            throw new IllegalArgumentException("artifactId节点不存在");
        }
        Element version = rootElement.element("version");

        artifact.setGroupId(groupId.getText());
        artifact.setArtifactId(artifactId.getText());
        artifact.setVersion(version == null ? null : version.getText());
        return artifact;
    }

    @SuppressWarnings("unchecked")
    public static List<String> parseVersionInfo(final Document document) {
        try {
            List<DefaultElement> list = document.selectNodes("//version");
            List<String> result = list.stream().map(DefaultElement::getText).collect(Collectors.toList());
            Collections.reverse(result);
            return result;
        } catch (Exception e) {
            log.error("Parse Version Exception", e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public static Optional<String> parseSnapshotVersion(final Document document) {
        List<DefaultElement> list = document.selectNodes("//snapshotVersion");
        for (DefaultElement defaultElement : list) {
            if (null == defaultElement.element("classifier")) {
                Element extension = defaultElement.element("extension");
                if (null != extension && "jar".equals(extension.getText())) {
                    Element value = defaultElement.element("value");
                    if (null != value) {
                        return Optional.of(value.getText());
                    }
                }
            }
        }
        return Optional.empty();
    }
}
