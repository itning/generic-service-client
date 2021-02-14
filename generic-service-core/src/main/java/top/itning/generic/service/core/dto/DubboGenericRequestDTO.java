package top.itning.generic.service.core.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * dubbo泛化调用入参
 *
 * @author itning
 * @since 2020/10/19 16:13
 */
@Data
public class DubboGenericRequestDTO implements Serializable {

    @NotEmpty(message = "URL不能为空")
    private String url;

    @NotEmpty(message = "接口名不能为空")
    private String interfaceName;

    @NotEmpty(message = "方法名不能为空")
    private String method;

    private String version;

    private String group;

    private List<Map<String, Object>> params;
}
