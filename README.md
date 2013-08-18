gradle-scct
===========
A plugin to enable the use of scalatest in a gradle Scala project.

Getting started
---------------
```groovy
buildscript {
    repositories {
        maven { url 'https://github.com/maiflai/mvn-repo/raw/master/snapshots' }
    }
    dependencies {
        classpath 'com.github.maiflai:gradle-scalatest:0.1-SNAPSHOT'
    }
}

apply plugin: 'scalatest'

dependencies {
    compile 'org.scala-lang:scala-library:2.10.1'
    testCompile 'org.scalatest:scalatest_2.10:2.0.M5b'
    testRuntime 'org.pegdown:pegdown:1.0.1'
}
```

This replaces the existing test task actions with a scalatest implementation.
