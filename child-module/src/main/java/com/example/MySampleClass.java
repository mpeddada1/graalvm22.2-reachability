package com.example;

public class MySampleClass extends com.anotherpackage.GeneratedMessage implements MySampleClassInterface{

    public boolean equals() {
        System.out.println("Calling equals in " + getName());
        return true;
    }

    @Override
    public java.lang.String getName() {
        return "MySampleClassName";
    }


    @java.lang.Override
    protected com.anotherpackage.GeneratedMessage.FieldAccessorTable
    internalGetFieldAccessorTable() {
        System.out.println("INTERNAL GET FIELD ACCESSOR TABLE");
        FieldAccessorTable fieldAccessorTable = new FieldAccessorTable();
        return fieldAccessorTable.ensureFieldAccessorsInitialized(com.example.MySampleClass.class,"getName");
    }
}
