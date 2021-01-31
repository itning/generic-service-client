package top.itning.generic.service.nexus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author itning
 * @since 2020/12/24 14:37
 */
@ConfigurationProperties(prefix = "generic-service-support-nexus")
@Component
@Data
public class NexusProperties {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 私服URL
     */
    private String baseUrl;

    /**
     * 下载目录，默认临时目录
     */
    private String fileDir;

    /**
     * 私服URL
     *
     * @param baseUrl URL
     */
    public void setBaseUrl(String baseUrl) {
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        this.baseUrl = baseUrl;
    }

    public String getFileDir() {
        if (null == fileDir) {
            fileDir = System.getProperty("java.io.tmpdir") + File.separator;
        }
        return fileDir;
    }

    /**
     * 下载目录，默认临时目录
     *
     * @param fileDir 目录
     */
    public void setFileDir(String fileDir) {
        File file = new File(fileDir);
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("不是目录：" + fileDir);
        }
        if (!file.exists()) {
            boolean success = file.mkdir();
            if (!success) {
                throw new RuntimeException("目录创建失败");
            }
        }
        this.fileDir = fileDir;
    }
}
