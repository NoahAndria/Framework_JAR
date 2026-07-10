package myframework.utils;

import java.util.HashMap;
import java.util.Map;

public class ModelView {
    private String view;
    private Map<String, Object> attributes = new HashMap<>();

    public ModelView(String name) {
        this.view = name;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String name, Object object) {
        attributes.put(name, object);
    }
}