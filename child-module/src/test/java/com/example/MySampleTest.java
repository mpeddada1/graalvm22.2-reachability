package com.example;

import org.junit.Test;

public class MySampleTest {

    MySampleClass mySample = new MySampleClass();

    @Test
    public void testSample(){
        // invokeAccessor() calls initializeMethodAccessor() which then calls MethodAccessor.retrieveMethod() that
        // retrieves "MySampleClass.getName()" reflectively.
        System.out.println("**********VALUE of USE_NATIVE_IMAGE_JAVA_PLATFORM_MODULE_SYSTEM************");
        System.out.println(System.getenv("USE_NATIVE_IMAGE_JAVA_PLATFORM_MODULE_SYSTEM"));
        mySample.invokeAccessor();
    }
}
