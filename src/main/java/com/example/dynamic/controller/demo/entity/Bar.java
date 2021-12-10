package com.example.dynamic.controller.demo.entity;

import net.bytebuddy.implementation.bind.annotation.BindingPriority;

public class Bar {

    @BindingPriority(20)
    public static String sayHelloBar(){
        return "Hello in Bar!";
    }

    @BindingPriority(3)
    public static String sayBar(){
        return "bar";
    }
}
