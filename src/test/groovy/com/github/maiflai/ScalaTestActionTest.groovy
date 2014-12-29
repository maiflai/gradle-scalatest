package com.github.maiflai;

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.process.internal.JavaExecAction;
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test

import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.CoreMatchers.hasItem
import static org.hamcrest.CoreMatchers.not
import static org.hamcrest.core.CombinableMatcher.both
import static org.junit.Assert.assertThat;

class ScalaTestActionTest {

    private static Project testProject() {
        Project project = ProjectBuilder.builder().build()
        project.plugins.apply(ScalaTestPlugin)
        project
    }

    private static org.gradle.api.tasks.testing.Test testTask() {
        testProject().tasks.test as org.gradle.api.tasks.testing.Test
    }

    private static List<String> commandLine(org.gradle.api.tasks.testing.Test task) {
        JavaExecAction action = ScalaTestAction.makeAction(task)
        action.getCommandLine()
    }

    @Test
    public void maxHeapSizeIsAdded() throws Exception {
        Task test = testTask()
        String size = '123m'
        test.maxHeapSize = size
        assertThat(commandLine(test), hasItem("-Xmx$size".toString()))
    }

    @Test
    public void minHeapSizeIsAdded() throws Exception {
        Task test = testTask()
        String size = '123m'
        test.minHeapSize = size
        assertThat(commandLine(test), hasItem("-Xms$size".toString()))
    }

    @Test
    public void jvmArgIsAdded() throws Exception {
        String permSize = '-XX:MaxPermSize=256m'
        Task test = testTask().jvmArgs(permSize)
        assertThat(commandLine(test), hasItem(permSize))
    }

    @Test
    public void sysPropIsAdded() throws Exception {
        Task test = testTask()
        test.systemProperties.put('bob', 'rita')
        assertThat(commandLine(test), hasItem('-Dbob=rita'))
    }

    @Test
    public void parallelDefaultsToProcessorCount() throws Exception {
        Task test = testTask()
        int processors = Runtime.runtime.availableProcessors()
        assertThat(commandLine(test), hasItem("-PS$processors".toString()))
    }

    @Test
    public void parallelSupportsConfiguration() throws Exception {
        Task test = testTask()
        int forks = Runtime.runtime.availableProcessors() + 1
        test.maxParallelForks = forks
        assertThat(commandLine(test), hasItem("-PS$forks".toString()))
    }

    @Test
    public void noTagsAreSpecifiedByDefault() throws Exception {
        Task test = testTask()
        assertThat(commandLine(test), both(not(hasItem('-n'))).and(not(hasItem('-l'))))
    }

    private static Matcher<List<String>> hasPair(String a, String b) {
        return new TypeSafeMatcher<List<String>>() {
            @Override
            protected boolean matchesSafely(List<String> strings) {
                def locationOfA = strings.indexOf(a)
                if (locationOfA != -1) {
                    return b.equals(strings.get(locationOfA + 1))
                } else {
                    return false
                }
            }

            @Override
            void describeTo(Description description) {
                description.appendText("a list containing $a followed by $b" )
            }
        }
    }

    @Test
    public void includesAreAddedAsTags() throws Exception {
        Task test = testTask()
        test.include('bob', 'rita')
        def args = commandLine(test)
        assertThat(args, hasPair('-n', 'bob rita'))
    }

    @Test
    public void excludesAreAddedAsTags() throws Exception {
        Task test = testTask()
        test.exclude('jane', 'sue')
        def args = commandLine(test)
        assertThat(args, hasPair('-l', 'jane sue'))
    }

    @Test
    public void testSpacesInTestClassesDirectoryAreEscaped() throws Exception {
        Task test = testTask()
        test.testClassesDir = new File("/bob rita sue")
        def args = commandLine(test)
        assertThat(args, hasPair('-R', '/bob\\ rita\\ sue'))
    }
}