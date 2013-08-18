package com.github.maiflai

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.scala.ScalaPlugin
import org.gradle.api.tasks.testing.Test

class ScalaTestPlugin implements Plugin<Project> {
    @Override
    void apply(Project t) {
        if (!t.plugins.hasPlugin(ScalaTestPlugin)) {
            t.plugins.add(this)
            t.plugins.apply(JavaPlugin)
            t.plugins.apply(ScalaPlugin)
            t.tasks.withType(Test) { test ->
                test.actions = [new ScalaTestAction()]
            }
        }
    }
}
