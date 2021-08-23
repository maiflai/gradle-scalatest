package com.github.maiflai

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Test

import static org.hamcrest.CoreMatchers.not
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.core.CombinableMatcher.both

class CommandLineIntegrationTest extends BaseExampleTest {

    CommandLineIntegrationTest() {
        super('cmdline')
    }

    @Test
    void testSpecIsRun() throws Exception {
        runTasks('clean', 'test', '--tests', 'MySpec')
        assertThat(testReport, both(ran('bob')).and(ran('rita')))
    }

    @Test
    void testOnlyTestIsRun() throws Exception {
        runTasks('clean', 'test', '--tests', 'bob')
        assertThat(testReport, both(ran('bob')).and(not(ran('rita'))))
    }

    private File testReport = new File(projectDir, 'build/test-results/test/TEST-MySpec.xml')

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
