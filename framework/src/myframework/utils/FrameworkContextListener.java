package myframework.utils;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import myframework.annotations.Controller;
import myframework.annotations.Repository;
import myframework.annotations.Service;

@WebListener
public class FrameworkContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {

            List<String> classNamesString = new ArrayList<>();
            ApplicationContext applicationContext = new ApplicationContext();

            String classesPath =
                    sce.getServletContext().getRealPath("/WEB-INF/classes");

            Path root = Paths.get(classesPath);

            List<String> classNames = Utils.findClasses(root);

            ClassLoader loader =
                    Thread.currentThread().getContextClassLoader();

            for (String className : classNames) {

                Class<?> clazz =
                        Class.forName(className, false, loader);

                if (clazz.isAnnotationPresent(Controller.class)) {
                    classNamesString.add(clazz.getName());
                }
                if(clazz.isAnnotationPresent(Service.class) ||
                   clazz.isAnnotationPresent(Repository.class)) {
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    applicationContext.addBean(clazz, instance);
                }
            }

            Map<UrlMethod, Mapping> mappings = Utils.getMappedUrls(classNamesString);

            // Store them globally
            ServletContext context = sce.getServletContext();

            context.setAttribute("controllersName", classNamesString);
            context.setAttribute("mappings", mappings);
            context.setAttribute("applicationContext", applicationContext);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}