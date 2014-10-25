import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Created by leon on 25.10.14.
 */
public class ResultSetIterator implements Iterable<String>, Iterator<String> {

    private boolean thisHasNext;
    private ResultSet resultSet;
    private long start, next, current, n;
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
    }

    @Override
    public Iterator<String> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        synchronized (resultSet) {
            return this.thisHasNext;
        }
    }

    @Override
    public String next() {
        synchronized(resultSet) {
            if (hasNext()) {
                try {
                    n += 1;
                    current = System.nanoTime();
                    if (current > next) {
                        res_sec = (double)n / ((current - start) / 1000000000.0);
                        System.out.printf("%.2f results/second %n", res_sec);
                        next += TimeUnit.SECONDS.toNanos(1);
                    }
                    this.thisHasNext = resultSet.next();
                    return resultSet.getString(1);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return null;
            }

        }
    }
}
