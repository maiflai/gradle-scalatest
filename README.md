gradle-scalatest
================
A plugin to enable the use of scalatest in a gradle Scala project.

Getting started
---------------
http://plugins.gradle.org/plugin/com.github.maiflai.scalatest

This replaces the existing test task actions with a scalatest implementation.

---
Note that the default behaviour is to use as many parallel threads as you have available processors.

`Test` tasks are modified at the time that you apply the plugin (as otherwise they would default to single-threaded).

To disable this, you should configure your test tasks accordingly.

```groovy
test {
    maxParallelForks = 1
}
```

Note that the minimum supported ScalaTest version is now '2.0' in order to support collection of parallel test output
