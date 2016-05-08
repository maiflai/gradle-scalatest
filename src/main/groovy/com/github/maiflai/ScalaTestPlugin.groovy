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
 * Replaces Java Test actions with a <code>ScalaTestAction</code>
 */
class ScalaTestPlugin implements Plugin<Project> {

    static String MODE = 'com.github.maiflai.gradle-scalatest.mode'
    static enum Mode {
        replaceAll, replaceOne, append
    }
    BackwardsCompatibleJavaExecActionFactory factory

    @Override
    void apply(Project t) {
        if (!t.plugins.hasPlugin(ScalaTestPlugin)) {
            factory = new BackwardsCompatibleJavaExecActionFactory(t.gradle.gradleVersion)
            t.plugins.apply(JavaPlugin)
            t.plugins.apply(ScalaPlugin)
            switch (getMode(t)) {
                case Mode.replaceAll:
                    t.tasks.withType(Test) { configure(it, factory) }
                    break
                case Mode.replaceOne:
                    t.tasks.withType(Test) {
                        if (it.name == JavaPlugin.TEST_TASK_NAME) {
                            configure(it, factory)
                        }
                    }
                    break
                case Mode.append:
                    configure(t.tasks.create(
                            name: 'scalatest', type: Test, group: 'verification',
                            description: 'Run scalatest unit tests',
                            dependsOn: t.tasks.testClasses) as Test, factory)
                    break
            }
        }
    }

    private static Mode getMode(Project t) {
        if (!t.hasProperty(MODE)) {
            return Mode.replaceAll
        } else {
            return Mode.valueOf(t.properties[MODE].toString())
        }
    }

    static void configure(Test test, BackwardsCompatibleJavaExecActionFactory factory) {
        test.maxParallelForks = Runtime.runtime.availableProcessors()
        //noinspection GroovyAssignabilityCheck
        test.actions = [
                new JacocoTestAction(),
                new ScalaTestAction(factory)
        ]
        test.testLogging.exceptionFormat = TestExceptionFormat.SHORT
        test.extensions.add(ScalaTestAction.TAGS, new PatternSet())
        List<String> suites = []
        test.extensions.add(ScalaTestAction.SUITES, suites)
        test.extensions.add("suite", { String name -> suites.add(name) })
        test.extensions.add("suites", { String... name -> suites.addAll(name) })
        Map<String, ?> config = [:]
        test.extensions.add(ScalaTestAction.CONFIG, config)
        test.extensions.add("config", { String name, value -> config.put(name, value) })
        test.extensions.add("configMap", { Map<String, ?> c -> config.putAll(c) })

        def result = new StringBuilder()
        test.extensions.add(ScalaTestAction.TESTRESULT, result)
        test.extensions.add('testResult', { String name -> result.append(name) } )

        def output = new StringBuilder()
        test.extensions.add(ScalaTestAction.TESTOUTPUT, output)
        test.extensions.add('testOutput', { String name -> output.append(name) } )

        def errorOutput = new StringBuilder()
        test.extensions.add(ScalaTestAction.TESTERROR, errorOutput)
        test.extensions.add('testError', { String name -> errorOutput.append(name) } )

        test.testLogging.events = TestLogEvent.values() as Set
    }

}
