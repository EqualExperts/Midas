import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public final class HelloWorldSpecs {
    @Test
    public void sayHello() {
        assertEquals(new JavaGreeter().greet(), "Hello World");
    }
}
