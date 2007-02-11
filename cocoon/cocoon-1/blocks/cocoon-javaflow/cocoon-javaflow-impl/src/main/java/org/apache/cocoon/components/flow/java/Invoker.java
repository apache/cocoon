package org.apache.cocoon.components.flow.java;

import java.io.Serializable;
import java.lang.reflect.Method;

public final class Invoker implements Runnable, Serializable {
    private final Method method;

    public Invoker(Method method) throws IllegalAccessException, InstantiationException {
        this.method = method;
    }

    public void run() {
        try {
            Object o = method.getDeclaringClass().newInstance();
            method.invoke(o, new Object[0]);
        } catch (Throwable e) {
            // FIXME
            e.printStackTrace();
        }
    }
}