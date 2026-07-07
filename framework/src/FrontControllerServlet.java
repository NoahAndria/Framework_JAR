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