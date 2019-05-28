package com.intershop.gradle.analysis.task

import com.intershop.gradle.analysis.model.AnalyzedDependency

class DependencyReporting {

    private final Set<AnalyzedDependency> used
    private final Set<AnalyzedDependency> unused
    private final Set<AnalyzedDependency> usedTransitive
    private final Set<AnalyzedDependency> duplicates
    private final Set<AnalyzedDependency> unusedTranstive
    private final Set<AnalyzedDependency> excludedDuplicates
    private final Set<AnalyzedDependency> analyzedArtifacts

    DependencyReporting(Set<AnalyzedDependency> used, Set<AnalyzedDependency> unused, Set<AnalyzedDependency> usedTransitive,
                        Set<AnalyzedDependency> unusedTranstive, Set<AnalyzedDependency> duplicates,
                        Set<AnalyzedDependency> excludedDuplicates, Set<AnalyzedDependency> analyzedArtifacts
    ) {

        this.excludedDuplicates = excludedDuplicates
        this.unusedTranstive = unusedTranstive
        this.duplicates = duplicates
        this.usedTransitive = usedTransitive
        this.unused = unused
        this.used = used
        this.analyzedArtifacts = analyzedArtifacts
    }
}
