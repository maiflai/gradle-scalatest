package com.github.maiflai.scalatest

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.tasks.testing.Test
import org.gradle.process.ExecResult
import org.gradle.process.internal.ExecException

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

import static com.github.maiflai.scalatest.ScalaTestAction.*

class QuickTestAction implements Action<Test> {

    File memento
    Date since

    QuickTestAction(File memento, Date since) {
        this.memento = memento
        this.since = since
    }

    @Override
    void execute(Test t) {

        t.logger.info("Using memento file at $memento.absolutePath")

        def argsTemplate = getArgsTemplate(t)

        def visitor = new NewFileVisitor(since)
        Files.walkFileTree(Paths.get(t.getTestClassesDir().absolutePath), visitor)
        def classes = visitor.files.collect { extractClass(it.toFile()) }.toSet()

        def newlyFailed = tempFile()
        def failedOnRepeat = tempFile()

        def result1 = executeNewSuites(classes, t, argsTemplate, newlyFailed)

        def result2 = executePreviousFailedSuites(newlyFailed, t, argsTemplate, failedOnRepeat)

        memento.parentFile.mkdirs()
        memento.withWriter { w ->
            newlyFailed.withReader { r ->
                w << r
            }
            failedOnRepeat.withReader { r ->
                w << r
            }
        }

        if (result1.exitValue != 0 || result2.exitValue !=0){
            throw new GradleException('There were failing tests')
        }
    }

    private ExecResult executePreviousFailedSuites(File newlyFailed, Test t, List<String> argsTemplate, File failedOnRepeat) {
        def stillToRun = subtract(memento, newlyFailed)
        if (stillToRun.length()) {
            t.logger.lifecycle("Re-running failed tests from ${stillToRun.absolutePath}")
            def failed = makeAction(t, argsTemplate)
            failed.args rerunFailures(stillToRun)
            failed.args recordFailures(failedOnRepeat)
            failed.execute()
        } else {
            t.logger.info('No extra tests to re-run')
            OkExecResult
        }
    }

    private ExecResult executeNewSuites(Set<String> classes, Test t, List<String> argsTemplate, File newlyFailed) {
        if (classes) {
            t.logger.lifecycle("Running as ${classes.size()} class files changed since $since")
            def additional = makeAction(t, argsTemplate)
            classes.each { additional.args runSuite(it) }
            additional.args recordFailures(newlyFailed)
            additional.execute()
        } else {
            t.logger.info('No change detected to test classes')
            OkExecResult
        }
    }

    private static File tempFile() {
        File.createTempFile('scalatest', '.txt')
    }

    private static getArgsTemplate(Test t) {
        def basic = getBasicArgs(t)
        basic.add(simpleReport)
        basic
    }

    private static final String simpleReport = '-oD'

    private static def runSuite(String suite) {
        ['-q', suite]
    }

    private static def rerunFailures(File file) {
        ['-A', file.absolutePath]
    }

    private static def recordFailures(File file) {
        ['-M', file.absolutePath]
    }

    /*
     * it appears that scalatest treats these as potential test cases
     * so we don't need to validate that they are indeed actually Suites
     */
    private static def extractClass(File file) {
        def name = file.getName()
        def dollar = name.indexOf('$')
        if (dollar == -1) dollar = name.length()
        def dot = name.indexOf('.')
        name.substring(0, Math.min(dollar, dot))
    }

    private static def subtract(File one, File another) {
        def output = tempFile()
        if (one.exists()) {
            def toRemove = another.readLines()
            output.withWriter { w ->
                one.withReader { r ->
                    r.each { line ->
                        if (!toRemove.contains(line)) {
                            w.write(line)
                            w.write('\n')
                        }
                    }
                }
            }
        }
        output
    }

    /**
     * Collects files that have been modified after a particular instant
     */
    static class NewFileVisitor extends SimpleFileVisitor<Path> {

        private final Date since;

        private List<Path> files = new ArrayList<Path>()

        NewFileVisitor(Date since) {
            this.since = since
        }

        @Override
        FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.toFile().lastModified() > since.time) {
                files.add(file)
            }
            return super.visitFile(file, attrs)
        }
    }

    private static ExecResult OkExecResult = new ExecResult() {
        @Override
        int getExitValue() {
            return 0
        }

        @Override
        ExecResult assertNormalExitValue() throws ExecException {
            return this
        }

        @Override
        ExecResult rethrowFailure() throws ExecException {
            return this
        }
    }

}
