import org.junit.Test;

import java.util.function.Consumer;

/**
 * Created by leon on 25.10.14.
 */
public class TestResultStream {
    @Test
    public void testResultStream() throws Exception {
        WikiDAO wiki = new WikiDAO("/home/leon/Downloads/wiki.db");

        wiki.all().limit(100000).forEach(s -> {});
    }
}
