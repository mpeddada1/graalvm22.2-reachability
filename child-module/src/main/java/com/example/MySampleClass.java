package com.example;

public class MySampleClass extends com.anotherpackage.GeneratedMessage {

    public java.lang.String getName() {
        return "MySampleClassName";
    }


    @java.lang.Override
    protected A invokeAccessor() {
        A a = new A();
        return a.initializeMethodAccessor(com.example.MySampleClass.class,"getName");
    }
}
