package chy.spring.context;

import chy.spring.annotation.aop.*;
import chy.spring.aop.AopConfig;
import chy.spring.aop.Aspect;
import chy.spring.bean.BeanDefinition;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public   class DefaultListableBeanFactory extends AbstractChyApplicationContext {

    protected Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String,BeanDefinition>();

    //aop 注解解析后的缓存, key 为切入点的正则,value 为 对应切入点的方法
    protected List<Aspect> aspectMap = new ArrayList<>();
    //bean是列的单列 缓存
    private Map<String,Object> beanCache = new ConcurrentHashMap<>();



    @Override
    protected void refreshBeanFactory() {

    }

    @Override
    public Object getBean(String name) {
        return null;
    }

    @Override
    public <T> T getBean( Class<T> requiredType) {
        return null;
    }

    @Override
    public boolean isSingleton(String name) {
        return false;
    }

    @Override
    public boolean isPrototype(String name) {
        return false;
    }


    protected void doAopRegisty()  {
        for (String key : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(key);
            Class<?> aClass = null;
            try {
                aClass = Class.forName(beanDefinition.getBeanClassName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            ChyAspect chyAspect = aClass.getAnnotation(ChyAspect.class);
            if(chyAspect == null ){continue;}
            AopMethodHandle(aClass,beanDefinition);
        }


    }


    private void AopMethodHandle(Class aClass, BeanDefinition beanDefinition){
        Method[] methods = aClass.getMethods();
       // Aspect result = new Aspect(instantionBean(beanDefinition));

        Map<String , Aspect> MethodNameTemp = new HashMap<>();



        for (Method method : methods) {
            ChyPointcut pointcut = method.getAnnotation(ChyPointcut.class);
            if(pointcut != null){
                String methodName = method.getName()+"()";
                Aspect aspect = getAspect(MethodNameTemp, methodName);
                aspect.setMatch(pointcut.value());
                continue;
            }

            ChyBefore before = method.getAnnotation(ChyBefore.class);
            if(before != null){
                String value = before.value();
                Aspect aspect = getAspect(MethodNameTemp, value);
                aspect.setBefore(method);
                continue;
            }

            ChyAround around = method.getAnnotation(ChyAround.class);
            if(around != null){
                String value = around.value();
                Aspect aspect = getAspect(MethodNameTemp, value);
                aspect.setAround(method);
                continue;
            }

            ChyAfter after = method.getAnnotation(ChyAfter.class);
            if(after != null){
                String value = after.value();
                Aspect aspect = getAspect(MethodNameTemp, value);
                aspect.setAfter(method);
                continue;
            }

        }

        Object o = instantionBean(beanDefinition);

        for (Aspect aspect : MethodNameTemp.values()) {
            aspect.setExecTarget(o);
            aspectMap.add(aspect);
        }


    }

    private Aspect getAspect(Map<String,Aspect> map,String key){
        Aspect aspect = map.get(key);
        if(aspect == null){
            aspect = new Aspect();
            map.put(key,aspect);
        }
        return aspect;
    }

    private String executionHandle(String v){
        Pattern compile = Pattern.compile("execution\\((.*)\\)");
        Matcher matcher = compile.matcher(v);
        if(matcher.find()){
           return matcher.group(1);
        }else{
            return null;
        }

    }


    //传一个BeanDefinition，就返回一个实例Bean
    protected Object instantionBean(BeanDefinition beanDefinition){
        String beanClassName = beanDefinition.getBeanClassName();
        Object bean = beanCache.get(beanClassName);
        if(bean != null){
            return bean;
        }

        try {
            Class<?> aClass = Class.forName(beanClassName);
            bean = aClass.newInstance();
            this.beanCache.put(beanClassName,bean);
            return bean;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }


}
