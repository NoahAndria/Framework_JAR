package myframework.utils;

import java.nio.file.*;
import java.io.*;
import java.util.ArrayList;

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

public static List<Mapping> getMappedUrls(List<String> controllers){

    List<Mapping> mappings = new ArrayList<>();

    try {

    for(int i = 0 ; i < controllers.size(); i++){
        Class<?> controllerClass = Class.forName(controllers.get(i));
        Method[] methods = controllerClass.getDeclaredMethods();
        for(int j = 0 ; j < methods.length; j++){
            Method method = methods[j];
            if(method.isAnnotationPresent(UrlMapping.class)){

                UrlMapping mapping = method.getAnnotation(UrlMapping.class);

                String url = mapping.name();

                Mapping m = new Mapping();
                m.setUrl(url);
                m.setPackageName(controllers.get(i));
                m.setMethodeName(method.getName());

                mappings.add(m);
            }
        }
    }

    } catch (Exception e) {
        System.err.println(e.getCause());
    }

    return mappings;
}


}