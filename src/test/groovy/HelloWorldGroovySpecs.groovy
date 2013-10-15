import spock.lang.*

public class HelloWorldGroovySpecs extends Specification {
    def "says Hello"() {
        given: 'a world'
            def greeter = new GroovyGreeter()

        when: 'I say hello'
            def greetings = greeter.greet()

        then:
            greetings == 'Hello World'
    }
}