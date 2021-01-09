package top.itning.generic.service.zk.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author itning
 * @since 2020/12/24 15:16
 */
@Data
public class ZkInfo implements Serializable {
    private boolean success;
    private boolean zkConnected;
    private Date updateTime;
    private String env;
    private List<String> nodes;

    public static ZkInfo withFailed(String env, boolean zkConnected) {
        ZkInfo zkInfo = new ZkInfo();
        zkInfo.setSuccess(false);
        zkInfo.setUpdateTime(new Date());
        zkInfo.setEnv(env);
        zkInfo.setZkConnected(zkConnected);
        zkInfo.setNodes(Collections.emptyList());
        return zkInfo;
    }

    public static ZkInfo withFailed(String env) {
        return withFailed(env, true);
    }

    public static ZkInfo withSuccess(String env, List<String> nodes) {
        ZkInfo zkInfo = new ZkInfo();
        zkInfo.setSuccess(true);
        zkInfo.setUpdateTime(new Date());
        zkInfo.setEnv(env);
        zkInfo.setZkConnected(true);
        zkInfo.setNodes(nodes);
        return zkInfo;
    }
}
