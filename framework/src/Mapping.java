package myframework.utils;

import java.lang.reflect.Method;

public class Mapping{
    
    String packageName;
    String methodeName;
    Class<?> controllerClass;
    Method methodInstance;

    
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getMethodeName() {
        return methodeName;
    }

    public void setMethodeName(String methodeName) {
        this.methodeName = methodeName;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public Method getMethodInstance() {
        return methodInstance;
    }

    public void setMethodInstance(Method methodInstance) {
        this.methodInstance = methodInstance;
    }


}