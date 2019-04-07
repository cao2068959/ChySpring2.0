package chy.spring.mvc.bean;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {


    public static final String type_forward = "forward";
    public static final String type_json = "json";

    private String viewName;
    private String type = type_forward;
    private Map<String,Object> model = new HashMap<>();
    private Object Data;

    public Object getData() {
        return Data;
    }

    public void setData(Object data) {
        Data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }

    public void setModel(String key,Object value) {
        model.put(key,value);
    }

}
