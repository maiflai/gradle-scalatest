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
 * Replaces all Java Test actions with a <code>ScalaTestAction</code>
 */
class ScalaTestPlugin implements Plugin<Project> {
    @Override
    void apply(Project t) {
        if (!t.plugins.hasPlugin(ScalaTestPlugin)) {
            t.plugins.add(this)
            t.plugins.apply(JavaPlugin)
            t.plugins.apply(ScalaPlugin)
            t.tasks.withType(Test) { test ->
                test.maxParallelForks = Runtime.runtime.availableProcessors()
                //noinspection GroovyAssignabilityCheck
                test.actions = [
                        new JacocoTestAction(),
                        new ScalaTestAction()
                ]
                test.testLogging.exceptionFormat = TestExceptionFormat.SHORT
                test.extensions.add(ScalaTestAction.TAGS, new PatternSet())
                List<String> suites = []
                test.extensions.add(ScalaTestAction.SUITES, suites)
                test.extensions.add("suite", { String name -> suites.add(name) } )
                test.extensions.add("suites", { String... name -> suites.addAll(name) } )
                Map<String, ?> config = [:]
                test.extensions.add(ScalaTestAction.CONFIG, config)
                test.extensions.add("config", { String name, value -> config.put(name, value) } )
                test.extensions.add("configMap", { Map<String, ?> c -> config.putAll(c) } )
                if (test.name != JavaPlugin.TEST_TASK_NAME) {
                    test.reports.html.destination = project.reporting.file(test.name)
                }
                test.testLogging.events = TestLogEvent.values() as Set
            }
        }
    }
}
