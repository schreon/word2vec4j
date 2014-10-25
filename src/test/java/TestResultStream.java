import org.junit.Test;

import java.util.function.Consumer;

/**
 * Created by leon on 25.10.14.
 */
public class TestResultStream {
    @Test
    public void testResultStream(){
        WikiDAO wiki = new WikiDAO("/home/leon/Downloads/wiki.db");

        wiki.stream().limit(100).forEach(new Consumer<String>(){

            @Override
            public void accept(String s) {

            }
        });
    }
}
