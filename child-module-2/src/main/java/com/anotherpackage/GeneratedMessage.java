package com.anotherpackage;

import java.lang.reflect.Method;

public abstract class GeneratedMessage {

    protected abstract A invokeAccessor();

    public static class A {
        public A initializeMethodAccessor(Class<? extends GeneratedMessage> messageClass, String camelCaseName){
            MethodAccessor methodAccessor = new MethodAccessor(messageClass, camelCaseName);
            return this;
        }
    }

    public static class MethodAccessor {

        public MethodAccessor(Class<? extends GeneratedMessage> messageClass, String camelCaseName) {
            // The constructor of this class retrieves another method reflectively
            retrieveMethod(messageClass,camelCaseName);
        }
    }

    @SuppressWarnings("unchecked")
    private static Method retrieveMethod(
            final Class clazz, final String name, final Class... params) {
        try {
            return clazz.getMethod(name, params);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    "Generated message class \"" + clazz.getName() + "\" missing method \"" + name + "\".",
                    e);
        }
    }
}
