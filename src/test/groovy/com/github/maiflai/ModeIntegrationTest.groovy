package com.github.maiflai

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat

class ModeIntegrationTest {

    @Test
    void testDefaultIsToReplaceAllTestTasks() throws Exception {
        setupBuild()
                .forTasks('clean', 'test', 'integrationTest')
                .run()
        assertThat(testReport, isScalaTestReport)
        assertThat(integrationTestReport, isScalaTestReport)
    }


    @Test
    void testAppendScalaTestTask() throws Exception {
        setupBuild(ScalaTestPlugin.Mode.append)
                .forTasks('clean', 'test', 'integrationTest', 'scalatest')
                .run()
        assertThat(scalaTestReport, isScalaTestReport)
        assertThat(testReport, isJUnitReport)
        assertThat(integrationTestReport, isJUnitReport)
    }

    @Test
    void testReplaceTestTask() throws Exception {
        setupBuild(ScalaTestPlugin.Mode.replaceOne)
                .forTasks('clean', 'test', 'integrationTest')
                .run()
        assertThat(testReport, isScalaTestReport)
        assertThat(integrationTestReport, isJUnitReport)
    }

    @Test
    void testReplaceAllTestTasks() throws Exception {
        setupBuild(ScalaTestPlugin.Mode.replaceAll)
                .forTasks('clean', 'test', 'integrationTest')
                .run()
        assertThat(testReport, isScalaTestReport)
        assertThat(integrationTestReport, isScalaTestReport)
    }

    private static File testReport = new File('src/test/examples/mixed/build/reports/tests/test/index.html')
    private static File scalaTestReport = new File('src/test/examples/mixed/build/reports/tests/scalatest/index.html')
    private static File integrationTestReport = new File('src/test/examples/mixed/build/reports/tests/integrationTest/index.html')

    private static BuildLauncher setupBuild() {
        return GradleConnector.
                newConnector().
                forProjectDirectory(new File('src/test/examples/mixed')).
                connect().
                newBuild()
    }

    private static BuildLauncher setupBuild(ScalaTestPlugin.Mode mode) {
        return GradleConnector.
                newConnector().
                forProjectDirectory(new File('src/test/examples/mixed')).
                connect().
                newBuild().
                withArguments("-Pcom.github.maiflai.gradle-scalatest.mode=$mode")
    }

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
