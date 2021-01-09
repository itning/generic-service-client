package top.itning.generic.service.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author itning
 * @since 2021/1/9 20:10
 */
@Controller
public class WebController {
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/generic")
    public String index2() {
        return "index";
    }

    @GetMapping("/index.html")
    public String index3() {
        return "index";
    }
}
