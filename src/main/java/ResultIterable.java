import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * Created by leon on 25.10.14.
 */
public class ResultIterable implements Iterable<String>, Iterator<String> {

    private boolean thisHasNext;
    private ResultSet resultSet;

    public ResultIterable(ResultSet resultSet) {
        this.resultSet = resultSet;
        try {
            thisHasNext = resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

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
