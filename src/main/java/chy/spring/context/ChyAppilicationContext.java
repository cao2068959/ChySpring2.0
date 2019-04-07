package chy.spring.context;

import chy.spring.annotation.ChyAutowired;
import chy.spring.annotation.ChyComponent;
import chy.spring.annotation.ChyController;
import chy.spring.annotation.ChyService;
import chy.spring.aop.AopConfig;
import chy.spring.aop.Aspect;
import chy.spring.bean.BeanDefinition;
import chy.spring.bean.BeanWrapper;
import chy.spring.context.support.BeanDefinitionReader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChyAppilicationContext extends  DefaultListableBeanFactory{


    private String configLocation;
    private BeanDefinitionReader reader = null;



    //用来存储所有的被代理过的对象
    private Map<String, BeanWrapper> beanWrapperMap = new ConcurrentHashMap<String, BeanWrapper>();


    public ChyAppilicationContext(String configLocation) {
        this.configLocation = configLocation;
        refresh();

    }

    public void refresh()  {
        //定位配置文件
        reader = new BeanDefinitionReader(configLocation);
        //加载
        List<String> beanDefinitions = reader.loadBeanDefinitions();
        //注册
        doRegisty(beanDefinitions);
        //aop注解的注册
        doAopRegisty();
        //注入
        doAutowrited();

    }


    private void doRegisty(List<String> beanDefinitions) {
        for (String beanPath : beanDefinitions) {
            try {
                Class<?> aClass = Class.forName(beanPath);
                //如果扫描的是接口,不处理
                if(aClass.isInterface()){
                    continue;
                }

                String annotationValue = annotationHandle(aClass);
                //类上没有注解,放弃
                if(annotationValue == null){
                    continue;
                }

                BeanDefinition definition = reader.getDefinition(beanPath);

                //注册进容器中
                if("".equals(annotationValue)){
                    annotationValue = aClass.getName();
                }
                this.beanDefinitionMap.put(annotationValue,definition);
                //把接口对应的实现类也注册进容器中
                for (Class<?> anInterface : aClass.getInterfaces()) {
                    this.beanDefinitionMap.put(anInterface.getName(),definition);
                }


            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        }


    }


    //相应注解做对应处理
    private String annotationHandle(Class<?> aclass){
        ChyController chyController = aclass.getAnnotation(ChyController.class);
        if(chyController != null){
            return chyController.value();
        }

        ChyService chyService = aclass.getAnnotation(ChyService.class);
        if(chyService != null){
            return chyService.value();
        }

        ChyComponent chyComponent = aclass.getAnnotation(ChyComponent.class);
        if(chyComponent != null){
            return chyComponent.value();
        }

        return null;
    }


    private void doAutowrited() {

        //把所有类 先初始化一遍
        for(Map.Entry<String,BeanDefinition> beanDefinitionEntry : this.beanDefinitionMap.entrySet()){
            String beanName = beanDefinitionEntry.getKey();
            if(!beanDefinitionEntry.getValue().isLazyInit()){
                Object obj = getBean(beanName);
            }
        }

        for(Map.Entry<String,BeanWrapper> beanWrapperEntry : this.beanWrapperMap.entrySet()){

            populateBean(beanWrapperEntry.getKey(),beanWrapperEntry.getValue().getInstance());

        }



    }

    private void populateBean(String key, Object instance) {
        Class<?> aClass = instance.getClass().getSuperclass();
        for (Field declaredField : aClass.getDeclaredFields()) {
            ChyAutowired annotation = declaredField.getAnnotation(ChyAutowired.class);
            if(annotation == null){continue;}
            String autowriteValue = annotation.value();
            if("".equals(autowriteValue)){
                autowriteValue = declaredField.getType().getName();
            }
            BeanWrapper beanWrapper = this.beanWrapperMap.get(autowriteValue);
            declaredField.setAccessible(true);
            try {
                declaredField.set(instance,beanWrapper.getInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    protected void refreshBeanFactory() {

    }

    @Override
    public Object getBean(String name) {
        BeanWrapper cacheWrapper = this.beanWrapperMap.get(name);
        if(cacheWrapper != null){
            return cacheWrapper.getInstance();
        }

        BeanDefinition beanDefinition = this.beanDefinitionMap.get(name);
        if(beanDefinition == null){
            return null;
        }

        Object original  = instantionBean(beanDefinition);

        BeanWrapper beanWrapper = new BeanWrapper();
        beanWrapper.setWrapperInstance(getAopConfig(original),original);

        this.beanWrapperMap.put(name,beanWrapper);

        return this.beanWrapperMap.get(name).getInstance();

    }

    private AopConfig getAopConfig(Object bean){
        AopConfig aopConfig = new AopConfig();

        Class<?> beanClass = bean.getClass();
        for (Method declaredMethod : beanClass.getDeclaredMethods()) {
            String methodName = declaredMethod.toString();

            for (Aspect aspect : this.aspectMap) {
                if(methodName.matches(aspect.getMatch())){
                    aopConfig.put(declaredMethod,aspect);
                }
            }


        }
        return aopConfig;
    }




    @Override
    public <T> T getBean(Class<T> requiredType) {
        String name = requiredType.getName();
        return (T) getBean(name);
    }



    @Override
    public boolean isSingleton(String name) {
        return false;
    }

    @Override
    public boolean isPrototype(String name) {
        return false;
    }


    /**
     * 获取容器中所有对象的key
     * @return
     */
    public Set<String> getBeanDefinitionNames(){
       return this.beanWrapperMap.keySet();
    }

}
