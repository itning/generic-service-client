package top.itning.generic.service.zk.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.itning.generic.service.zk.dto.ZkInfo;
import top.itning.generic.service.zk.service.ZkInfoService;

import java.util.Set;

/**
 * @author itning
 * @since 2020/12/24 15:05
 */
@CrossOrigin
@RestController
@RequestMapping("/zk")
public class ZkController {
    private final ZkInfoService zkInfoService;

    @Autowired
    public ZkController(ZkInfoService zkInfoService) {
        this.zkInfoService = zkInfoService;
    }

    @GetMapping("/env")
    public Set<String> allEnv() {
        return zkInfoService.getEnv();
    }

    @GetMapping("/node")
    public ZkInfo node(String env) {
        return zkInfoService.getChildNodeNameWithCache(env, "/dubbo");
    }

    @GetMapping("/providers")
    public ZkInfo value(String env, String name) {
        return zkInfoService.getChildNodeName(env, "/dubbo/" + name + "/providers");
    }
}
