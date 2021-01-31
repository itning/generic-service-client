package top.itning.generic.service.nexus.service;

import java.util.Optional;

/**
 * @author itning
 * @since 2021/1/26 14:26
 */
public interface UserService {
    Optional<String> login();

    boolean logout(String cookie);
}
