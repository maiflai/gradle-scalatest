package com.github.maiflai

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.process.internal.JavaExecAction;
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Test

import static org.hamcrest.CoreMatchers.hasItem
import static org.hamcrest.CoreMatchers.not
import static org.hamcrest.core.CombinableMatcher.both
import static org.junit.Assert.assertThat

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

    private static Matcher<List<String>> hasOption(String option, String required) {
        return new TypeSafeMatcher<List<String>>() {
            @Override
            protected boolean matchesSafely(List<String> strings) {
                def optionLocations = strings.findIndexValues { it == option }
                def optionValues = optionLocations.grep { locationOfOption ->
                    def optionValue = strings.get((locationOfOption + 1) as Integer)
                    required.equals(optionValue)
                }
                return optionValues.size() == 1
            }

            @Override
            void describeTo(Description description) {
                description.appendText("a list containing $option followed by $required")
            }
        }
    }

    @Test
    public void includesAreAddedAsTags() throws Exception {
        Task test = testTask()
        test.include('bob', 'rita')
        def args = commandLine(test)
        assertThat(args, both(hasOption('-n', 'bob')).and(hasOption('-n', 'rita')))
    }

    @Test
    public void excludesAreAddedAsTags() throws Exception {
        Task test = testTask()
        test.exclude('jane', 'sue')
        def args = commandLine(test)
        assertThat(args, both(hasOption('-l', 'jane')).and(hasOption('-l', 'sue')))
    }

    @Test
    public void testSpacesInTestClassesDirectoryAreEscaped() throws Exception {
        Task test = testTask()
        test.testClassesDir = new File("/bob rita sue")
        def args = commandLine(test)
        assertThat(args, hasOption('-R', '/bob\\ rita\\ sue'))
    }
}