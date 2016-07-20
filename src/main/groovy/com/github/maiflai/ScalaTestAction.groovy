package com.github.maiflai

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.reporting.DirectoryReport
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.util.PatternSet
import org.gradle.internal.UncheckedException
import org.gradle.process.internal.DefaultJavaExecAction
import org.gradle.process.internal.JavaExecAction

/**
 * <p>Designed to replace the normal Test Action with a new JavaExecAction
 * launching the scalatest Runner.</p>
 * <p>Classpath, JVM Args and System Properties are propagated.</p>
 * <p>Tests are launched against the testClassesDir.</p>
 */
class ScalaTestAction implements Action<Test> {

    static String TAGS = 'tags'
    static String SUITES = '_suites'
    static String CONFIG = '_config'

    @Override
    void execute(Test t) {
        def result = makeAction(t).execute()
        if (result.exitValue != 0){
            handleTestFailures(t)
        }
    }

    private static void handleTestFailures(Test t) {
        String message = "There were failing tests"
        def htmlReport = t.reports.html
        if (htmlReport.isEnabled()) {
            message = message.concat(". See the report at: ").concat(url(htmlReport))
        } else {
            def junitXmlReport = t.reports.junitXml
            if (junitXmlReport.isEnabled()) {
                message = message.concat(". See the results at: ").concat(url(junitXmlReport))
            }
        }
        if (t.ignoreFailures) {
            t.logger.warn(message)
        }
        else {
            throw new GradleException(message)
        }
    }

    private static String url(DirectoryReport report) {
        try {
            return new URI("file", "", report.getEntryPoint().toURI().getPath(), null, null).toString();
        } catch (URISyntaxException e) {
            throw UncheckedException.throwAsUncheckedException(e);
        }
    }


    static JavaExecAction makeAction(Test t) {
        FileResolver fileResolver = t.getServices().get(FileResolver.class);
        JavaExecAction javaExecHandleBuilder = new DefaultJavaExecAction(fileResolver);
        t.copyTo(javaExecHandleBuilder)
        javaExecHandleBuilder.setMain('org.scalatest.tools.Runner')
        javaExecHandleBuilder.setClasspath(t.getClasspath())
        javaExecHandleBuilder.setJvmArgs(t.getAllJvmArgs())
        javaExecHandleBuilder.setArgs(getArgs(t))
        javaExecHandleBuilder.setIgnoreExitValue(true)
        return javaExecHandleBuilder
    }

    private static Iterable<String> getArgs(Test t) {
        List<String> args = new ArrayList<String>()
        // this represents similar behaviour to the existing JUnit test action
        if (t.getProject().getGradle().getStartParameter().isColorOutput()) {
            args.add('-oD')
        } else {
            args.add('-oDW')
        }
        if (t.maxParallelForks == 0) {
            args.add('-PS')
        } else {
            args.add("-PS${t.maxParallelForks}".toString())
        }
        args.add('-R')
        args.add(t.getTestClassesDir().absolutePath.replace(' ', '\\ '))
        t.filter.includePatterns.each {
            args.add('-z')
            args.add(it)
        }
        if (t.reports.getJunitXml().isEnabled()){
            args.add('-u')
            args.add(t.reports.getJunitXml().getEntryPoint().getAbsolutePath())
        }
        if (t.reports.getHtml().isEnabled()){
            args.add('-h')
             def dest = t.reports.getHtml().getDestination()
             dest.mkdirs()
             args.add(dest.getAbsolutePath())
        }
        def tags = t.extensions.findByName(TAGS) as PatternSet
        if (tags) {
            tags.includes.each {
                args.add('-n')
                args.add(it)
            }
            tags.excludes.each {
                args.add('-l')
                args.add(it)
            }
        }
        def suites = t.extensions.findByName(SUITES) as List<String>
        suites?.toSet()?.each {
            args.add('-s')
            args.add(it)
        }
        def config = t.extensions.findByName(CONFIG) as Map<String, ?>
        config?.entrySet()?.each { entry ->
            args.add("-D${entry.key}=${entry.value}")
        }
        return args
    }
}
