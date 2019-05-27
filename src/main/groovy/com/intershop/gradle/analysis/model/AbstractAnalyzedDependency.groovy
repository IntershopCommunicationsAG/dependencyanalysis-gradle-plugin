package com.intershop.gradle.analysis.model;

import groovy.transform.CompileStatic;
import groovy.util.logging.Slf4j;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@CompileStatic
@Slf4j
abstract class AbstractAnalyzedDependency implements AnalyzedDependency {

    private Set<File> files = [] as HashSet

    void addFile(File file) {
        this.files.add(file)
        containedClasses = getClassFiles(file)
    }

    Set<File> getFiles() {
        return files
    }

    String configuration

    String group
    String module
    String version
    Set<String> containedClasses = []
    Set<String> usedClasses = []

    Set<String> duplicatedClasses = []
    Set<String> excludedDuplicatedClasses = []
    Set<? extends AbstractAnalyzedDependency> duplicatedArtifacts = []
    Set<? extends AbstractAnalyzedDependency> excludedDuplicatedArtifacts = []

    boolean ignoreForAnalysis
    boolean projectDependeny
    boolean libraryDependency
    boolean firstLevel

    String projectPath

    AbstractAnalyzedDependency(String configuration) {
        this.configuration = configuration
    }

    void setTransitive(int value) {
        transitive = value
    }

    String getName() {
        return "${group}:${module}:${version}"
    }

    Set<String> getContainedClasses() {
        return containedClasses
    }


    /**
     * {@inheritDoc}
     */
    @Override
    boolean equals(Object other) {
        if (!(other instanceof AbstractAnalyzedDependency)) {
            return false
        }
        boolean rv = false

        rv = rv && ((AbstractAnalyzedDependency) other).getGroup() == group
        if (!rv) {
            return false
        }

        rv = ((AbstractAnalyzedDependency) other).getModule() == module
        if (!rv) {
            return false
        }

        rv = ((AbstractAnalyzedDependency) other).getVersion() == version
        if (!rv) {
            return false
        }

        rv = ((AbstractAnalyzedDependency) other).getConfiguration() == configuration
        if (!rv) {
            return false
        }

        rv = ((AbstractAnalyzedDependency) other).getProjectPath() == projectPath
        if (!rv) {
            return false
        }

        rv = ((AbstractAnalyzedDependency) other).getFiles().size() == files.size()
        if (!rv) {
            return false
        }

        Collection<File> similar = new HashSet<File>(((AbstractAnalyzedDependency) other).getFiles())
        Collection<File> different = new HashSet<File>()
        different.addAll(((AbstractAnalyzedDependency) other).getFiles())
        different.addAll(files)

        similar.retainAll(files)
        different.removeAll(similar)

        return different.isEmpty()

    }


    /**
     * {@inheritDoc}
     */
    @Override
    int hashCode() {
        int hash = 5
        if (configuration) {
            hash = 97 * hash + configuration.hashCode()
        }
        if (group) {
            hash = 97 * hash + group.hashCode()
        }
        if (module) {
            hash = 97 * hash + module.hashCode()
        }
        if (version) {
            hash = 97 * hash + version.hashCode()
        }
        if (projectPath) {
            hash = 97 * hash + projectPath.hashCode()
        }
        if (!files.isEmpty()) {
            files.each {
                hash = 97 * hash + it.getAbsolutePath().hashCode()
            }
        }
        return hash
    }

    /**
     * Calculates the used class files
     *
     * @param file
     * @return
     */
    static Set<String> getClassFiles(File file) {
        Set<String> classfiles = new HashSet<String>()

        try {
            ZipFile zipFile = new ZipFile(file)

            zipFile.entries().findAll { !((ZipEntry) it).directory && ((ZipEntry) it).name.endsWith('.class') }.each { Object entry ->
                String name = ((ZipEntry) entry).getName()

                if (name.endsWith(".class")) {
                    classfiles.add(name.replaceAll("/", "."))
                }
            }
            log.info('Analysing of {} was successful.', file)
        } catch (Exception ex) {
            log.error('Errors during the analyze of the jar file {}: {}', file, ex.getMessage())
        }
        return classfiles
    }


    abstract String getIdentifier()

    abstract String getDisplayName()
}