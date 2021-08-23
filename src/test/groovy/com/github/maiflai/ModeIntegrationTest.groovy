package com.github.maiflai

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat

class ModeIntegrationTest extends BaseExampleTest {

    ModeIntegrationTest() {
        super('mixed')
    }

    @Test
    void testDefaultIsToReplaceAllTestTasks() throws Exception {
        runTasks('clean', 'test', 'integrationTest')
        assertThat(testReport, isScalaTestReport)
        assertThat(integrationTestReport, isScalaTestReport)
    }

    @Test
    void testAppendScalaTestTask() throws Exception {
        runTasks(ScalaTestPlugin.Mode.append, 'clean', 'test', 'integrationTest', 'scalatest')
        assertThat(scalaTestReport, isScalaTestReport)
        assertThat(testReport, isJUnitReport)
        assertThat(integrationTestReport, isJUnitReport)
    }

    @Test
    void testReplaceTestTask() throws Exception {
        runTasks(ScalaTestPlugin.Mode.replaceOne, 'clean', 'test', 'integrationTest')
        assertThat(testReport, isScalaTestReport)
        assertThat(integrationTestReport, isJUnitReport)
    }

    @Test
    void testReplaceAllTestTasks() throws Exception {
        runTasks(ScalaTestPlugin.Mode.replaceAll, 'clean', 'test', 'integrationTest')
        assertThat(testReport, isScalaTestReport)
        assertThat(integrationTestReport, isScalaTestReport)
    }

    private File testReport = new File(projectDir, 'build/reports/tests/test/index.html')
    private File scalaTestReport = new File(projectDir, 'build/reports/tests/scalatest/index.html')
    private File integrationTestReport = new File(projectDir, 'build/reports/tests/integrationTest/index.html')

    private static boolean contains(File file, String string) {
        return file.exists() &&
                file.canRead() &&
                file.isFile() &&
                file.readLines().grep { it.contains(string) }
    }

    private TypeSafeMatcher<File> isJUnitReport = new TypeSafeMatcher<File>() {
        @Override
        protected boolean matchesSafely(File file) {
            return contains(file, 'http://www.gradle.org')
        }

        @Override
        void describeTo(Description description) {
            description.appendText('a file containing the gradle runner signature')
        }
    }

    private TypeSafeMatcher<File> isScalaTestReport = new TypeSafeMatcher<File>() {
        @Override
        protected boolean matchesSafely(File file) {
            return contains(file, 'scalatest-report')
        }

        @Override
        void describeTo(Description description) {
            description.appendText('a file containing the scalatest signature')
        }
    }

}
