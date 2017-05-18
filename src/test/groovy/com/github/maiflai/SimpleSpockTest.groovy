package com.github.maiflai

import spock.lang.Specification

/**
 * Created by scr on 5/18/17.
 */
class SimpleSpockTest extends Specification {
    def "Simple spock test to see if the framework breaks on travis"() {
        given:
        Integer one = 1
        expect:
        one.toInteger() == 1
    }
}
