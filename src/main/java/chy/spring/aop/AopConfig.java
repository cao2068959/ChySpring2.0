package chy.spring.aop;

import chy.spring.annotation.aop.ChyBefore;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//在一个类中有对应的一个 config
public class AopConfig {
    //
    private Map<Method,List<Aspect>> methodMapping = new HashMap<>();



    public void put(Method key, Aspect value){
        List<Aspect> list = methodMapping.get(key);
        if(list == null){
            list = new ArrayList<>();
            methodMapping.put(key,list);
        }
        list.add(value);
    }

    public List<Aspect> get(Method key){
        return methodMapping.get(key);
    }

    public boolean contains(Method key){
        return this.methodMapping.containsKey(key);
    }



}
