package top.itning.generic.service.nexus.entry;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.net.URI;

/**
 * @author itning
 * @since 2021/1/23 16:18
 */
@ToString
@Getter
public class Artifact {
    private String groupId;
    private String artifactId;
    private String version;
    private String snapshotVersion;

    public void setSnapshotVersion(String snapshotVersion) {
        this.snapshotVersion = snapshotVersion;
    }

    public void setGroupId(String groupId) {
        if (!StringUtils.hasText(groupId)) {
            throw new IllegalArgumentException("groupId不能为空");
        }
        this.groupId = groupId;
    }

    public void setArtifactId(String artifactId) {
        if (!StringUtils.hasText(artifactId)) {
            throw new IllegalArgumentException("artifactId不能为空");
        }
        this.artifactId = artifactId;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAvailableVersion() {
        if (StringUtils.hasText(snapshotVersion)) {
            return snapshotVersion;
        }
        return version;
    }

    public URI toURI(String baseUrl, String path) {

        String groupString = Joiner.on('/').join(Splitter.on('.').split(this.groupId));
        String artifactString = Joiner.on('/').join(Splitter.on('.').split(this.artifactId));

        String uriString = baseUrl + "content/groups/public/" +
                groupString +
                "/" +
                artifactString +
                "/" +
                (null == version ? "" : version);
        if (!uriString.endsWith("/")) {
            uriString += "/";
        }
        uriString += path == null ? "" : path;
        return URI.create(uriString);
    }

    public URI toURIWithoutVersion(String baseUrl) {
        return toURIWithoutVersion(baseUrl, "");
    }

    public URI toURIWithoutVersion(String baseUrl, String path) {

        String groupString = Joiner.on('/').join(Splitter.on('.').split(this.groupId));
        String artifactString = Joiner.on('/').join(Splitter.on('.').split(this.artifactId));

        String uriString = baseUrl + "content/groups/public/" +
                groupString +
                "/" +
                artifactString +
                "/" +
                path;
        return URI.create(uriString);
    }
}
