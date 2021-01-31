package top.itning.generic.service.nexus.http;

import java.util.Map;

/**
 * @author itning
 * @since 2020/8/29 12:31
 */
public interface HttpRequestClient {
    String post(String url, Map<String, String> headerMap, Map<String, String> bodyMap) throws Exception;

    String get(String url, Map<String, String> headerMap, Map<String, String> searchParamMap) throws Exception;
}
