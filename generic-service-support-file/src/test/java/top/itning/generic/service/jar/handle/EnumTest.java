package top.itning.generic.service.jar.handle;

import lombok.Getter;

/**
 * @author itning
 * @since 2021/1/2 10:10
 */
public enum EnumTest {
    A("a"),
    B("a"),
    C("a"),
    D("a");

    @Getter
    private final String name;

    EnumTest(String name) {
        this.name = name;
    }
}
