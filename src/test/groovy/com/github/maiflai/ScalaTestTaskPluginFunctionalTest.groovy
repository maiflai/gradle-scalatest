package com.github.maiflai

import org.junit.Test

import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.MatcherAssert.assertThat

/**
 * @author scr on 5/17/17.
 */
class ScalaTestTaskPluginFunctionalTest {
    @Test
    void testTestngAndScalatest() {
        File projectDir = new File('src/test/examples/testngAndScalatest')
        def launcher = SetupBuild.setupBuild(projectDir)
        launcher.forTasks('clean', 'check').run()

        File testResultsDir = new File(projectDir, 'build/test-results')
        File scalatestsResultsDir = new File(testResultsDir, 'scalatest')
        File scalatestTestFile = new File(scalatestsResultsDir, 'TEST-ScalatestTest.xml')
        File testngResultsDir = new File(testResultsDir, 'test')
        File testngTestFile = new File(testngResultsDir, 'TEST-TestngTest.xml')

        assertThat(scalatestTestFile.path + " Doesn't exist", scalatestTestFile.exists())
        assertThat(testngTestFile.path + " Doesn't exist", testngTestFile.exists())

        def scalatestTestResults = new XmlSlurper().parse(scalatestTestFile)
        assertThat(scalatestTestResults.testcase.@name.text(), is('scalatest test should pass'))

        def testngTestResults = new XmlSlurper().parse(testngTestFile)
        assertThat(testngTestResults.testcase.@name.text(), is('testPass'))
    }
}
