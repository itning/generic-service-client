package top.itning.generic.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "top.itning.generic.service")
@EnableScheduling
public class GenericServiceClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(GenericServiceClientApplication.class, args);
    }

}
