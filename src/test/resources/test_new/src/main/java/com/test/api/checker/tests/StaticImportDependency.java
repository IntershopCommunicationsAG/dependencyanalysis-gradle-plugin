package com.test.api.checker.tests;

import com.netflix.servo.publish.atlas.UpdateRequest;
import org.apache.commons.logging.impl.SimpleLog;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class StaticImportDependency {


    public void setClassReference(Class clazz) {
        clazz.getClass();
    }

    /**
     * Test assert
     */
    public void assertTest(String str1, String str2) {
        assertEquals(str1, str2);
    }

    /**
     * Print hello in log.
     *
     * @param name
     */
    public void sayHello(String name) {
        LoggerFactory.getLogger(StaticImportDependency.class).info("Hi, {}", name);
        LoggerFactory.getLogger(StaticImportDependency.class).info("Welcome to the HelloWorld example of SLF4J");

        System.out.print(SimpleLog.LOG_LEVEL_ALL);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        StaticImportDependency slf4jHello = new StaticImportDependency();
        slf4jHello.sayHello("srccodes.com");
        slf4jHello.assertTest("bla", "bla");
        slf4jHello.setClassReference(UpdateRequest.class);
    }
}
