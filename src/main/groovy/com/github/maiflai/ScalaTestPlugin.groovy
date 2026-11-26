package com.github.maiflai

import org.gradle.api.Plugin
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.scala.ScalaPlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.tasks.util.PatternSet
import org.gradle.process.ExecOperations

import javax.inject.Inject

/**
 * Applies the Java & Scala Plugins
 * Replaces Java Test actions with a <code>ScalaTestAction</code>
 */
class ScalaTestPlugin implements Plugin<Project> {

    static String MODE = 'com.github.maiflai.gradle-scalatest.mode'
    static enum Mode {
        replaceAll, replaceOne, append
    }

    private ExecOperations execOperations
    private Provider<Mode> mode 

    @Inject
    ScalaTestPlugin(ExecOperations execOperations, ProviderFactory providers) {
        this.execOperations = execOperations
        this.mode = providers.gradleProperty(MODE).map { it -> Mode.valueOf(it) }.orElse(Mode.replaceAll)
    }

    @Override
    void apply(Project t) {
        if (!t.plugins.hasPlugin(ScalaTestPlugin)) {
            t.plugins.apply(JavaPlugin)
            t.plugins.apply(ScalaPlugin)
            switch (mode.get()) {
                case Mode.replaceAll:
                    t.tasks.withType(Test).tap { configureEach { configure(it) } }
                    break
                case Mode.replaceOne:
                    t.tasks.withType(Test).tap {
                        configureEach {
                            if (it.name == JavaPlugin.TEST_TASK_NAME) {
                                configure(it)
                            }
                        }
                    }
                    break
                case Mode.append:
                    t.tasks.register('scalatest', Test) {
                        group = 'verification'
                        description = 'Run scalatest unit tests'
                        testClassesDirs = t.testing.suites.test.sources.output.classesDirs
                        classpath = t.testing.suites.test.sources.runtimeClasspath
                        configure(it)
                    }
                    break
            }
        }
    }

    void configure(Test test) {
        test.maxParallelForks = Runtime.runtime.availableProcessors()
        test.testLogging.exceptionFormat = TestExceptionFormat.SHORT
        PatternSet tags = new PatternSet()
        test.extensions.add(ScalaTestAction.TAGS, tags)
        List<String> suites = []
        test.extensions.add(ScalaTestAction.SUITES, suites)
        test.extensions.add("suite", { String name -> suites.add(name) })
        test.extensions.add("suites", { String... name -> suites.addAll(name) })
        Map<String, ?> config = [:]
        test.extensions.add(ScalaTestAction.CONFIG, config)
        test.extensions.add("config", { String name, value -> config.put(name, value) })
        test.extensions.add("configMap", { Map<String, ?> c -> config.putAll(c) })
        List<String> reporters = []
        test.extensions.add(ScalaTestAction.REPORTERS, reporters)
        test.extensions.add("reporter", { String name -> reporters.add(name) })
        test.extensions.add("reporters", { String... name -> reporters.addAll(name) })
        test.testLogging.events = TestLogEvent.values() as Set
        //noinspection GroovyAssignabilityCheck
        test.actions = [
                new JacocoTestAction(test.extensions.findByName('jacoco')),
                new ScalaTestAction(execOperations, test, config, suites, tags, reporters)
        ]
    }

}
