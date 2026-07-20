package myframework.utils;
import java.util.HashMap;
import java.util.Map;

public class ApplicationContext {

    private static final Map<Class<?>, Object> beans =new HashMap<>();

    public static void addBean(Class<?> clazz,Object instance) {
        beans.put(clazz, instance);
    }

    public static Object getBean(Class<?> clazz) {
        return beans.get(clazz);
    }

}