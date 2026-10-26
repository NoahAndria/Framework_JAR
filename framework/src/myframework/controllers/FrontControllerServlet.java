package myframework.controllers;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import myframework.annotations.Auth;
import myframework.annotations.RestAPI;
import myframework.utils.Mapping;
import myframework.utils.ModelView;
import myframework.utils.Session;
import myframework.utils.UrlMethod;
import myframework.utils.Utils;

@MultipartConfig
public class FrontControllerServlet extends HttpServlet{

        List<String> classNamesString;
        Map<UrlMethod, Mapping> mappings;
        Map<Class<?>, Object> controllerInstances = new HashMap<>();
        String prefix;
        String suffix;
        String authString;

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
                        Object[] args = Utils.constructArguments(methodInstance, req);

                        Object controllerInstance = getControllerInstance(m.getControllerClass());
                        Object retour = methodInstance.invoke(controllerInstance, args);


                        if(methodInstance.isAnnotationPresent(Auth.class)){
                            Session session =  (Session) req.getSession().getAttribute("session");

                            if(session == null){
                                throw new RuntimeException("Session object is null. Please ensure that the session is properly initialized.");
                            }
               
                            Auth auth = methodInstance.getAnnotation(Auth.class);
                            String[] roles = auth.roles();

                            if(roles != null && roles.length > 0){
                                for(int i = 0; i < roles.length; i++){
                                    String role = roles[i];
                                    String sessionName = req.getServletContext().getInitParameter(role);
                                    
                                    if(session.get(sessionName) == null){
                                        System.out.println("Role  " + role +" not authenticated. Redirecting to login page.");
                                        res.sendRedirect(req.getContextPath() + "/login");
                                        return;
                                    }
                                }
                            }
                            else if(session.get(authString) == null){
                                System.out.println("User is not authenticated. Redirecting to login page.");
                                res.sendRedirect(req.getContextPath() + "/login");
                                return;
                            }
                            System.out.println("User is authenticated. Proceeding with the request.");
                        }
                        
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

         prefix = getServletContext().getInitParameter("prefix");
         suffix = getServletContext().getInitParameter("suffix");
         authString = getServletContext().getInitParameter("auth");

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

    public Object getControllerInstance(Class<?> controllerClass) throws Exception {
        if (!controllerInstances.containsKey(controllerClass)) {
            Object instance = controllerClass.getDeclaredConstructor().newInstance();
            controllerInstances.put(controllerClass, instance);
        }
        return controllerInstances.get(controllerClass);
    }


}