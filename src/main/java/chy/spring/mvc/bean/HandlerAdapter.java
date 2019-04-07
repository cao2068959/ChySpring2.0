package chy.spring.mvc.bean;

import chy.spring.annotation.ChyAutowired;
import chy.spring.annotation.ChyResponebody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class HandlerAdapter {

    private Map<String,Integer> paramMapping;

    public HandlerAdapter(Map<String, Integer> paramMapping) {
        this.paramMapping = paramMapping;
    }

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, HandleMapping handler) throws InvocationTargetException, IllegalAccessException {

        Method method = handler.getMethod();

        //请求中传过来的所有参数
        Map<String,String[]> parameterMap = request.getParameterMap();

        //要执行方法中需要传入的参数的类型
        Class<?>[] parameterTypes = method.getParameterTypes();

        Object[] param = new Object[parameterTypes.length];
        for (String paramKey : parameterMap.keySet()) {
            Integer index = paramMapping.get(paramKey);
            String[] values = parameterMap.get(paramKey);

            if(index == null){
                continue;
            }
            //把值转换成对应的类型,注入进去
            Class<?> parameterType = parameterTypes[index];
            param[index] = typeHandle(values,parameterType);
        }


        //注入request
        if(this.paramMapping.containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = this.paramMapping.get(HttpServletRequest.class.getName());
            param[reqIndex] = request;
        }

        //注入response
        if(this.paramMapping.containsKey(HttpServletResponse.class.getName())) {
            int respIndex = this.paramMapping.get(HttpServletResponse.class.getName());
            param[respIndex] = response;
        }

        //执行对应的方法
        Object result = method.invoke(handler.getController(), param);

        //如果打了ChyAutowired 则转对应的JSON
        if(method.getAnnotation(ChyResponebody.class)!=null){
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setType(ModelAndView.type_json);
            modelAndView.setData(result);
            return modelAndView;
        }


        Class<?> returnType = method.getReturnType();
        if(returnType == ModelAndView.class){
            return (ModelAndView)result;
        }else if(returnType == String.class){
           ModelAndView modelAndView = new ModelAndView();
           modelAndView.setViewName((String) result);
           return modelAndView;
        }else{
            return null;
        }
    }

    public Object typeHandle(String[] values,Class<?> type){
        if(values == null || values.length == 0){
            return null;
        }
        if(type == String.class){
            return values[0];
        }else if(type == Integer.class){
            return  Integer.valueOf(values[0]);
        }else if(type == int.class){
            return Integer.valueOf(values[0]).intValue();
        }else {
            return null;
        }
    }

}
