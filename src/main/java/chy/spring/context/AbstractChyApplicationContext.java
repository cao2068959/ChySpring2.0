package chy.spring.context;

import chy.spring.core.BeanFactory;

public abstract class AbstractChyApplicationContext implements BeanFactory {

    //提供给子类重写
    protected void onRefresh(){
    }

    protected abstract void refreshBeanFactory();

}
