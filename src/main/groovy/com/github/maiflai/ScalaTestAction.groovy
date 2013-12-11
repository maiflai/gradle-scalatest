package com.github.maiflai

import org.gradle.api.Action
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.testing.Test
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
        FileResolver fileResolver = t.getServices().get(FileResolver.class);
        JavaExecAction javaExecHandleBuilder = new DefaultJavaExecAction(fileResolver);
        javaExecHandleBuilder.setMain('org.scalatest.tools.Runner')
        javaExecHandleBuilder.setClasspath(t.getClasspath())
        javaExecHandleBuilder.setArgs(getArgs(t))
        javaExecHandleBuilder.setJvmArgs(t.getJvmArgs())
        javaExecHandleBuilder.setSystemProperties(t.getSystemProperties())
        javaExecHandleBuilder.execute();
    }

    private static Iterable<String> getArgs(Test t) {
        List<String> args = new ArrayList<String>()
        args.add('-P')
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
