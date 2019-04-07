package chy.spring.aop;


import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;

//cglib实现的动态代理
public class AopProxy implements MethodInterceptor {

    private Object target;
    private AopConfig aopConfig;

    public AopProxy(AopConfig aopConfig) {
        this.aopConfig = aopConfig;
    }

    public Object getInstance(Object target) {
        this.target = target;  //给业务对象赋值
        Enhancer enhancer = new Enhancer(); //创建加强器，用来创建动态代理类
        enhancer.setSuperclass(this.target.getClass());  //为加强器指定要代理的业务类（即：为下面生成的代理类指定父类）
        //设置回调：对于代理类上所有方法的调用，都会调用CallBack，而Callback则需要实现intercept()方法进行拦
        enhancer.setCallback(this);
        // 创建动态代理类对象并返回
        return enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

        List<Aspect> aspects = aopConfig.get(method);
        //没有任何的代理
        if(aspects == null ){
            return methodProxy.invokeSuper(o,objects);
        }

        //执行所有的before
        for (Aspect aspect : aspects) {
            Method before = aspect.getBefore();
            if(before == null){continue;}
            before.invoke(aspect.getExecTarget());
        }

        //执行所有的around,递归调用
        Object result = aroundHandle(aspects, 0, o, method, objects, methodProxy);

        //执行所有的after
        for (int i = aspects.size()-1; i >=0 ; i--) {
            Aspect aspect = aspects.get(i);
            Method after = aspect.getAfter();
            if(after == null){continue;}
            after.invoke(aspect.getExecTarget());
        }

        return result;
    }

    public Object aroundHandle(List<Aspect> aspects,int index,Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        //最后一层
        if(aspects.size() <= index){
            return methodProxy.invokeSuper(o,objects);
        }

        //寻找拥有around的方法
        Method around = null;
        Aspect aspect =null;
        while(aspects.size() > index && around == null ){
            aspect = aspects.get(index);
            around = aspect.getAround();
            index++;
        }

        final int findex = index;

        return around.invoke(aspect.getExecTarget(),new ProceedingJoinPoint(){
            @Override
            public Object proceed() throws Throwable {
                return aroundHandle(aspects,findex,o,method,objects,methodProxy);
            }
        });


    }

}
