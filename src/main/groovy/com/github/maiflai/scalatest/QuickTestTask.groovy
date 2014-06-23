package com.github.maiflai.scalatest

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.GradleConnector

import java.nio.file.FileSystems
import java.nio.file.Paths
import java.nio.file.WatchService

import static com.github.maiflai.scalatest.ScalaTestExtension.*
import static java.nio.file.StandardWatchEventKinds.*

/**
 * gradle implementation of the sbt ~testQuick target
 * @See https://bitbucket.org/grimrose/watchdog-continuous-test-plugin/
 */
class QuickTestTask extends DefaultTask {

    @TaskAction
    def watch(){
        def taskExecutor = new TaskExecutor(rootDir: project.projectDir)
        // produce an initial run
        taskExecutor.invoke()
        // and then watch for subsequent changes
        new RestrictedWatchService(delegate: fileWatchService(), executor: taskExecutor).polling()
    }

    WatchService fileWatchService() {
        def service = FileSystems.default.newWatchService()
        project.sourceSets.each {
            it.allSource.srcDirs.grep { it.exists() }.each { File dir ->
                def path = dir.path
                Paths.get(path).register(service, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
            }
        }
        service
    }

    static class RestrictedWatchService {

        WatchService delegate

        TaskExecutor executor

        void polling() {
            while (true) {
                def key = delegate.take()
                key.pollEvents().each { event ->
                    if (event.kind() == OVERFLOW) return
                    invoke()
                }

                boolean valid = key.reset();
                if (!valid) break
            }
        }

        void invoke() {
            executor.invoke()
        }

    }

    static class TaskExecutor {

        File rootDir
        String target = 'test'
        String memento = UUID.randomUUID().toString()

        void invoke() {
            def connection = GradleConnector.newConnector().forProjectDirectory(rootDir).connect()
            try {
                def out = new ByteArrayOutputStream()
                connection.newBuild()
                        .withArguments(
                            // the 'memento' is required to retain information from the previous build
                            // and is therefore static for our lifetime
                            toArgument(memento),
                            // however 'since' means classes that were compiled during the current build
                            // so we initialise it at each invocation
                            toArgument(new Date(System.currentTimeMillis())))
                        .forTasks(target)
                        .setStandardOutput(out)
                        .setStandardError(out)
                        .run()
            } catch (GradleConnectionException e) {
                println e
            } finally {
                connection?.close()
            }
        }

    }

}
