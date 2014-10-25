import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetIterator;
import org.sqlite.JDBC;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by leon on 25.10.14.
 */
public class WikiDAO {
    private final Connection connection;
    private final PreparedStatement getAllStatement;
    public WikiDAO(String pathToWikiDb) throws Exception {
        this.connection = JDBC.createConnection("jdbc:sqlite:"+(new File(pathToWikiDb).toURI().toURL()), new Properties());
        getAllStatement = connection.prepareStatement("SELECT content FROM pages");
    }

    public Stream all() throws SQLException {
        final ResultSet resultSet = getAllStatement.executeQuery();
        ResultIterable it = new ResultIterable(resultSet);

        Stream<String> s = StreamSupport.stream(
          Spliterators.spliteratorUnknownSize(it, 0), false
        );
        return s;
    }
}
