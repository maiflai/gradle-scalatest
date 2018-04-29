package com.github.maiflai

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.core.CombinableMatcher.both
import static org.hamcrest.CoreMatchers.not

class CommandLineIntegrationTest {

    @Test
    void testSpecIsRun() throws Exception {
        setupBuild()
                .forTasks('clean', 'test', '--tests', 'MySpec')
                .run()
        assertThat(testReport, both(ran('bob')).and(ran('rita')))
    }

    @Test
    void testOnlyTestIsRun() throws Exception {
        setupBuild()
                .forTasks('clean', 'test', '--tests', 'bob')
                .run()
        assertThat(testReport, both(ran('bob')).and(not(ran('rita'))))
    }


    private static File projectDir = new File('src/test/examples/cmdline')
    private static File testReport = new File(projectDir, 'build/test-results/test/TEST-MySpec.xml')

    private static BuildLauncher setupBuild() {
        return GradleConnector.
                newConnector().
                forProjectDirectory(projectDir).
                connect().
                newBuild()
    }

    private static boolean contains(File file, String string) {
        return file.exists() &&
                file.canRead() &&
                file.isFile() &&
                file.readLines().grep { it.contains(string) }
    }

    private TypeSafeMatcher<File> ran(String testName) {
        return new TypeSafeMatcher<File>() {
            @Override
            protected boolean matchesSafely(File file) {
                return contains(file, "testcase name=\"$testName\" classname=\"MySpec\"")
            }

            @Override
            void describeTo(Description description) {
                description.appendText("a file containing a test result for $testName")
            }
        }

    }
}
