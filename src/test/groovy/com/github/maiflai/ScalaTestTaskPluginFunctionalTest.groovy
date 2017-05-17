package com.github.maiflai

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * @author scr on 5/17/17.
 */
class ScalaTestTaskPluginFunctionalTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    File projectDir
    File buildFile

    void setup() {
        projectDir = temporaryFolder.root
        buildFile = temporaryFolder.newFile('build.gradle')

        def pluginClasspathResource = getClass().getResource("/plugin-classpath.txt")
        assert pluginClasspathResource != null
        def pluginClasspath = pluginClasspathResource.readLines()
                .collect { it.replace('\\', '\\\\') } // escape backslashes in Windows paths
                .collect { "'$it'" }
                .join(", ")
        buildFile << """
    buildscript {
        dependencies {
            classpath files($pluginClasspath)
        }
    }
    apply plugin: 'com.github.maiflai.scalatest.task'
"""
    }

    protected BuildResult build(String... arguments) {
        createAndConfigureGradleRunner(arguments).build()
    }

    protected BuildResult buildAndFail(String... arguments) {
        createAndConfigureGradleRunner(arguments).buildAndFail()
    }

    private GradleRunner createAndConfigureGradleRunner(String... arguments) {
        GradleRunner.create().withProjectDir(projectDir).withArguments(arguments)
    }

    def "Plugin should apply and have scalatest task"() {
        when:
        BuildResult result = build('tasks', '--all')
        println(result.output)
        then:
        result.task(':tasks').outcome == TaskOutcome.SUCCESS
        result.output.contains("scalatest - ")
    }

    def "Plugin should run both testng & scalatest tests"() {
        given:
        new AntBuilder().copy(todir: projectDir) {
            fileset(dir: new File('src/test/examples/testngAndScalatest'))
        }
        and:
        buildFile << """
    repositories {
        mavenCentral()
    }

    dependencies {
        testCompile 'org.scala-lang:scala-library:2.11.8'
        testCompile "org.scalatest:scalatest_2.11:3.0.1"
    
        testRuntime 'org.pegdown:pegdown:1.6.0'
    
        testCompile 'org.testng:testng:6.11'
    }
    test.useTestNG()
"""
        when:
        BuildResult result = build('check')
        then:
        result.task(':check').outcome == TaskOutcome.SUCCESS
        when:
        File testResultsDir = new File(projectDir, 'build/test-results')
        File scalatestsResultsDir = new File(testResultsDir, 'scalatest')
        File scalatestTestFile = new File(scalatestsResultsDir, 'TEST-ScalatestTest.xml')
        File testngResultsDir = new File(testResultsDir, 'test')
        File testngTestFile = new File(testngResultsDir, 'TEST-TestngTest.xml')
        then:
        scalatestTestFile.exists()
        testngTestFile.exists()

        when:
        def scalatestTestResults = new XmlSlurper().parse(scalatestTestFile)
        then:
        scalatestTestResults.testcase.@name.text() == 'scalatest test should pass'

        when:
        def testngTestResults = new XmlSlurper().parse(testngTestFile)
        then:
        testngTestResults.testcase.@name.text() == 'testPass'
    }
}
