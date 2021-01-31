package top.itning.generic.service.nexus.http;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author itning
 * @since 2020/9/1 8:57
 */
public class ApacheHttpRequestClient implements HttpRequestClient {
    private final CloseableHttpClient httpClient;

    public ApacheHttpRequestClient() {
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        // 连接池最大连接数
        poolingHttpClientConnectionManager.setMaxTotal(300);
        // 单个路由默认最大连接数
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(50);

        int timeout = 10;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000)
                .build();
        httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .setConnectionManager(poolingHttpClientConnectionManager)
                .build();
    }

    @Override
    public String post(String url, Map<String, String> headerMap, Map<String, String> bodyMap) throws Exception {
        List<NameValuePair> parameters = new ArrayList<>(bodyMap.size());
        bodyMap.forEach((k, v) -> parameters.add(new BasicNameValuePair(k, v)));
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters);

        HttpPost httpPost = new HttpPost(url);
        headerMap.forEach(httpPost::addHeader);
        httpPost.setEntity(formEntity);

        return executeRequest(httpPost);
    }

    @Override
    public String get(String url, Map<String, String> headerMap, Map<String, String> searchParamMap) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(URI.create(url));
        searchParamMap.forEach(uriBuilder::addParameter);
        URI uri = uriBuilder.build();

        HttpGet httpget = new HttpGet(uri);
        headerMap.forEach(httpget::addHeader);

        return executeRequest(httpget);
    }

    public CloseableHttpResponse get(URI uri, Map<String, String> headerMap) throws Exception {
        HttpGet httpget = new HttpGet(uri);
        headerMap.forEach(httpget::addHeader);
        return httpClient.execute(httpget);
    }

    public CloseableHttpResponse get(HttpGet httpGet) throws Exception {
        return httpClient.execute(httpGet);
    }

    public CloseableHttpResponse get(URI uri) throws Exception {
        HttpGet httpget = new HttpGet(uri);
        return httpClient.execute(httpget);
    }

    private String executeRequest(final HttpUriRequest request) throws IOException {
        CloseableHttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }

    public CloseableHttpResponse getForResponse(String url, Map<String, String> headerMap, Map<String, String> searchParamMap) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(URI.create(url));
        searchParamMap.forEach(uriBuilder::addParameter);
        URI uri = uriBuilder.build();

        HttpGet httpget = new HttpGet(uri);
        headerMap.forEach(httpget::addHeader);

        return httpClient.execute(httpget);
    }
}
