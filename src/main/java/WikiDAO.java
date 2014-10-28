import org.sqlite.JDBC;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class WikiDAO implements Iterator<List<String>> {
    private final String pathToWikiDb;
    private final int chunkSize = 5000;
    private final long size;
    private long currentOffSet = 0;
    private List<String> resList;
    // Time measurement
    private long start, next, current;
    private double res_sec;
    private long limit = -1;

    public WikiDAO(String pathToWikiDb, long limit) throws Exception {
        this(pathToWikiDb);
        this.limit = limit;
    }
    public WikiDAO(String pathToWikiDb) throws Exception {
        start = System.nanoTime();
        this.pathToWikiDb = pathToWikiDb;
        size = getSize();
        resList = new ArrayList<>(chunkSize);
        next = start;
    }

    private long getSize() throws Exception {
        Connection con = JDBC.createConnection("jdbc:sqlite:" + (new File(pathToWikiDb).toURI().toURL()), new Properties());
        Statement s = con.createStatement();
        ResultSet r = s.executeQuery("SELECT COUNT(*) AS rowcount FROM pages");
        r.next();
        int count = r.getInt("rowcount");
        r.close();
        con.close();
        return count;
    }

    private List<String> getNextChunk() throws Exception {
        resList.clear();
        Connection con = JDBC.createConnection("jdbc:sqlite:" + (new File(pathToWikiDb).toURI().toURL()), new Properties());
        PreparedStatement stmt = con.prepareStatement("SELECT content FROM pages LIMIT " + chunkSize + " OFFSET " + Math.min(currentOffSet, size));
        ResultSet resultSet = stmt.executeQuery();

        while (resultSet.next()) {
            resList.add(resultSet.getString(1));
        }
        resultSet.close();
        con.close();
        currentOffSet += resList.size();


        // Time measurement
        current = System.nanoTime();
        if (current > next) {
            res_sec = (double) currentOffSet / ((current - start) / 1000000000.0);
            System.out.printf("%d @ %.2f results/second %n", currentOffSet, res_sec);
            next += TimeUnit.SECONDS.toNanos(1);
        }
        return resList;
    }

    public AsyncIterator<List<String>> async() {
        return new AsyncIterator<>(this);
    }

    @Override
    public boolean hasNext() {
        if (limit != -1) {
            return (currentOffSet < size) && (currentOffSet < limit);
        } else {
            return currentOffSet < size;
        }
    }

    @Override
    public List<String> next() {
        try {
            return new ArrayList<>(getNextChunk());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
