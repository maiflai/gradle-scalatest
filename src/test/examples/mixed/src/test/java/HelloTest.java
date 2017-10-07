import org.junit.Test;
import org.junit.Assert;

public class HelloTest {
    @Test
    public void testCoverage() {
       Assert.assertEquals(new Hello().say(), "world");
    }
}