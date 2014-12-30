gradle-scalatest
================
A plugin to enable the use of scalatest in a gradle Scala project.

Getting started
---------------
http://plugins.gradle.org/plugin/com.github.maiflai.scalatest

This replaces the existing test task actions with a scalatest implementation.

---

Parallel Testing
----------------
The default behaviour is to use as many parallel threads as you have available processors.

`Test` tasks are modified at the time that you apply the plugin (as otherwise they would default to single-threaded).

To disable this, you should configure your test tasks accordingly.

```groovy
test {
    maxParallelForks = 1
}
```

Tags
----
Scalatest provides support for filtering tests by tagging. We cannot use the `PatternSet` provided by the `Test`
task because it applies this filter to test files internally.

We therefore provide an extension named `tags` to `Test` tasks.

```groovy
test {
    tags {
        exclude 'org.scalatest.tags.Slow'
    }
}

task slowTest(type: Test) {
    tags {
        include 'org.scalatest.tags.Slow'
    }
}
```