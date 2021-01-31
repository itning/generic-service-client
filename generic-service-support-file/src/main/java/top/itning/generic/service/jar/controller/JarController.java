package top.itning.generic.service.jar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.itning.generic.service.common.jar.MethodInfo;
import top.itning.generic.service.jar.handle.JarHandler;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

/**
 * @author itning
 * @since 2020/12/27 17:01
 */
@Validated
@CrossOrigin
@RestController
@RequestMapping("/jar")
public class JarController {

    private final JarHandler jarHandler;

    @Autowired
    public JarController(JarHandler jarHandler) {
        this.jarHandler = jarHandler;
    }

    @PostMapping("/upload")
    public List<MethodInfo> upload(@NotNull(message = "文件不能为空") MultipartFile file,
                                   @NotEmpty(message = "接口名不能为空") String interfaceName,
                                   @NotEmpty(message = "方法名不能为空") String methodName) {
        if (file.isEmpty()) {
            return Collections.emptyList();
        }
        return jarHandler.handle(file, interfaceName, methodName);
    }
}
