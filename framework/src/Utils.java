package myframework.utils;

import java.nio.file.*;
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.lang.annotation.Annotation;
import java.util.List;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.lang.reflect.Method;

import annotations.UrlMapping;

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


}