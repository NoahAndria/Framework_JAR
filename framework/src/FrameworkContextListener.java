package myframework.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

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

@WebListener
public class FrameworkContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {

            List<String> classNamesString = new ArrayList<>();

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
            }

            Map<UrlMethod, Mapping> mappings =
                    Utils.getMappedUrls(classNamesString);

            // Store them globally
            ServletContext context = sce.getServletContext();

            context.setAttribute("controllersName", classNamesString);
            context.setAttribute("mappings", mappings);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}