package com.wjz.spring.rmi;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.remoting.support.RemoteInvocation;

import java.lang.reflect.InvocationTargetException;

public class RmiRemoteInvocation extends RemoteInvocation {

    public RmiRemoteInvocation(MethodInvocation methodInvocation) {
        super(methodInvocation);
    }

    @Override
    public Object invoke(Object targetObject) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        System.out.println("before invoke");
        return super.invoke(targetObject);
    }
}
