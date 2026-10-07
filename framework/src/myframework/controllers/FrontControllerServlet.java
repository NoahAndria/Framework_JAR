package myframework.controllers;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import myframework.annotations.RestAPI;
import myframework.utils.Mapping;
import myframework.utils.ModelView;
import myframework.utils.UrlMethod;
import myframework.utils.Utils;

    public class FrontControllerServlet extends HttpServlet{

        List<String> classNamesString;
        Map<UrlMethod, Mapping> mappings;

        protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
            handleRequest(req, res);
        }
        protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
            handleRequest(req, res);
        }

        public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{

            String url = req.getRequestURI().substring(req.getContextPath().length());
            String method = req.getMethod();
            Mapping m = getMappingByUrl(url, method);
            
            
            if(url.equals("/"))  req.setAttribute("mappings", mappings);
            
            req.setAttribute("method", method);
            req.setAttribute("mapping", m);
            req.setAttribute("url", url);


            if(m != null){
                    Method methodInstance = m.getMethodInstance();
                    methodInstance.setAccessible(true);
                    try {
                        Object[] args = constructArguments(methodInstance, req);

                        Object retour = methodInstance.invoke(m.getControllerClass().getDeclaredConstructor().newInstance(), args);
                        String prefix = getServletContext().getInitParameter("prefix");
                        String suffix = getServletContext().getInitParameter("suffix");


                        if(methodInstance.isAnnotationPresent(RestAPI.class)){

                            res.setContentType("application/json");
                            ObjectMapper mapper = new ObjectMapper();
                            String json = mapper.writeValueAsString(retour);
                            res.getWriter().write(json);     
                        }
                        else if(retour instanceof ModelView){
                            ModelView mv = (ModelView) retour;
                            
                            String view = prefix + mv.getView() + suffix;
                            System.out.println("View that you are being redirected at my guy: " + view);

                            if (mv.getAttributes() != null) {
                                for (Map.Entry<String, Object> entry : mv.getAttributes().entrySet()) {
                                    req.setAttribute(entry.getKey(), entry.getValue());
                                }
                            }

                            RequestDispatcher dispat = req.getRequestDispatcher(view);
                            dispat.forward(req, res);
                        } else {
                            req.setAttribute("retour", retour);
                        }
                    } catch (Exception e) {
                        throw new ServletException(e);
                    }
            }

        }

    @Override
    public void init() throws ServletException {
        classNamesString = (List<String>) getServletContext().getAttribute("controllersName");
        mappings = (Map<UrlMethod, Mapping>) getServletContext().getAttribute("mappings");

        if (mappings == null) {
            throw new ServletException("Mappings were not initialized.");
        }
    }

    public Mapping getMappingByUrl(String url, String method) {
        for (Map.Entry<UrlMethod, Mapping> mapping : mappings.entrySet()) {
            UrlMethod urlMethod = mapping.getKey();
            if (urlMethod.getUrl().equals(url) && urlMethod.getMethod().equalsIgnoreCase(method)) {
                System.out.print("Mapping found :" + url + " " + "method");
                return mapping.getValue();
            }
        }
        return null;
    }

    public Object[] constructArguments(Method method, HttpServletRequest req){
        Parameter[] parameters = method.getParameters();
        int parametersNumber = parameters.length;
        Object[] arguments = new Object[parametersNumber];
        for(int i = 0 ; i < parametersNumber; i++){
            Parameter p = parameters[i];

            Class<?> parameterType = p.getType();
            String pName = p.getName();

            if(Utils.isCustomObject(parameterType, pName)){
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
    public Object convert(Class<?> clazz, String value , String nom){
            
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

    public Object formObjectFromRequest(Class<?> clazz, HttpServletRequest req){
        try {

        Object instance = clazz.getDeclaredConstructor().newInstance();
        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields){
            String fieldName = field.getName();
            String value = req.getParameter(fieldName);
            
            System.out.println("Objects Field name: " + fieldName + ", Value: " + value);
            if(value != null){
            
                Method m = clazz.getMethod("set" + capitalizeFirst(fieldName), field.getType());
                m.invoke(instance, convert(field.getType(), value, fieldName));

            }
        }
        return instance;
        } 
        catch (Exception e) {
            System.err.println(e.getCause());
        }
        return null;
    }

    }