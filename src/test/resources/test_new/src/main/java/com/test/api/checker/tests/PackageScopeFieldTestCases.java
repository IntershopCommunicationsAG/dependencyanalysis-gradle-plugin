package com.test.api.checker.tests;

class PackageScopeFieldTestCases {
    // change of type
    public Boolean testChangeOfTypePublic;
    protected Boolean testChangeOfTypeProtected;
    private Boolean testChangeOfTypePrivate;
    
    // Change of scope
    protected String testChangeOfScopeFromPublicToProtected;
    private String testChangeOfScopeFromPublicToPrivate;
    public String testChangeOfScopeFromProtectedToPublic;
    private String testChangeOfScopeFromProtectedToPrivate;
    protected String testChangeOfScopeFromPrivateToProtected;
    public String testChangeOfScopeFromPrivateToPublic;
    
    // Static
    public static String testPublicChangeToStatic;
    public String testPublicChangeFromStatic;
    protected static String testProtectedChangeToStatic;
    protected String testProtectedChangeFromStatic;
    private static String testPrivateChangeToStatic;
    private String testPrivateChangeFromStatic;
    
    // Final
    public final String publicFinalToNoFinal = "";
    public String publicNotfinalToFinal = "";
    protected final String protectedFinalToNoFinal = "";
    protected String protectedNotfinalToFinal = "";
    private final String privateFinalToNoFinal = "";
    private String privateNotfinalToFinal = "";

    
}
