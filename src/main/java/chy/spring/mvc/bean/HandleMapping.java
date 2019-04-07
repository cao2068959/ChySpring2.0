package chy.spring.mvc.bean;

import java.lang.reflect.Method;

public class HandleMapping {
    private String patter;
    private Method method;
    private Object controller;

    public HandleMapping(String patter, Method method, Object controller) {
        this.patter = patter;
        this.method = method;
        this.controller = controller;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public String getPatter() {
        return patter;
    }

    public void setPatter(String patter) {
        this.patter = patter;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
