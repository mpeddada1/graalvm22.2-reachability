package com.anotherpackage;

import java.lang.reflect.Method;

public abstract class GeneratedMessage {

    protected abstract GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable();

    @SuppressWarnings("unchecked")
    private static Method getMethodOrDie(
            final Class clazz, final String name, final Class... params) {
        System.out.println("GET METHOD OR DIE");
        try {
            return clazz.getMethod(name, params);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    "Generated message class \"" + clazz.getName() + "\" missing method \"" + name + "\".",
                    e);
        }
    }

    public static class FieldAccessorTable {
        public FieldAccessorTable ensureFieldAccessorsInitialized(Class<? extends GeneratedMessage> messageClass, String camelCaseName){
            System.out.println("ENSURE FIELD ACCESSORS INITIALLIZED");
            new SingularFieldAccessor(messageClass, camelCaseName);
            return this;
        }
    }

    public static class SingularFieldAccessor {

        public SingularFieldAccessor(Class<? extends GeneratedMessage> messageClass, String camelCaseName) {
            System.out.println("SINGULAR FIELD ACCESSOR");
            getMethodOrDie(messageClass,camelCaseName);
        }
    }
}
