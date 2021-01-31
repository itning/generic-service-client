package top.itning.generic.service.nexus.entry;

import lombok.Data;

import java.util.List;

/**
 * @author itning
 * @since 2021/1/23 17:14
 */
@Data
public class Dependency {
    private String msg;
    private List<String> versions;
}
