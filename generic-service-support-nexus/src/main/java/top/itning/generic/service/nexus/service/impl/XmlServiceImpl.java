package top.itning.generic.service.nexus.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.springframework.stereotype.Service;
import top.itning.generic.service.nexus.entry.Artifact;
import top.itning.generic.service.nexus.service.XmlService;
import top.itning.generic.service.nexus.util.XmlUtils;

import java.util.Optional;

/**
 * @author itning
 * @since 2021/1/23 16:33
 */
@Slf4j
@Service
public class XmlServiceImpl implements XmlService {
    @Override
    public Optional<Artifact> parseArtifactInfo(String xml) {
        try {
            Document document = DocumentHelper.parseText(xml);
            return Optional.of(XmlUtils.parseArtifactInfo(document));
        } catch (DocumentException e) {
            log.warn("Document Parse Exception", e);
        }
        return Optional.empty();
    }
}
