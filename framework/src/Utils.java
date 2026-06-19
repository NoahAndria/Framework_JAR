package utils;

import java.nio.file.*;
import java.io.*;
import java.util.ArrayList;

import java.util.List;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

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
}