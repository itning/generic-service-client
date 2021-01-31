package top.itning.generic.service.common.jar;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author itning
 * @since 2020/12/26 12:21
 */
@Data
public class MethodInfo implements Serializable {
    private String signature;
    private List<String> paramClassName;
    private List<Map<String, Object>> property;
}
