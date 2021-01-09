package top.itning.generic.service.jar.handle.util;

import com.google.common.base.Joiner;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

/**
 * 反射工具
 *
 * @author itning
 * @since 2021/1/1 11:11
 */
public class ReflectionUtils {

    /**
     * 中文逗号分隔
     */
    private static final Joiner JOINER = Joiner.on(", ").skipNulls();

    /**
     * 判断是不是基本类型或包装类型
     *
     * @param clazz 要判断的类
     * @return 是返回<code>true</code>
     */
    public static boolean isPrimitiveOrWrap(Class<?> clazz) {
        return clazz.isPrimitive() || isWrapClass(clazz);
    }

    /**
     * 是不是包装类型
     *
     * @param clazz 要判断的类
     * @return 是返回<code>true</code>
     */
    public static boolean isWrapClass(Class<?> clazz) {
        try {
            return ((Class<?>) clazz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 尝试加载一个类
     *
     * @param classLoader 类加载器
     * @param className   类名
     * @return 加载成功返回该类
     */
    public static Optional<Class<?>> tryLoadClass(ClassLoader classLoader, String className) {
        try {
            return Optional.ofNullable(classLoader.loadClass(className));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * 获取一个方法的文本签名
     *
     * @param method 方法
     * @return 文本签名
     */
    public static String signatureString(Method method) {
        String params = JOINER.join(Arrays.stream(method.getParameters()).map(parameter -> parameter.getParameterizedType().getTypeName() + " " + parameter.getName()).iterator());
        return Modifier.toString(method.getModifiers()) + " " + method.getReturnType().getName() + " " + method.getName() + "(" + params + ")";
    }
}
