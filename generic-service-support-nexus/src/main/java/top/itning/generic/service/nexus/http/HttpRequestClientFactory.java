package top.itning.generic.service.nexus.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author itning
 * @since 2020/8/31 13:58
 */
@Configuration
public class HttpRequestClientFactory {
    private static HttpRequestClient HTTP_REQUEST_CLIENT;

    public static HttpRequestClient withDefaultClient() {
        if (null == HTTP_REQUEST_CLIENT) {
            synchronized (HttpRequestClientFactory.class) {
                if (null == HTTP_REQUEST_CLIENT) {
                    HTTP_REQUEST_CLIENT = createApacheHttpRequestClient();
                }
            }
        }
        return HTTP_REQUEST_CLIENT;
    }

    private static ApacheHttpRequestClient createApacheHttpRequestClient() {
        return new ApacheHttpRequestClient();
    }

    @Bean
    public ApacheHttpRequestClient apacheHttpRequestClient() {
        return HttpRequestClientFactory.createApacheHttpRequestClient();
    }
}
