import spock.lang.*

public class HelloWorldGroovySpecs extends Specification {
    def "says Hello"() {
        given: 'a world'
            def greeting = "Hello"

        when: 'I say hello to'
            def sayHelloTo = "World"

        then:
            greeting + ' ' + sayHelloTo == 'Hello World'
    }
}