package com.stu.lock.loader;

import java.util.*;

/**
 * SPI方式加载
 *
 * @author Qi.qingshan
 * @date 2020/5/9
 */
public final class NewInstanceServiceLoader {

    private NewInstanceServiceLoader() {
    }

    private static final Map<Class, Collection<Class<?>>> SERVICE_MAP = new HashMap<>();

    public static <T> void register(Class<T> service) {
        for (T each : ServiceLoader.load(service)) {
            registerServiceClass(service, each);
        }
    }

    public static <T> void registerServiceClass(final Class<T> service, T instance) {
        Collection<Class<?>> servieClasses = SERVICE_MAP.get(service);
        if (null == servieClasses) {
            servieClasses = new LinkedList<>();
        }
        servieClasses.add(instance.getClass());
        SERVICE_MAP.put(service, servieClasses);
    }

    public static <T> Collection<T> newServiceInstance(final Class<T> service) {
        Collection<T> result = new LinkedList<>();
        Collection<Class<?>> serviceClasses = SERVICE_MAP.get(service);
        if (null == serviceClasses) {
            return result;
        }
        for (Class<?> each : serviceClasses) {
            try {
                result.add((T) each.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
