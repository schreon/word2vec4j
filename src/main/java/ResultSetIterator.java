import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Created by leon on 25.10.14.
 */
public class ResultSetIterator implements Iterable<String>, Iterator<String> {

    private boolean thisHasNext;
    private ResultSet resultSet;
    private long start, next, current, n, fin;
    private double res_sec;

    public ResultSetIterator(ResultSet resultSet) {
        this.resultSet = resultSet;
        try {
            thisHasNext = resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        start = System.nanoTime();
        next = start;
        n = 0;
        fin = 0;
    }

    @Override
    public Iterator<String> iterator() {
        return this;
    }

    @Override
    public synchronized boolean hasNext(){
        try {
            return !resultSet.isAfterLast();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized String next() {
        try {
            n += 1;
            if (n % 1000 == 0) {
                current = System.nanoTime();
                if (current > next) {
                    res_sec = (double) n / ((current - start) / 1000000000.0);
                    System.out.printf("%d @ %.2f results/second %n", n, res_sec);
                    next += TimeUnit.SECONDS.toNanos(1);
                }
            }
            if (resultSet.isClosed() || resultSet.isAfterLast()) {
                throw new NoSuchElementException();
            } else {
                resultSet.next();
            }
            return resultSet.getString(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
