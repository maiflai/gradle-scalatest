package com.github.maiflai

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat

class JacocoTestActionIntegrationTest extends BaseExampleTest {

    JacocoTestActionIntegrationTest() {
        super('jacoco')
    }

    @Test
    void testReportsAreProduced() throws Exception {
        runTasks('clean', 'test', 'jacocoTestReport')
        assertThat(new File(projectDir, 'build/reports/jacoco/test/html'), isReport)
        assertThat(new File(projectDir, 'build/reports/tests/test'), isReport)
        assertThat(new File(projectDir, 'build/test-results/test/TEST-HelloSpec.xml'), isReadableFile)
    }

    protected TypeSafeMatcher<File> isReport = new TypeSafeMatcher<File>() {
        @Override
        protected boolean matchesSafely(File file) {
            return file.isDirectory() && new File(file, 'index.html').isFile()
        }

        @Override
        void describeTo(Description description) {
            description.appendText('a directory containing index.html')
        }
    }

    protected TypeSafeMatcher<File> isReadableFile = new TypeSafeMatcher<File>() {
        @Override
        protected boolean matchesSafely(File file) {
            return file.isFile() && file.exists() && file.canRead()
        }

        @Override
        void describeTo(Description description) {
            description.appendText('a readable file')
        }
    }
}
