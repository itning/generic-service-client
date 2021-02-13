package top.itning.generic.service.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author itning
 * @since 2021/2/14 0:23
 */
public class JsonUtils {
    public static final Gson GSON_INSTANCE_WITH_PRETTY_PRINT = new GsonBuilder().setPrettyPrinting().create();

    public static final Gson GSON_INSTANCE = new Gson();
}
