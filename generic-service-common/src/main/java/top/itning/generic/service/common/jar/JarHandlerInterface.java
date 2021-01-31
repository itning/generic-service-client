package top.itning.generic.service.common.jar;

import java.io.File;
import java.util.List;

/**
 * @author itning
 * @since 2021/1/28 9:44
 */
public interface JarHandlerInterface {
    List<MethodInfo> handler(File file, String interfaceName, String methodName);
}
