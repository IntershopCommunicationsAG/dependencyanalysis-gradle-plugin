package com.test.api.checker.tests.annotations;

import javax.annotation.Nonnull;

public class CheckMethodAnnotation {

    @Nonnull
    public Object getNonnullToNothingPublicObject() {
        return null;
    }
    
    public Object getDeprecatedEntity() {
    	return null;
    }
    
    @Deprecated
    public Object getObjectEntity() {
    	return null;
    }
}
