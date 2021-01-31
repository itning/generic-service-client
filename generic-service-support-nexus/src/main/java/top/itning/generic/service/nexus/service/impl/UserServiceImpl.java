package top.itning.generic.service.nexus.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.stereotype.Service;
import top.itning.generic.service.nexus.config.NexusProperties;
import top.itning.generic.service.nexus.http.ApacheHttpRequestClient;
import top.itning.generic.service.nexus.service.UserService;

import java.util.*;

/**
 * @author itning
 * @since 2021/1/26 14:34
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final ApacheHttpRequestClient httpRequestClient;
    private final NexusProperties nexusProperties;

    public UserServiceImpl(ApacheHttpRequestClient httpRequestClient, NexusProperties nexusProperties) {
        this.httpRequestClient = httpRequestClient;
        this.nexusProperties = nexusProperties;
    }

    @Override
    public Optional<String> login() {
        log.info("登录请求...");
        String encode = Base64.getEncoder().encodeToString((nexusProperties.getUsername() + ":" + nexusProperties.getPassword()).getBytes());
        Map<String, String> headerMap = Collections.singletonMap("Authorization", "Basic " + encode);
        try (CloseableHttpResponse response = httpRequestClient.getForResponse(nexusProperties.getBaseUrl() + "service/local/authentication/login", headerMap, Collections.emptyMap())) {
            Header header = response.getFirstHeader("Set-Cookie");
            if (null == header) {
                return Optional.empty();
            }
            return Arrays.stream(header.getElements()).filter(it -> "NXSESSIONID".equals(it.getName())).map(HeaderElement::getValue).findAny();
        } catch (Exception e) {
            log.error("Login Exception", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean logout(String cookie) {
        log.info("登出请求...");
        Map<String, String> headerMap = Collections.singletonMap("Cookie", "NXSESSIONID=" + cookie);
        try (CloseableHttpResponse response = httpRequestClient.getForResponse(nexusProperties.getBaseUrl() + "service/local/authentication/logout", headerMap, Collections.emptyMap())) {
            return HttpStatus.SC_OK == response.getStatusLine().getStatusCode();
        } catch (Exception e) {
            log.error("Logout Exception", e);
        }
        return false;
    }
}
