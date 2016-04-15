package com.test.api.checker.tests;

public class CheckMethodVariableArity {

	public void publicArrayToArity(String ... arg) { }
	protected void protectedArrayToArity(String ... arg) { }
	private void privateArrayToArity(String ... arg) { }
	
	public void publicArityToArray(String[] arg) { }
	protected void protectedArityToArray(String[] arg) { }
	private void privateArityToArray(String[] arg) { }
}
