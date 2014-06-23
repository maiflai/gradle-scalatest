package com.github.maiflai.scalatest

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
        def extension = t.project.extensions.scalatest as ScalaTestExtension
        if (extension.isQuick()) {
            new QuickTestAction(extension.memento, extension.since).execute(t)
        } else {
            new FullTestAction().execute(t)
        }
    }

    static JavaExecAction makeAction(Test t, Iterable<String> args) {
        FileResolver fileResolver = t.getServices().get(FileResolver.class);
        JavaExecAction javaExecHandleBuilder = new DefaultJavaExecAction(fileResolver);
        javaExecHandleBuilder.setMain('org.scalatest.tools.Runner')
        javaExecHandleBuilder.setClasspath(t.getClasspath())
        javaExecHandleBuilder.setJvmArgs(t.getAllJvmArgs())
        javaExecHandleBuilder.setArgs(args)
        javaExecHandleBuilder.setIgnoreExitValue(true)
        return javaExecHandleBuilder
    }

    static List<String> getBasicArgs(Test t) {
        List<String> args = new ArrayList<String>()
        // this represents similar behaviour to the existing JUnit test action
        if (t.maxParallelForks == 0) {
            args.add('-PS')
        } else {
            args.add("-PS${t.maxParallelForks}".toString())
        }
        args.add('-R')
        args.add(t.getTestClassesDir().absolutePath)
        return args
    }
}
