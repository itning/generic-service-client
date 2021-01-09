package top.itning.generic.service.support.registry.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author itning
 * @since 2021/1/9 15:43
 */
@Data
public class ServiceInfo implements Serializable {
    private boolean success;
    private boolean regConnected;
    private Date updateTime;
    private String env;
    private List<String> data;

    public static ServiceInfo failed(String env, boolean regConnected) {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setSuccess(false);
        serviceInfo.setRegConnected(regConnected);
        serviceInfo.setUpdateTime(new Date());
        serviceInfo.setEnv(env);
        serviceInfo.setData(Collections.emptyList());
        return serviceInfo;
    }

    public static ServiceInfo success(String env, List<String> data) {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setSuccess(true);
        serviceInfo.setRegConnected(true);
        serviceInfo.setUpdateTime(new Date());
        serviceInfo.setEnv(env);
        serviceInfo.setData(data);
        return serviceInfo;
    }
}
