package top.itning.generic.service.core.bo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * dubbo泛化调用入参
 *
 * @author itning
 * @since 2020/10/19 16:17
 */
@Data
public class DubboGenericRequestBO {

    private String url;

    private String interfaceName;

    private String method;

    private String version;

    private String group;

    private Integer retries;

    private Integer timeout;

    private List<Map<String, Object>> params;

    private String token;

    private String echo;

    public Integer getRetries() {
        return null == retries ? 0 : retries;
    }

    public Integer getTimeout() {
        return null == timeout ? 5000 : timeout;
    }
}
