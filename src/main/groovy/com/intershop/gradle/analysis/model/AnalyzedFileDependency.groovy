package com.intershop.gradle.analysis.model

class AnalyzedFileDependency extends AbstractAnalyzedDependency {

    String path

    AnalyzedFileDependency(File file, String configuration) {
        super(configuration)
        this.path = file.absolutePath
        this.firstLevel = true
        addFile(file)
    }

    @Override
    String getIdentifier() {
        return path
    }

    String getName() {
        return getIdentifier()
    }

}
