package top.itning.generic.service.nexus.entry;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author itning
 * @since 2021/1/28 9:55
 */
@Data
public class DownloadInfo implements Serializable {

    @NotEmpty(message = "依赖信息不能为空")
    String dependency;

    @NotEmpty(message = "接口名不能为空")
    String interfaceName;

    @NotEmpty(message = "方法名不能为空")
    String methodName;

    @NotEmpty(message = "Token不能为空")
    String token;

    String echo;
}
