package content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by schreon on 10/31/14.
 */
public abstract class DocumentsProvider {
    public void start(final Connection con, final int start, final int end) throws Exception {
        try {
            final PreparedStatement stmt = con.prepareStatement("SELECT content FROM pages LIMIT " + (end - start) + " OFFSET " + start);
            final ResultSet resultSet = stmt.executeQuery();
            onStart();
            while (resultSet.next()) {
                onDocument(resultSet.getString(1));
            }
            resultSet.getStatement().close();
            onEnd();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void onStart() {}
    public void onEnd() {}

    public abstract void onDocument(String nextDoc);


}
