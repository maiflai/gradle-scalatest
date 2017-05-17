package com.github.maiflai

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.scala.ScalaPlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.tasks.util.PatternSet

/**
 * Applies the Java & Scala Plugins
 * Adds a scalatest Test task with a <code>ScalaTestAction</code>
 */
class ScalaTestTaskPlugin implements Plugin<Project> {
    @Override
    void apply(Project t) {
        if (!t.plugins.hasPlugin(ScalaTestPlugin)) {
            t.plugins.add(this)
            t.plugins.apply(JavaPlugin)
            t.plugins.apply(ScalaPlugin)
            def scalatestTask = t.tasks.create(
                    name: 'scalatest', type: Test, group: 'verification',
                    description: 'Run scalatest unit tests',
                    dependsOn: t.tasks.testClasses) {
                maxParallelForks = Runtime.runtime.availableProcessors()
                //noinspection GroovyAssignabilityCheck
                actions = [
                        new JacocoTestAction(),
                        new ScalaTestAction()
                ]
                testLogging.exceptionFormat = TestExceptionFormat.SHORT
                extensions.add(ScalaTestAction.TAGS, new PatternSet())
                List<String> suites = []
                extensions.add(ScalaTestAction.SUITES, suites)
                extensions.add("suite", { String name -> suites.add(name) })
                extensions.add("suites", { String... name -> suites.addAll(name) })
                Map<String, ?> config = [:]
                extensions.add(ScalaTestAction.CONFIG, config)
                extensions.add("config", { String name, value -> config.put(name, value) })
                extensions.add("configMap", { Map<String, ?> c -> config.putAll(c) })
                if (name != JavaPlugin.TEST_TASK_NAME) {
                    reports.html.destination = project.reporting.file(name)
                }
                testLogging.events = TestLogEvent.values() as Set
            }
            t.tasks.check.dependsOn scalatestTask
        }
    }
}
