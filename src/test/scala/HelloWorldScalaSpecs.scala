import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HelloWorldScalaSpecs extends Specification {
  "Says Hello to the world" should  {
      val greetings = ScalaGreeter.greet

      "contain 11 characters" in {
         greetings must have size(11)
      }

      "start with 'Hello'" in {
        greetings must startWith("Hello")
      }

     "end with 'World'" in {
       greetings must endWith("World")
     }
  }
}
