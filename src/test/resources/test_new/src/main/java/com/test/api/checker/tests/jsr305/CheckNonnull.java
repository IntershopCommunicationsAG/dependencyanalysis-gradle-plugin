package com.test.api.checker.tests.jsr305;

import javax.annotation.Nullable;

public class CheckNonnull {

    public Object getNonnullToNothingPublicObject() {
        return null;
    }
    
    protected Object getNonnullToNothingProtectedObject() {
        return null;
    }

    private Object getNonnullToNothingPrivateObject() {
        return null;
    }

    @Nullable
    public Object getNonnullToNullablePublicObject() {
        return null;
    }
    
    @Nullable
    protected Object getNonnullToNullableProtectedObject() {
        return null;
    }

    @Nullable
    private Object getNonnullToNullablePrivateObject() {
        return null;
    }
}
