package com.github.maiflai

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.reporting.DirectoryReport
import org.gradle.api.tasks.testing.Test
import org.gradle.logging.ConsoleRenderer
import org.gradle.process.internal.DefaultJavaExecAction
import org.gradle.process.internal.JavaExecAction

/**
 * <p>Designed to replace the normal Test Action with a new JavaExecAction
 * launching the scalatest Runner.</p>
 * <p>Classpath, JVM Args and System Properties are propagated.</p>
 * <p>Tests are launched against the testClassesDir.</p>
 */
class ScalaTestAction implements Action<Test> {

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
        new ConsoleRenderer().asClickableFileUrl(report.getEntryPoint())
    }


    static JavaExecAction makeAction(Test t) {
        FileResolver fileResolver = t.getServices().get(FileResolver.class);
        JavaExecAction javaExecHandleBuilder = new DefaultJavaExecAction(fileResolver);
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
        args.add('-oID')
        if (t.maxParallelForks == 0) {
            args.add('-PS')
        } else {
            args.add("-PS${t.maxParallelForks}".toString())
        }
        args.add('-R')
        args.add(t.getTestClassesDir().absolutePath)
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
        return args
    }
}
