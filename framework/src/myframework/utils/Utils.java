package myframework.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
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

    public static Object[] constructArguments(Method method, HttpServletRequest req){
        Parameter[] parameters = method.getParameters();
        int parametersNumber = parameters.length;
        Object[] arguments = new Object[parametersNumber];
        for(int i = 0 ; i < parametersNumber; i++){
            Parameter p = parameters[i];

            Class<?> parameterType = p.getType();
            String pName = p.getName();

            if (List.class.isAssignableFrom(parameterType)) {
                arguments[i] = formListFromRequest(p.getParameterizedType(), pName, req );
            }

            else if(isCustomObject(parameterType, pName)){
                System.out.print(" Parameter name is a custom object: "+ pName );
                arguments[i] = formObjectFromRequest(parameterType, req); 
            }
            else {
                String value = req.getParameter(pName);
                System.out.print(" Parameter name is not a custom object : "+ pName );
                arguments[i] = convert(parameterType, value, pName);
            }
    

        }

        return arguments;

    }
    public static Object convert(Class<?> clazz, String value , String nom){
            
            if (value == null) {
                if (clazz.isPrimitive()) {
                    throw new IllegalArgumentException(
                        "Le paramètre '" + nom + "' est obligatoire."
                    );
                }

                return null;
            }

            if (clazz == String.class) {
                return value;
            }
            if (clazz == int.class ||clazz == Integer.class) {
                    return Integer.parseInt(value);
                }

            if (clazz == long.class || clazz == Long.class) {
                return Long.parseLong(value);
            }

            if (clazz == double.class || clazz == Double.class) {
                return Double.parseDouble(value);
            }

            throw new IllegalArgumentException(
                "Type non pris en charge : " + clazz.getName()
            );
    }

    public static String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

public static Object formObjectFromRequest(Class<?> clazz,HttpServletRequest req) {
    return formObjectFromRequest(clazz, req, "");
}

private static Object formObjectFromRequest(Class<?> clazz,HttpServletRequest req,String prefix ) {
    try {
        Object instance = clazz.getDeclaredConstructor().newInstance();

        for (Field field : clazz.getDeclaredFields()) {

            // champ non modifiable
            if (Modifier.isStatic(field.getModifiers())
                    || Modifier.isFinal(field.getModifiers())
                    || field.isSynthetic()) {
                continue;
            }
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();
            //nom, adresse.ville
            String parameterName = prefix + fieldName;

            Object value;
            
            if (List.class.isAssignableFrom(fieldType)) {

                value = formListFromRequest(
                    field.getGenericType(), parameterName, req
                );

            } 
            else if (isCustomObject(fieldType, fieldName)) {

                String prefixeParent = parameterName + ".";
                if (!hasParametersWithPrefix(req, prefixeParent)) {
                    continue;
                }
                value = formObjectFromRequest(fieldType,req,prefixeParent);
            } else {
                String valeurString = req.getParameter(parameterName);
                if (valeurString == null) {
                    continue;
                }
                value = convert(fieldType, valeurString, parameterName);
            }
            Method setter = clazz.getMethod("set" + capitalizeFirst(fieldName),fieldType);
            setter.invoke(instance, value);
        }

        return instance;

    } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Impossible de construire l'objet " + clazz.getName() + " avec le préfixe '" + prefix + "'",e);
    }
}

private static boolean hasParametersWithPrefix( HttpServletRequest req,String prefix) {
    for (String name : req.getParameterMap().keySet()) {
        if (name.startsWith(prefix)) {
            return true;
        }
    }
    return false;
}

private static List<Object> formListFromRequest(
        Type genericType, String name, HttpServletRequest req) {

    ParameterizedType listType = (ParameterizedType) genericType;
    Class<?> elementType = (Class<?>) listType.getActualTypeArguments()[0];

    List<Object> result = new ArrayList<>();

    if (isCustomObject(elementType, name)) {
        int index = 0;
        String prefix = name + "[" + index + "].";

        while (hasParametersWithPrefix(req, prefix)) {
            result.add(formObjectFromRequest(elementType, req, prefix));

            index++;
            prefix = name + "[" + index + "].";
        }
    } else {
        String[] values = req.getParameterValues(name);

        if (values != null) {
            for (String value : values) {
                result.add(convert(elementType, value, name));
            }
        }
    }

    return result;
}


}