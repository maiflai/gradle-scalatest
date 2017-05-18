package com.github.maiflai

import spock.lang.Specification

/**
 * @author scr on 5/17/17.
 */
class ScalaTestTaskPluginFunctionalTest extends Specification {
    def "Plugin should run both testng & scalatest tests"() {
        given:
        File projectDir = new File('src/test/examples/testngAndScalatest')
        def launcher = SetupBuild.setupBuild(projectDir)

        when:
        launcher.forTasks('clean', 'check').run()
        and:
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
