package chy.spring.core;

public interface BeanFactory {

    Object getBean(String name);

    <T> T getBean(Class<T> requiredType);

    boolean isSingleton(String name);

    boolean isPrototype(String name);

}
