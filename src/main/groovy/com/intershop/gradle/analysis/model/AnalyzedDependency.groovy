package com.intershop.gradle.analysis.model

interface AnalyzedDependency {

    String getIdentifier()
    String getName()

    Set<String> getDuplicatedClasses()
    Set<String> getExcludedDuplicatedClasses()
    Set<AnalyzedDependency> getDuplicatedArtifacts()
    Set<AnalyzedDependency> getExcludedDuplicatedArtifacts()

    Set<String> getContainedClasses()
    Set<String> getUsedClasses()

    void setIgnoreForAnalysis(boolean b)

    void setDuplicatedArtifacts(Set<AnalyzedDependency> dependencies)

    boolean getIgnoreForAnalysis()

    boolean getFirstLevel()
}
