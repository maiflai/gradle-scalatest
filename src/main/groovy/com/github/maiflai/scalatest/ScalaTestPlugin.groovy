package com.github.maiflai.scalatest

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.scala.ScalaPlugin
import org.gradle.api.tasks.testing.Test

/**
 * Applies the Java & Scala Plugins
 * Replaces all Java Test actions with a <code>ScalaTestAction</code>
 * Adds a new task 'quickTest' which repeatedly runs failing and updated tests
 */
class ScalaTestPlugin implements Plugin<Project> {
    @Override
    void apply(Project t) {
        if (!t.plugins.hasPlugin(ScalaTestPlugin)) {
            t.plugins.add(this)
            t.plugins.apply(JavaPlugin)
            t.plugins.apply(ScalaPlugin)
            t.extensions.create('scalatest', ScalaTestExtension, t)
            t.tasks.create('quickTest', QuickTestTask)
            t.tasks.withType(Test) { test ->
                test.maxParallelForks = Runtime.runtime.availableProcessors()
                test.actions = [new ScalaTestAction()]
            }
        }
    }
}
