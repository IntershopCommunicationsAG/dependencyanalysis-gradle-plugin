package com.test.api.checker.tests.exceptions;

public class CheckMethodExceptionInheritance {
    
    public void publicInheritedException() throws MyNewException {
        
    }

    protected void protectedInheritedException() throws MyNewException {
        
    }

    private void privatedInheritedException() throws MyNewException {
        
    }
    
    public void publicInheritedExceptionAndAdditional() throws MyNewException, MyAdditionalException {
        
    }

    protected void protectedInheritedExceptionAndAdditional() throws MyNewException, MyAdditionalException {
        
    }

    private void privatedInheritedExceptionAndAdditional() throws MyNewException, MyAdditionalException {
        
    }
    
}
