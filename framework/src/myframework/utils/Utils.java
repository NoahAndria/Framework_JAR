package myframework.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import myframework.annotations.UrlMapping;

public class Utils {
    
public static List<String> findClasses(Path root) throws IOException {
    List<String> classNames = new ArrayList<>();

    Files.walk(root)
        .filter(p -> p.toString().endsWith(".class"))
        .forEach(p -> {
            Path relative = root.relativize(p);

            String className = relative.toString()
                    .replace(File.separatorChar, '.')
                    .replaceAll("\\.class$", "");

            classNames.add(className);
        });

    return classNames;
}

public static Map<UrlMethod, Mapping> getMappedUrls(List<String> controllers) throws Exception {

    Map<UrlMethod, Mapping> mappings = new HashMap<>();

    try {

    for(int i = 0 ; i < controllers.size(); i++){
        Class<?> controllerClass = Class.forName(controllers.get(i));
        Method[] methods = controllerClass.getDeclaredMethods();
        for(int j = 0 ; j < methods.length; j++){
            Method method = methods[j];
            if(method.isAnnotationPresent(UrlMapping.class)){

                UrlMapping mapping = method.getAnnotation(UrlMapping.class);

                String url = mapping.name();
                String methodString = mapping.method();

                UrlMethod um = new UrlMethod();
                um.setUrl(url);
                um.setMethod(methodString);

                Mapping m = new Mapping();
                if(mappings.containsKey(um))
                {
                    throw new ServletException("The url " +  url + " has multiple methods / controller handling it for " + methodString.toUpperCase());
                }
                m.setPackageName(controllers.get(i));
                m.setMethodeName(method.getName());
                m.setControllerClass(controllerClass);
                m.setMethodInstance(method);

                mappings.put(um, m);
            }
        }
    }

    } catch (Exception e) {
        System.err.println(e.getCause());
    }

    return mappings;
}


    public static ApplicationContext getContext(ServletContext context) {

        return (ApplicationContext) context.getAttribute("applicationContext");

    }

    public static boolean isCustomObject(Class<?> clazz, String nom){

        if (clazz == String.class) {
            return false;
        }
        if (clazz == int.class ||clazz == Integer.class) {
                return false;
            }

        if (clazz == long.class || clazz == Long.class) {
            return false;
        }

        if (clazz == double.class || clazz == Double.class) {
            return false;
        }

     return true;
}

}