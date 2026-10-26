package myframework.utils;

import java.util.Map;
import java.util.HashMap;

public class Session {

    // private final HttpSession httpSession;
    Map<String, Object> attributes;

    public Session(){
        // this.httpSession = null;
        this.attributes  = new HashMap<String, Object>();
    }
    // public Session(HttpSession httpSession) {
    //     this.httpSession = httpSession;
    // }


    // public Session(HttpSession httpSession, Map<String, Object> attributes) {
    //     this.httpSession = httpSession;
    //     setAll(attributes);
    // }

    public void set(String name, Object value) {
        // httpSession.setAttribute(name, value);
        attributes.put(name, value);
    }

    public Object get(String name) {
        // return httpSession.getAttribute(name);
        return attributes.get(name);
    }
    // public void setAll(Map<String, Object> attributes) {
    //     for (Map.Entry<String, Object> entry : attributes.entrySet()) {
    //         httpSession.setAttribute(entry.getKey(), entry.getValue());
    //     }
    // }

}