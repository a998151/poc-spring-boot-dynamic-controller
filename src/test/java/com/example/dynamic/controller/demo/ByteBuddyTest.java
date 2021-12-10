package com.example.dynamic.controller.demo;

import com.example.dynamic.controller.demo.entity.Bar;
import com.example.dynamic.controller.demo.entity.Foo;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ByteBuddyTest {

    private static Logger log = LoggerFactory.getLogger(ByteBuddyTest.class);

    /**
     * 创建 Hello
     */
    @Test
    public void test(){
        DynamicType.Unloaded<Object> make = new ByteBuddy()
                .subclass(Object.class)
                .method(ElementMatchers.isToString())
                .intercept(FixedValue.value("Hello World ByteBuddy!"))
                .make();

        System.out.println("111");
    }

    @Test
    public void test_hello() throws InstantiationException, IllegalAccessException, IOException {
        DynamicType.Unloaded unloadedType = new ByteBuddy()
                .subclass(Object.class)
                .method(ElementMatchers.isToString())
                .intercept(FixedValue.value("Hello World ByteBuddy!"))
                .make();
        unloadedType.saveIn(new File("F:\\logs\\buddy"));
        Class<?> dynamicType = unloadedType.load(getClass().getClassLoader()).getLoaded();
        assertEquals(dynamicType.newInstance().toString(), "Hello World ByteBuddy!");
    }

    /**
     * 新建的对象，方法执行代理到已知的对象方法中去
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Test
    public void proxy() throws InstantiationException, IllegalAccessException {
        String r = new ByteBuddy()
                .subclass(Foo.class)
                .method(named("sayHelloFoo").and(isDeclaredBy(Foo.class)).and(returns(String.class)))
                .intercept(MethodDelegation.to(Bar.class))
                .make()
                .load(getClass().getClassLoader())
                .getLoaded()
                .newInstance()
                .sayHelloFoo();

        assertEquals(r, Bar.sayHelloBar());
    }


    /**
     * 自定义方法与字段
     */
    @Test
    public void test_method_field() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        Class<?> type = new ByteBuddy()
                .subclass(Object.class)
                .name("MyClassName")
                .defineMethod("custom", String.class, Modifier.PUBLIC)
                .intercept(MethodDelegation.to(Bar.class))
                .defineField("x", String.class, Modifier.PUBLIC)
                .make()
                .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();

        Method m = type.getDeclaredMethod("custom", null);
        assertEquals(m.invoke(type.newInstance()), Bar.sayHelloBar());
        assertNotNull(type.getDeclaredField("x"));
    }


    /**
     * 重定义一个已经存在的类
     */
    @Test
    public void test_redefine(){
        ByteBuddyAgent.install();
        new ByteBuddy()
                .redefine(Foo.class)
                .method(named("sayHelloFoo"))
                .intercept(FixedValue.value("Hello Foo Redefined"))
                .make()
                .load(Foo.class.getClassLoader() , ClassReloadingStrategy.fromInstalledAgent());

        Foo f = new Foo();
        log.info("f.sayHelloFoo() = {}" , f.sayHelloFoo());
    }
}
