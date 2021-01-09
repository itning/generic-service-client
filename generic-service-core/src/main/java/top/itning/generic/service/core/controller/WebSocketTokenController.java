package top.itning.generic.service.core.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author itning
 * @since 2020/10/26 10:11
 */
@CrossOrigin
@RestController
public class WebSocketTokenController {

    @GetMapping("/socket_token")
    public String token() {
        return UUID.randomUUID().toString();
    }
}
