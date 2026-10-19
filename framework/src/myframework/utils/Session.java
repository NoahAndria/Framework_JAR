package myframework.utils;

import jakarta.servlet.http.HttpSession;

public class Session {

    private final HttpSession httpSession;

    public Session(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    public void set(String name, Object value) {
        httpSession.setAttribute(name, value);
    }

    public Object get(String name) {
        return httpSession.getAttribute(name);
    }
}