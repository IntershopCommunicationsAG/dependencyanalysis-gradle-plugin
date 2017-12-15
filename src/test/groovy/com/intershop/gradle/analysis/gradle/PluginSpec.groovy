package com.intershop.gradle.analysis.gradle

import com.intershop.gradle.analysis.DependencyAnalysisPlugin
import com.intershop.gradle.test.AbstractProjectSpec
import org.gradle.api.Plugin

class PluginSpec extends AbstractProjectSpec {

    @Override
    Plugin getPlugin() {
        return new DependencyAnalysisPlugin()
    }
}
