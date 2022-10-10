package com.example;

import org.junit.Test;

public class MySampleTest {

    MySampleClass mySample = new MySampleClass();

    @Test
    public void testSample(){
        // invokeAccessor() calls initializeMethodAccessor() which then calls MethodAccessor.retrieveMethod() that
        // retrieves "MySampleClass.getName()" reflectively.
        mySample.invokeAccessor();
    }
}
