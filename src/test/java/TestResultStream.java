import org.junit.Test;

import java.util.function.Consumer;

/**
 * Created by leon on 25.10.14.
 */
public class TestResultStream {
    @Test
    public void testResultStream() throws Exception {
        WikiDAO wiki = new WikiDAO("/home/leon/Downloads/wiki.db");

        wiki.all().limit(5).forEach(new Consumer<String>() {

            @Override
            public void accept(String s) {
                System.out.println(s);
            }
        });
    }
}
