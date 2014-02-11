package com.ee.midas.dsl.interpreter.representation

import spock.lang.Specification


class ContextSpecs extends Specification {
    def "context is change set"() {
        when: 'A Context is created'
            def ctx = new Context()

        then: 'it is database aware'
            ctx instanceof DatabaseAware
    }

    def "context is DatabaseAware"() {
        when: 'A Context is created'
            def ctx = new Context()

        then: 'it is database aware'
            ctx instanceof ChangeSetAware
    }

    def "retrieves set database from context"() {
        given: 'A Context'
            def ctx = new Context()

        and: 'a database'
            def db = new Database('test', ctx)

        when: 'db is set'
            ctx.updateDB(db)

        then:
            ctx.currentDB() == db
    }

    def "retrieves set change set from context"() {
        given: 'A Context'
            def ctx = new Context()

        and: 'a database'
            def db = new Database('test', ctx)
            def cs = 2

        when: 'db is set'
            ctx.updateCS(cs)

        then:
            ctx.currentCS() == cs
    }

    def "resets change set in context"() {
        given: 'A Context'
            def ctx = new Context()

        and: 'a database'
            def db = new Database('test', ctx)

        and: 'db is set'
            ctx.updateCS(2)

        when:
            ctx.resetCS()

        then:
            ctx.currentCS() == 0
    }
}
