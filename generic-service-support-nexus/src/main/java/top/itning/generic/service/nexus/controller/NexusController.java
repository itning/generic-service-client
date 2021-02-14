package top.itning.generic.service.nexus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.itning.generic.service.nexus.entry.Artifact;
import top.itning.generic.service.nexus.entry.DownloadInfo;
import top.itning.generic.service.common.model.RestModel;
import top.itning.generic.service.nexus.http.HttpRequestClientFactory;
import top.itning.generic.service.nexus.service.DependencyService;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author itning
 * @since 2021/1/23 16:25
 */
@Validated
@CrossOrigin
@RestController
@RequestMapping("/nexus")
public class NexusController {

    private final DependencyService dependencyService;

    @Autowired
    public NexusController(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    @PostConstruct
    public void init() {
        HttpRequestClientFactory.withDefaultClient();
    }

    @PostMapping("/dependency/parse")
    public RestModel<List<Artifact>> parseDependency(@NotEmpty(message = "依赖信息不能为空") String dependency) {
        try {
            return RestModel.success(dependencyService.get(dependency));
        } catch (Exception e) {
            return RestModel.failed(e.getMessage());
        }
    }

    @PostMapping("/dependency/download")
    public RestModel<?> downloadDependency(@RequestBody @Validated DownloadInfo downloadInfo) {
        return dependencyService.download(downloadInfo.getDependency(), downloadInfo.getInterfaceName(), downloadInfo.getMethodName(), downloadInfo.getToken(), downloadInfo.getEcho());
    }

    @GetMapping("/dependency/download/cancel")
    public void cancelDownloadDependency(@NotEmpty(message = "Token不能为空") String token) {
        dependencyService.cancel(token);
    }
}
