package com.github.maiflai

import org.junit.Test

import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.MatcherAssert.assertThat

/**
 * @author scr on 5/17/17.
 */
class ScalaTestTaskPluginFunctionalTest {
    @Test
    void testJunitAndScalatest() {
        File projectDir = new File('src/test/examples/twoTestFrameworks')
        def launcher = SetupBuild.setupBuild(projectDir)
        launcher.forTasks('clean', 'check').run()

        File testResultsDir = new File(projectDir, 'build/test-results')
        File scalatestsResultsDir = new File(testResultsDir, 'scalatest')
        File scalatestTestFile = new File(scalatestsResultsDir, 'TEST-ScalatestTest.xml')
        File junitResultsDir = new File(testResultsDir, 'test')
        File junitTestFile = new File(junitResultsDir, 'TEST-JunitTest.xml')

        assertThat(scalatestTestFile.path + " doesn't exist", scalatestTestFile.exists())
        assertThat(junitTestFile.path + " doesn't exist", junitTestFile.exists())

        def scalatestTestResults = new XmlSlurper().parse(scalatestTestFile)
        assertThat(scalatestTestResults.testcase.@name.text(), is('scalatest test should pass'))

        def junitTestResults = new XmlSlurper().parse(junitTestFile)
        assertThat(junitTestResults.testcase.@name.text(), is('testJunitPass'))
    }
}
