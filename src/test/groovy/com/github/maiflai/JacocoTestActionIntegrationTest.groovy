package com.github.maiflai

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat

class JacocoTestActionIntegrationTest {

    @Test
    public void testReportsAreProduced() throws Exception {
        def launcher = setupBuild(new File('src/test/examples/jacoco'))
        launcher.forTasks('clean', 'test', 'jacoco').run()
        assertThat(new File('src/test/examples/jacoco/build/reports/jacoco/test/html'), isReport)
        assertThat(new File('src/test/examples/jacoco/build/reports/tests'), isReport)
    }

    protected BuildLauncher setupBuild(File projectRoot) {
        return GradleConnector.
                newConnector().
                forProjectDirectory(projectRoot).
                connect().
                newBuild()
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
}
