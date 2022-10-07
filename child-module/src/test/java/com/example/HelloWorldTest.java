package com.example;

import org.junit.Test;

public class HelloWorldTest {

    MySampleClass mySample = new MySampleClass();

    @Test
    public void testSample(){
        System.out.println("test in Hello World ");
        mySample.equals();
        mySample.internalGetFieldAccessorTable();
    }
}
