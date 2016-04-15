package com.test.api.checker.tests;

public class CheckMethodAccess {

    public final void publicToFinal() {}
    protected final void protectedToFinal() {}
    private final void privateToFinal() {}

    public static void publicToStatic() {}
    protected static void protectedToStatic() {}
    private static void privateToStatic() {}
 
    public void publicFromStatic() {}
    protected void protectedFromStatic() {}
    private void privateFromStatic() {}

}
