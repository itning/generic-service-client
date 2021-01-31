package top.itning.generic.service.jar.handle;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import top.itning.generic.service.common.jar.JarHandlerInterface;
import top.itning.generic.service.common.jar.MethodInfo;
import top.itning.generic.service.jar.handle.spring.boot.JarLauncher;
import top.itning.generic.service.jar.handle.spring.boot.WarLauncher;
import top.itning.generic.service.jar.handle.spring.boot.archive.JarFileArchive;
import top.itning.generic.service.jar.handle.zip.ZipClassLoader;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static top.itning.generic.service.jar.handle.util.ReflectionUtils.*;

/**
 * 处理器
 *
 * @author itning
 * @since 2020/12/26 11:26
 */
@Slf4j
@Component
public class JarHandler implements JarHandlerInterface {

    private static final Gson GSON_INSTANCE = new Gson();

    @Override
    public List<MethodInfo> handler(File file, String interfaceName, String methodName) {
        StopWatch stopwatch = new StopWatch("Handle " + interfaceName + " " + methodName);
        List<MethodInfo> result = new ArrayList<>();
        try {
            jarFileHandle(interfaceName, methodName, stopwatch, result, file);
            if (result.isEmpty()) {
                zipFileHandle(interfaceName, methodName, stopwatch, result, file);
            }
        } catch (Exception e) {
            log.warn("处理失败：InterfaceName:{} MethodName:{}", interfaceName, methodName, e);
        } finally {
            log.info("Total Cost：{} Seconds", stopwatch.getTotalTimeSeconds());
            log.info(stopwatch.prettyPrint());
        }
        return result;
    }

    /**
     * 处理
     *
     * @param file          文件
     * @param interfaceName 接口名
     * @param methodName    方法名
     * @return 处理结果
     */
    public final List<MethodInfo> handle(MultipartFile file, final String interfaceName, final String methodName) {
        StopWatch stopwatch = new StopWatch("Handle " + interfaceName + " " + methodName);

        List<MethodInfo> result = new ArrayList<>();

        String tempFileName = UUID.randomUUID().toString();
        File tempDirectory = new File(System.getProperty("java.io.tmpdir"));
        File targetFile = new File(tempDirectory + File.separator + tempFileName);

        try {
            String filenameExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            if ("zip".equals(filenameExtension)) {
                zipFileHandle(interfaceName, methodName, stopwatch, result, file.getInputStream());
            }
            if (result.isEmpty()) {
                doHandle(file, interfaceName, methodName, stopwatch, result, targetFile);
            }
        } catch (IOException e) {
            log.warn("IO异常：InterfaceName:{} MethodName:{}", interfaceName, methodName, e);
        } catch (Exception e) {
            log.warn("处理失败：InterfaceName:{} MethodName:{}", interfaceName, methodName, e);
        } finally {
            if (targetFile.exists()) {
                stopwatch.start("Delete File");
                boolean delete = targetFile.delete();
                stopwatch.stop();

                if (!delete) {
                    log.error("删除失败：{}", targetFile);
                }
            }
            log.info("Total Cost：{} Seconds", stopwatch.getTotalTimeSeconds());
            log.info(stopwatch.prettyPrint());
        }
        return result;
    }

    /**
     * 针对文件使用每一个类加载器进行处理
     *
     * @param file          文件
     * @param interfaceName 接口
     * @param methodName    方法
     * @param stopwatch     {@link StopWatch}
     * @param result        处理结果
     * @param targetFile    临时文件
     * @throws Exception 处理时发生的异常信息
     */
    private void doHandle(MultipartFile file, String interfaceName, String methodName, StopWatch stopwatch, List<MethodInfo> result, File targetFile) throws Exception {
        stopwatch.start("File Transfer");
        try {
            file.transferTo(targetFile);
        } finally {
            stopwatch.stop();
        }

        jarFileHandle(interfaceName, methodName, stopwatch, result, targetFile);
        if (result.isEmpty()) {
            warFileHandle(interfaceName, methodName, stopwatch, result, targetFile);
        }
        if (result.isEmpty()) {
            zipFileHandle(interfaceName, methodName, stopwatch, result, targetFile);
        }
    }

    /**
     * WAR文件处理器
     *
     * @param interfaceName 接口名
     * @param methodName    方法名
     * @param stopwatch     {@link StopWatch}
     * @param result        处理结果
     * @param targetFile    临时文件
     * @throws Exception 处理时发生的异常信息
     */
    private void warFileHandle(String interfaceName, String methodName, StopWatch stopwatch, List<MethodInfo> result, File targetFile) throws Exception {
        stopwatch.start("SpringBootWarClassLoader LoadClass");
        try (JarFileArchive entries = new JarFileArchive(targetFile)) {
            final ClassLoader warClassLoader = new WarLauncher(entries).getClassLoader();
            tryLoadClass(warClassLoader, interfaceName).ifPresent(clazz -> resolve(warClassLoader, clazz, interfaceName, methodName, result));
        } finally {
            stopwatch.stop();
        }
    }

    /**
     * ZIP文件处理器
     *
     * @param interfaceName 接口名
     * @param methodName    方法名
     * @param stopwatch     {@link StopWatch}
     * @param result        处理结果
     * @param targetFile    临时文件
     * @throws IOException I/O流读取异常
     */
    private void zipFileHandle(String interfaceName, String methodName, StopWatch stopwatch, List<MethodInfo> result, File targetFile) throws IOException {
        stopwatch.start("ZipClassLoader LoadClass");
        try (FileInputStream inputStream = new FileInputStream(targetFile)) {
            final ZipClassLoader zipClassLoader = new ZipClassLoader(inputStream);
            tryLoadClass(zipClassLoader, interfaceName).ifPresent(clazz -> resolve(zipClassLoader, clazz, interfaceName, methodName, result));
        } finally {
            stopwatch.stop();
        }
    }

    /**
     * ZIP文件处理器
     *
     * @param interfaceName 接口名
     * @param methodName    方法名
     * @param stopwatch     {@link StopWatch}
     * @param result        处理结果
     * @param inputStream   输入流
     * @throws IOException I/O流读取异常
     */
    private void zipFileHandle(String interfaceName, String methodName, StopWatch stopwatch, List<MethodInfo> result, InputStream inputStream) throws IOException {
        stopwatch.start("ZipClassLoader LoadClass");
        try {
            final ZipClassLoader zipClassLoader = new ZipClassLoader(inputStream);
            tryLoadClass(zipClassLoader, interfaceName).ifPresent(clazz -> resolve(zipClassLoader, clazz, interfaceName, methodName, result));
        } finally {
            stopwatch.stop();
        }
    }

    /**
     * JAR文件处理器
     *
     * @param interfaceName 接口名
     * @param methodName    方法名
     * @param stopwatch     {@link StopWatch}
     * @param result        处理结果
     * @param targetFile    临时文件
     * @throws Exception 处理时发生的异常信息
     */
    private void jarFileHandle(String interfaceName, String methodName, StopWatch stopwatch, List<MethodInfo> result, File targetFile) throws Exception {
        stopwatch.start("SpringBootJarClassLoader LoadClass");
        try (JarFileArchive entries = new JarFileArchive(targetFile)) {
            final ClassLoader jarClassLoader = new JarLauncher(entries).getClassLoader();
            tryLoadClass(jarClassLoader, interfaceName).ifPresent(clazz -> resolve(jarClassLoader, clazz, interfaceName, methodName, result));
        } finally {
            stopwatch.stop();
        }
    }

    /**
     * 使用类加载器进行加载解析
     *
     * @param classLoader   类加载器
     * @param loadClass     加载的类
     * @param interfaceName 接口名
     * @param methodName    方法名
     * @param result        解析结果
     */
    private void resolve(final ClassLoader classLoader,
                         final Class<?> loadClass,
                         final String interfaceName,
                         final String methodName,
                         final List<MethodInfo> result) {
        try {
            Method[] methods = loadClass.getMethods();
            List<Method> methodList = Arrays.stream(methods)
                    .filter(m -> methodName.equals(m.getName()))
                    .collect(Collectors.toList());
            for (Method method : methodList) {
                Class<?>[] parameterTypes = method.getParameterTypes();

                List<Map<String, Object>> attribute = getAttribute(classLoader, method);

                MethodInfo methodInfo = new MethodInfo();
                methodInfo.setSignature(signatureString(method));
                methodInfo.setProperty(attribute);
                methodInfo.setParamClassName(Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.toList()));
                result.add(methodInfo);
            }
        } catch (ClassNotFoundException e) {
            log.warn("类没有找到：InterfaceName:{} MethodName:{}", interfaceName, methodName, e);
        } catch (IntrospectionException e) {
            log.warn("内省失败：InterfaceName:{} MethodName:{}", interfaceName, methodName, e);
        } catch (LinkageError e) {
            log.warn("链接错误：InterfaceName:{} MethodName:{}", interfaceName, methodName, e);
        } catch (Exception e) {
            log.warn("处理失败：InterfaceName:{} MethodName:{}", interfaceName, methodName, e);
        }
    }

    /**
     * 获取每个对象的属性信息
     *
     * @param classLoader 类加载器
     * @param method      对象所属的方法
     * @return 属性信息集合
     * @throws ClassNotFoundException 类加载器加载类失败
     * @throws IntrospectionException 内省失败
     */
    private List<Map<String, Object>> getAttribute(final ClassLoader classLoader, final Method method) throws ClassNotFoundException, IntrospectionException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            Type type = parameter.getParameterizedType();
            Map<String, Object> oneParamMap = getOneParamMap(classLoader, parameter.getType(), type, parameter.getName());
            list.add(oneParamMap);
        }
        return list;
    }

    /**
     * 获取一个参数的信息
     *
     * @param classLoader   类加载器
     * @param paramClass    参数类
     * @param genericType   泛型信息
     * @param parameterName 参数名
     * @return 参数的信息集合
     * @throws IntrospectionException 内省失败
     * @throws ClassNotFoundException 类没找到
     */
    private Map<String, Object> getOneParamMap(final ClassLoader classLoader,
                                               final Class<?> paramClass,
                                               final Type genericType,
                                               final String parameterName) throws IntrospectionException, ClassNotFoundException {
        Map<String, Object> result = new HashMap<>();

        if (isPlain(paramClass)) {
            // 文本
            result.put(parameterName, paramClass.getName());
        } else if (Enum.class.isAssignableFrom(paramClass)) {
            // 枚举
            Object[] enumConstants = paramClass.getEnumConstants();
            String enums = GSON_INSTANCE.toJson(enumConstants);
            String value = "enum|" + paramClass.getName() + "|" + enums;
            result.put(parameterName, value);
        } else if (paramClass.isArray()) {
            // 数组
            getOneParamMapForArray(classLoader, paramClass, parameterName, result);
        } else if (Collection.class.isAssignableFrom(paramClass)) {
            // 集合
            getOneParamMapForCollection(classLoader, genericType, parameterName, result);
        } else if (Map.class.isAssignableFrom(paramClass)) {
            // 字典
            getOneParamMapForMap(classLoader, genericType, parameterName, result);
        } else {
            // 剩下的都是对象？
            getOneParamMapForObject(classLoader, paramClass, genericType, parameterName, result);
        }
        return result;
    }

    /**
     * 获取对象的参数的信息
     *
     * @param classLoader   类加载器
     * @param paramClass    参数类
     * @param genericType   泛型信息
     * @param parameterName 参数名
     * @param result        结果
     * @throws IntrospectionException 内省失败
     * @throws ClassNotFoundException 类没找到
     */
    private void getOneParamMapForObject(ClassLoader classLoader, Class<?> paramClass, Type genericType, String parameterName, Map<String, Object> result) throws IntrospectionException, ClassNotFoundException {
        if (Serializable.class.isAssignableFrom(paramClass)) {
            BeanInfo beanInfo = Introspector.getBeanInfo(paramClass, Object.class);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            Map<String, Object> map = new HashMap<>();
            // 泛型信息：T->TYPE
            Map<String, Type> typeMap = new HashMap<>();
            // 对象上有泛型
            if (genericType instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
                int i = 0;
                for (TypeVariable<?> item : paramClass.getTypeParameters()) {
                    Type actualTypeArgument = actualTypeArguments[i++];
                    // 上限
                    if (actualTypeArgument instanceof TypeVariable) {
                        TypeVariable<?> typeVariable = (TypeVariable<?>) actualTypeArgument;
                        Type type = getBoundType(typeVariable);
                        if (null != type) {
                            actualTypeArgument = type;
                        }
                    }
                    if ("?".equals(actualTypeArgument.getTypeName())) {
                        typeMap.put(item.getName(), new Type() {
                            @Override
                            public String getTypeName() {
                                return Object.class.getTypeName();
                            }
                        });
                    } else {
                        typeMap.put(item.getName(), actualTypeArgument);
                    }
                }
            }
            for (PropertyDescriptor p : propertyDescriptors) {
                if (null == p.getWriteMethod()) {
                    continue;
                }
                // 每一个参数名
                String name = p.getName();
                Class<?> propertyType = p.getPropertyType();
                Type type = null;
                try {
                    type = paramClass.getDeclaredField(name).getGenericType();
                } catch (NoSuchFieldException | SecurityException e) {
                    log.warn("获取字段失败，可能是没有这个字段:{}", e.getMessage());
                }
                if (null != type) {
                    Type t = typeMap.get(type.getTypeName());
                    if (t instanceof ParameterizedType) {
                        type = t;
                        propertyType = tryLoadClass(classLoader, ((ParameterizedType) t).getRawType().getTypeName()).orElse(Object.class);
                    } else if (null != t) {
                        propertyType = tryLoadClass(classLoader, t.getTypeName()).orElse(Object.class);
                    }
                }
                getOneParamMap(classLoader, propertyType, type, name).forEach(map::put);
            }
            result.put(parameterName, map);
        } else {
            result.put(parameterName, Collections.emptyMap());
            log.warn("略过{}，没有实现序列化", paramClass.getName());
        }
    }

    /**
     * 获取MAP的参数的信息
     *
     * @param classLoader   类加载器
     * @param genericType   泛型信息
     * @param parameterName 参数名
     * @param result        结果
     * @throws IntrospectionException 内省失败
     * @throws ClassNotFoundException 类没找到
     */
    private void getOneParamMapForMap(ClassLoader classLoader, Type genericType, String parameterName, Map<String, Object> result) throws ClassNotFoundException, IntrospectionException {
        Map<String, Object> map = new HashMap<>();
        if (null != genericType) {
            if (genericType instanceof ParameterizedType) {
                // 有泛型信息
                ParameterizedType gType = (ParameterizedType) genericType;
                Type[] actualTypeArguments = gType.getActualTypeArguments();
                if (actualTypeArguments.length == 2) {
                    Type actualTypeArgument2 = actualTypeArguments[1];
                    if (actualTypeArgument2 instanceof ParameterizedType) {
                        ParameterizedType actualTypeArgument = (ParameterizedType) actualTypeArgument2;
                        Class<?> typeClass = classLoader.loadClass(actualTypeArgument.getRawType().getTypeName());
                        getOneParamMap(classLoader, typeClass, actualTypeArgument, parameterName).forEach(map::put);
                    } else {
                        Class<?> typeClass = classLoader.loadClass(actualTypeArgument2.getTypeName());
                        getOneParamMap(classLoader, typeClass, null, parameterName).forEach(map::put);
                    }
                }
            }
        }
        if (map.isEmpty()) {
            result.put(parameterName, Collections.emptyMap());
        } else {
            result.putAll(map);
        }
    }

    /**
     * 获取集合的参数的信息
     *
     * @param classLoader   类加载器
     * @param genericType   泛型信息
     * @param parameterName 参数名
     * @param result        结果
     * @throws IntrospectionException 内省失败
     * @throws ClassNotFoundException 类没找到
     */
    private void getOneParamMapForCollection(ClassLoader classLoader, Type genericType, String parameterName, Map<String, Object> result) throws ClassNotFoundException, IntrospectionException {
        List<Object> list = new ArrayList<>();
        if (null != genericType) {
            if (genericType instanceof ParameterizedType) {
                // 有泛型信息
                ParameterizedType gType = (ParameterizedType) genericType;
                Type[] actualTypeArguments = gType.getActualTypeArguments();
                if (actualTypeArguments.length == 1) {
                    Type actualTypeArgument1 = actualTypeArguments[0];
                    // 泛型中还有泛型
                    if (actualTypeArgument1 instanceof ParameterizedType) {
                        ParameterizedType actualTypeArgument = (ParameterizedType) actualTypeArgument1;

                        Class<?> typeClass = classLoader.loadClass(actualTypeArgument.getRawType().getTypeName());
                        Collection<Object> values = getOneParamMap(classLoader, typeClass, actualTypeArgument, parameterName).values();
                        list.addAll(values);
                    } else if (actualTypeArgument1 instanceof TypeVariable) {
                        TypeVariable<?> typeVariable = (TypeVariable<?>) actualTypeArgument1;
                        Type actualTypeArgument = getBoundType(typeVariable);
                        if (null != actualTypeArgument) {
                            Class<?> typeClass = classLoader.loadClass(actualTypeArgument.getTypeName());
                            Collection<Object> values = getOneParamMap(classLoader, typeClass, actualTypeArgument, parameterName).values();
                            list.addAll(values);
                        } else {
                            list.addAll(Collections.emptyList());
                        }
                    } else {
                        Class<?> typeClass = classLoader.loadClass(actualTypeArgument1.getTypeName());
                        Collection<Object> values = getOneParamMap(classLoader, typeClass, null, parameterName).values();
                        list.addAll(values);
                    }
                }
            }
        }
        result.put(parameterName, list);
    }

    /**
     * 获取数组的参数的信息
     *
     * @param classLoader   类加载器
     * @param paramClass    参数类
     * @param parameterName 参数名
     * @param result        结果
     * @throws IntrospectionException 内省失败
     * @throws ClassNotFoundException 类没找到
     */
    private void getOneParamMapForArray(ClassLoader classLoader, Class<?> paramClass, String parameterName, Map<String, Object> result) throws IntrospectionException, ClassNotFoundException {
        Class<?> arrayTypeClass = paramClass.getComponentType();
        Collection<Object> values = getOneParamMap(classLoader, arrayTypeClass, null, parameterName).values();
        result.put(parameterName, values);
    }

    /**
     * 获取泛型的上限
     *
     * @param typeVariable 类型
     * @return 上限
     */
    private Type getBoundType(TypeVariable<?> typeVariable) {
        Type actualTypeArgument = null;
        // 上限可能有多个：
        // <A extends String & Serializable & AutoCloseable> void test(Test<A> a)
        Type[] bounds = typeVariable.getBounds();
        for (Type itemBound : bounds) {
            if (itemBound instanceof Class) {
                if (!((Class<?>) itemBound).isInterface()) {
                    actualTypeArgument = itemBound;
                    break;
                }
            } else if (itemBound instanceof ParameterizedType) {
                actualTypeArgument = itemBound;
                break;
            }
        }
        return actualTypeArgument;
    }

    /**
     * 类是文本类型吗
     *
     * @param propertyType 类
     * @return 是返回<code>true</code>
     */
    private boolean isPlain(Class<?> propertyType) {
        return isPrimitiveOrWrap(propertyType) ||
                CharSequence.class.isAssignableFrom(propertyType) ||
                Date.class.isAssignableFrom(propertyType) ||
                java.sql.Date.class.isAssignableFrom(propertyType) ||
                java.sql.Timestamp.class.isAssignableFrom(propertyType) ||
                java.sql.Time.class.isAssignableFrom(propertyType) ||
                LocalDate.class.isAssignableFrom(propertyType) ||
                LocalTime.class.isAssignableFrom(propertyType) ||
                LocalDateTime.class.isAssignableFrom(propertyType);
    }
}
