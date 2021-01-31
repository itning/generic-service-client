package top.itning.generic.service.nexus.service.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.itning.generic.service.common.jar.JarHandlerInterface;
import top.itning.generic.service.common.jar.MethodInfo;
import top.itning.generic.service.common.websocket.ProgressWebSocket;
import top.itning.generic.service.common.websocket.WebSocketMessageType;
import top.itning.generic.service.nexus.config.NexusProperties;
import top.itning.generic.service.nexus.entry.Artifact;
import top.itning.generic.service.nexus.entry.RestModel;
import top.itning.generic.service.nexus.http.ApacheHttpRequestClient;
import top.itning.generic.service.nexus.service.DependencyService;
import top.itning.generic.service.nexus.service.UserService;
import top.itning.generic.service.nexus.service.XmlService;
import top.itning.generic.service.nexus.util.XmlUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.SocketException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author itning
 * @since 2021/1/26 15:15
 */
@Slf4j
@Service
public class DependencyServiceImpl implements DependencyService, ApplicationContextAware {

    private static final Gson GSON_INSTANCE = new Gson();

    private static final ExecutorService EXECUTOR_SERVICE;

    static {
        int availableProcessors = Runtime.getRuntime().availableProcessors() * 2;
        EXECUTOR_SERVICE = new ThreadPoolExecutor(availableProcessors, availableProcessors,
                0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("download-thread-%d").build(),
                new ThreadPoolExecutor.AbortPolicy());
        Runtime.getRuntime().addShutdownHook(new Thread(EXECUTOR_SERVICE::shutdown));
    }

    private static final Map<String, HttpGet> CANCEL_MAP = new ConcurrentHashMap<>();

    private static final Map<String, String> DOWNLOAD_TOKEN_INFO = new ConcurrentHashMap<>();

    private final UserService userService;
    private final XmlService xmlService;
    private final NexusProperties nexusProperties;
    private final ApacheHttpRequestClient httpRequestClient;
    private JarHandlerInterface jarHandlerInterface;

    @Autowired
    public DependencyServiceImpl(UserService userService, XmlService xmlService, NexusProperties nexusProperties, ApacheHttpRequestClient httpRequestClient) {
        this.userService = userService;
        this.xmlService = xmlService;
        this.nexusProperties = nexusProperties;
        this.httpRequestClient = httpRequestClient;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            jarHandlerInterface = applicationContext.getBean(JarHandlerInterface.class);
        } catch (BeansException e) {
            log.error("Bean Exception", e);
            throw e;
        }
    }

    @Override
    public List<Artifact> get(String dependencyXml) {
        return get(dependencyXml, 0);
    }

    @Override
    public RestModel<?> download(String dependencyXml, String interfaceName, String methodName, String token, String echo) {

        log.info("解析XML:{}", dependencyXml);
        Optional<Artifact> artifactOptional = xmlService.parseArtifactInfo(dependencyXml);
        if (!artifactOptional.isPresent()) {
            return RestModel.failed("XML解析失败");
        }
        Artifact artifact = artifactOptional.get();
        // 2.判断解析结果有Version
        if (!StringUtils.hasText(artifact.getVersion())) {
            return RestModel.failed("未解析到version版本信息");
        }
        artifact.setSnapshotVersion(null);
        final String downloadToken = DigestUtils.md5Hex(artifact.getGroupId() + artifact.getArtifactId() + artifact.getVersion());
        if (CANCEL_MAP.containsKey(downloadToken)) {
            return RestModel.failed("Ta人正在下载，请稍后再试");
        }
        // 3.判断存在maven-metadata.xml有的化拿到版本号
        checkAndGetVersionFromMavenMetaDataXml(nexusProperties.getBaseUrl(), artifact, 0).ifPresent(artifact::setSnapshotVersion);
        // 4.判断存在md5文件->存在获取->判断本地一致
        Optional<File> localFile = getLocalFileIfExist(nexusProperties.getFileDir(), nexusProperties.getBaseUrl(), artifact, 0);
        if (localFile.isPresent()) {
            postProcess(localFile.get(), interfaceName, methodName, token, echo);
            return RestModel.success();
        }
        // 5.下载JAR
        String lastToken = DOWNLOAD_TOKEN_INFO.get(token);
        if (null != lastToken) {
            this.cancel(lastToken);
            DOWNLOAD_TOKEN_INFO.remove(token);
        }
        log.info("Artifact:{} 下载Token:{}", artifact, downloadToken);
        try {
            EXECUTOR_SERVICE.submit(() -> {
                try {
                    ProgressWebSocket.sendMessage(token, echo, WebSocketMessageType.NEXUS_DOWNLOAD_CANCEL_TOKEN, downloadToken);
                    DOWNLOAD_TOKEN_INFO.put(token, downloadToken);
                    URI uri = artifact.toURI(nexusProperties.getBaseUrl(), artifact.getArtifactId() + "-" + artifact.getAvailableVersion() + ".jar");
                    HttpGet httpGet = new HttpGet(uri);
                    CANCEL_MAP.put(downloadToken, httpGet);
                    downloadFile(httpGet, nexusProperties.getFileDir(), artifact, token, echo, 0).ifPresent(file -> postProcess(file, interfaceName, methodName, token, echo));
                } catch (Exception e) {
                    log.info("下载失败：", e);
                    ProgressWebSocket.sendMessage(token, echo, WebSocketMessageType.NEXUS_DOWNLOAD_FAILED, e.getMessage());
                } finally {
                    CANCEL_MAP.remove(downloadToken);
                    DOWNLOAD_TOKEN_INFO.remove(token);
                }
            });
        } catch (RejectedExecutionException e) {
            log.warn("线程池满了：", e);
            return RestModel.failed("下载队列已满，请稍后再试");
        }
        return RestModel.success();
    }

    @Override
    public void cancel(String token) {
        HttpGet httpGet = CANCEL_MAP.get(token);
        if (null != httpGet) {
            httpGet.abort();
        }
        log.info("取消Token:{}", token);
    }

    private void postProcess(File file, String interfaceName, String methodName, String token, String echo) {
        if (null != jarHandlerInterface) {
            List<MethodInfo> handler = jarHandlerInterface.handler(file, interfaceName, methodName);
            ProgressWebSocket.sendMessage(token, echo, WebSocketMessageType.NEXUS_DOWNLOAD_SUCCESS, GSON_INSTANCE.toJson(handler));
        }
    }

    private void progress(long downloadBytes, long totalBytes, long thisTimeDownloadBytes, long startTime, long endTime, String token, String echo) {
        double speed = ((double) thisTimeDownloadBytes / (endTime - startTime)) * 1000 / 1024;
        ProgressWebSocket.sendMessage(token, echo, WebSocketMessageType.NEXUS_DOWNLOAD_PROGRESS, downloadBytes + "-" + totalBytes + "-" + speed);
    }

    private Optional<File> downloadFile(HttpGet httpGet, String dir, Artifact artifact, String token, String echo, int retryCount) throws Exception {

        if (retryCount > 5) {
            return Optional.empty();
        }
        try (CloseableHttpResponse response = httpRequestClient.get(httpGet)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_UNAUTHORIZED == statusCode) {
                userService.login();
                return downloadFile(httpGet, dir, artifact, token, echo, retryCount + 1);
            }
            if (HttpStatus.SC_NOT_FOUND == statusCode) {
                throw new RuntimeException("没找到依赖信息");
            }
            if (HttpStatus.SC_OK != statusCode) {
                log.info("下载非200状态码：{} URL：{}", statusCode, httpGet.getURI());
                throw new RuntimeException("下载失败：" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String filename = dir + artifact.getArtifactId() + "-" + artifact.getAvailableVersion() + ".jar";
            File targetFile = new File(filename);
            if (targetFile.exists()) {
                log.info("文件已经存在，即将删除：{}", targetFile);
                boolean delete = targetFile.delete();
                if (!delete) {
                    throw new RuntimeException("删除文件失败：" + targetFile);
                }
            }
            try (InputStream inputStream = entity.getContent();
                 FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
                byte[] buf = new byte[8192];
                // 总共下载的字节数
                long totalDownloadBytes = 0;
                // 本次下载的字节数
                int thisTimeDownloadBytes;
                // 总共字节数
                long totalBytes = entity.getContentLength();
                long downloadStartTime;
                // 一段时间内开始
                long rangeTimeStart = downloadStartTime = System.currentTimeMillis();
                // 一段时间内的下载字节数
                int rangeTimeDownloadBytes = 0;
                while ((thisTimeDownloadBytes = inputStream.read(buf)) > 0) {
                    fileOutputStream.write(buf, 0, thisTimeDownloadBytes);
                    long rangeTimeEnd = System.currentTimeMillis();
                    totalDownloadBytes += thisTimeDownloadBytes;
                    rangeTimeDownloadBytes += thisTimeDownloadBytes;
                    // 每一秒取一次
                    if (rangeTimeEnd - rangeTimeStart > 1000) {
                        progress(totalDownloadBytes, totalBytes, rangeTimeDownloadBytes, rangeTimeStart, rangeTimeEnd, token, echo);
                        rangeTimeDownloadBytes = 0;
                        rangeTimeStart = System.currentTimeMillis();
                    }
                }
                progress(totalDownloadBytes, totalBytes, totalBytes, downloadStartTime, System.currentTimeMillis(), token, echo);
                if (httpGet.isAborted()) {
                    return Optional.empty();
                }
                return Optional.of(targetFile);
            } finally {
                log.info("文件复制结束：{}", httpGet.getURI());
                if (httpGet.isAborted()) {
                    ProgressWebSocket.sendMessage(token, echo, WebSocketMessageType.NEXUS_DOWNLOAD_FAILED, "下载已经被取消");
                    log.info("下载已经被取消，删除遗留文件：{}", targetFile);
                    boolean delete = targetFile.delete();
                    if (delete) {
                        log.info("下载已经被取消，删除遗留文件成功");
                    } else {
                        log.error("下载已经被取消，删除遗留文件失败：{}", targetFile);
                    }
                }
            }
        } catch (SocketException e) {
            if (httpGet.isAborted()) {
                log.warn("连接被中断了:{}", e.getMessage());
            } else {
                log.error("连接被中断了", e);
            }
        } finally {
            log.info("下载执行结束：{}", httpGet.getURI());
        }
        return Optional.empty();
    }

    private Optional<File> getLocalFileIfExist(String dir, String baseUrl, Artifact artifact, int retryCount) {

        if (retryCount > 5) {
            return Optional.empty();
        }
        URI md5Uri = artifact.toURI(baseUrl, artifact.getArtifactId() + "-" + artifact.getAvailableVersion() + ".jar.md5");
        try (CloseableHttpResponse response = httpRequestClient.get(md5Uri)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_UNAUTHORIZED == statusCode) {
                userService.login();
                return getLocalFileIfExist(dir, baseUrl, artifact, retryCount);
            }
            if (HttpStatus.SC_OK == statusCode) {
                HttpEntity entity = response.getEntity();
                String md5 = EntityUtils.toString(entity);
                log.info("Remote File MD5：{}", md5);
                String filePath = dir + artifact.getArtifactId() + "-" + artifact.getAvailableVersion() + ".jar";
                File file = new File(filePath);
                String computationalMd5 = null;
                if (StringUtils.hasText(md5) && file.exists()) {
                    try (FileInputStream fileInputStream = new FileInputStream(file)) {
                        computationalMd5 = DigestUtils.md5Hex(fileInputStream);
                    } finally {
                        log.info("MD5计算结束：{}", computationalMd5);
                    }
                    if (computationalMd5.equals(md5)) {
                        log.info("本地文件验证成功：MD5:{} 文件：{}", computationalMd5, file);
                        return Optional.of(file);
                    }
                }
                log.info("本地文件验证失败：文件存在[{}] 远程MD5信息[{}] 本地MD5信息[{}]", file.exists(), md5, computationalMd5);
            }
        } catch (Exception e) {
            log.error("Get {} Exception", md5Uri, e);
        }
        return Optional.empty();
    }

    private Optional<String> checkAndGetVersionFromMavenMetaDataXml(String baseUrl, Artifact artifact, int retryCount) {

        if (retryCount > 5) {
            return Optional.empty();
        }
        URI mavenMetaDataXmlUri = artifact.toURI(baseUrl, "maven-metadata.xml");
        try (CloseableHttpResponse response = httpRequestClient.get(mavenMetaDataXmlUri)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_UNAUTHORIZED == statusCode) {
                userService.login();
                return checkAndGetVersionFromMavenMetaDataXml(baseUrl, artifact, retryCount + 1);
            }
            if (HttpStatus.SC_OK == statusCode) {
                HttpEntity entity = response.getEntity();
                String xml = EntityUtils.toString(entity);
                log.info("maven-metadata.xml：{}", xml);
                Document document = DocumentHelper.parseText(xml);
                return XmlUtils.parseSnapshotVersion(document);
            }
        } catch (Exception e) {
            log.error("Get maven-metadata.xml Exception", e);
        }
        return Optional.empty();
    }

    private List<Artifact> get(String dependencyXml, int retryCount) {

        if (retryCount > 5) {
            return Collections.emptyList();
        }
        Optional<Artifact> artifactOptional = xmlService.parseArtifactInfo(dependencyXml);
        if (!artifactOptional.isPresent()) {
            return Collections.emptyList();
        }
        Artifact artifact = artifactOptional.get();
        if (StringUtils.hasText(artifact.getVersion())) {
            return Collections.singletonList(artifact);
        }

        URI uri = artifact.toURIWithoutVersion(nexusProperties.getBaseUrl(), "maven-metadata.xml");
        try (CloseableHttpResponse response = httpRequestClient.get(uri)) {
            if (HttpStatus.SC_UNAUTHORIZED == response.getStatusLine().getStatusCode()) {
                userService.login();
                return get(dependencyXml, retryCount + 1);
            }
            HttpEntity entity = response.getEntity();
            String xml = EntityUtils.toString(entity);
            Document document = DocumentHelper.parseText(xml);
            return XmlUtils.parseVersionInfo(document)
                    .stream()
                    .map(it -> {
                        Artifact artifactItem = new Artifact();
                        artifactItem.setGroupId(artifact.getGroupId());
                        artifactItem.setArtifactId(artifact.getArtifactId());
                        artifactItem.setVersion(it);
                        return artifactItem;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Get XML Exception", e);
        }
        return Collections.emptyList();
    }
}
