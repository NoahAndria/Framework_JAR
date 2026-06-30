package controllers;

import myframework.utils.Utils;
import myframework.utils.UrlMethod;
import myframework.utils.Mapping;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import annotations.Controller;
import java.nio.file.*;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

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
          System.out.println("FrontControllerServlet: forwarding to /pageAcceuil.jsp");
          
          if(url.equals("/"))  req.setAttribute("mappings", mappings);
         
          req.setAttribute("method", method);
          req.setAttribute("mapping", m);
          req.setAttribute("url", url);
          if(m != null){
                m.getMethodInstance().setAccessible(true);
                try {
                    m.getMethodInstance().invoke(m.getControllerClass().getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    throw new ServletException(e);
                }
          }
          RequestDispatcher dispat = req.getRequestDispatcher("/WEB-INF/views/pageAcceuil.jsp");
          dispat.forward(req, res);
    }

public void init() throws ServletException {
    try {
        classNamesString = new ArrayList<>();
        String classesPath = getServletContext().getRealPath("/WEB-INF/classes");
        Path root = Paths.get(classesPath);

        List<String> classNames = Utils.findClasses(root);
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        for (String className : classNames) {
            Class<?> clazz = Class.forName(className, false, loader);
            if (clazz.isAnnotationPresent(Controller.class)) {
                System.out.println("Controller: " + clazz.getName());
                classNamesString.add(clazz.getName());
            }
        }

        mappings = Utils.getMappedUrls(classNamesString);

    } catch (Exception e) {
        throw new ServletException(e);
    }
}

public Mapping getMappingByUrl(String url, String method) {
    for (Map.Entry<UrlMethod, Mapping> mapping : mappings.entrySet()) {
        UrlMethod urlMethod = mapping.getKey();
        if (urlMethod.getUrl().equals(url) && urlMethod.getMethod().equalsIgnoreCase(method)) {
            return mapping.getValue();
        }
    }
    return null;
}
}