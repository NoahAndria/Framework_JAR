package myframework.controllers;

import myframework.utils.Utils;
import myframework.utils.UrlMethod;
import myframework.utils.ModelView;
import myframework.utils.Mapping;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import myframework.annotations.Controller;
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
        
          
          if(url.equals("/"))  req.setAttribute("mappings", mappings);
         
          req.setAttribute("method", method);
          req.setAttribute("mapping", m);
          req.setAttribute("url", url);
          if(m != null){
                m.getMethodInstance().setAccessible(true);
                try {
                    Object retour = m.getMethodInstance().invoke(m.getControllerClass().getDeclaredConstructor().newInstance());
                    String prefix = getServletContext().getInitParameter("prefix");
                    String suffix = getServletContext().getInitParameter("suffix");

                    if(retour instanceof ModelView){
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
            return mapping.getValue();
        }
    }
    return null;
}
}