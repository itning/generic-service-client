package top.itning.generic.service.support.registry.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.itning.generic.service.support.registry.pojo.ServiceInfo;
import top.itning.generic.service.support.registry.service.RegistryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author itning
 * @since 2021/1/9 16:04
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/service")
public class ServiceInfoController implements ApplicationContextAware {

    private Map<String, RegistryService> beansOfType;

    @GetMapping("/env")
    public List<String> env() {
        List<String> list = new ArrayList<>(beansOfType.size());
        beansOfType.forEach((tag, bean) -> bean.getAllEnv().forEach(i -> list.add(tag + "||" + i)));
        return list;
    }

    @GetMapping("/providers")
    public ServiceInfo getProviders(String tag, String env) {
        RegistryService registryService = beansOfType.get(tag);
        if (null == registryService) {
            return ServiceInfo.failed(env, false);
        }
        return registryService.getProviders(env);
    }

    @GetMapping("/provideDetail")
    public ServiceInfo getProvideDetail(String tag, String env, String interfaceName) {
        RegistryService registryService = beansOfType.get(tag);
        if (null == registryService) {
            return ServiceInfo.failed(env, false);
        }
        return registryService.getProvideDetail(env, interfaceName);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        beansOfType = applicationContext.getBeansOfType(RegistryService.class);
        beansOfType.keySet()
                .stream()
                .filter(key -> key.contains("||"))
                .forEach(item -> log.error("RegistryService Implement:{} Can Not Contains '||' String, Please Rename Bean Name", item));
    }
}
