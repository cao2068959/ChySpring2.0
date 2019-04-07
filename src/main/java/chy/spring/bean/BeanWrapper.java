package chy.spring.bean;

import chy.spring.aop.AopConfig;
import chy.spring.aop.AopProxy;

public class BeanWrapper {

    //包装对象
    private Object wrapperInstance;
    //原始对象
    private Object originalInstance;

    private AopConfig aopConfig;

    public AopConfig getAopConfig() {
        return aopConfig;
    }

    public void setAopConfig(AopConfig aopConfig) {
        this.aopConfig = aopConfig;
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public void setWrapperInstance(AopConfig aopConfig,Object originalInstance) {
        this.aopConfig = aopConfig;
        this.originalInstance = originalInstance;

        AopProxy aopProxy = new AopProxy(aopConfig);
        Object instance = aopProxy.getInstance(originalInstance);
        wrapperInstance = instance;

    }

    public Object getOriginalInstance() {
        return originalInstance;
    }


    public Object getInstance(){
        return wrapperInstance;
    }
}
