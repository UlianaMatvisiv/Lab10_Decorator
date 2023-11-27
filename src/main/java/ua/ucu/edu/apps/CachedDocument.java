package ua.ucu.edu.apps;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


@AllArgsConstructor
public class CachedDocument implements Document {
    private String gcsPath;

    @Override
    public String parse() throws SQLException, IOException {
        try (Connection connect = DriverManager
                    .getConnection("jdbc:sqlite:cache.sqlite");
             Statement statement = connect.createStatement()) {
            statement.execute(
                "CREATE TABLE IF NOT EXISTS documents " +
                "(id INTEGER PRIMARY KEY, gcsPath TEXT, text TEXT)"
            );
            
            String text = getCachedText(connect);
            if (text != null) {
                return text;
            }
            text = new SmartDocument(gcsPath).parse();
            cacheText(connect, text);
            return text;
        }
    }

    private String getCachedText(Connection connect) throws SQLException {
        try (PreparedStatement pstatement = connect
        .prepareStatement("SELECT text FROM documents WHERE gcsPath = ?")) {
            pstatement.setString(1, gcsPath);

            try (ResultSet resultset = pstatement.executeQuery()) {
                if (resultset.next()) {
                    return resultset.getString("text");
                }
            }
        }
        return null;
    }

    private void cacheText(Connection connect, String text) throws SQLException {
        try (PreparedStatement pstatement = connect
        .prepareStatement("INSERT INTO documents (gcsPath, text) VALUES (?, ?)")) {
            pstatement.setString(1, gcsPath);
            pstatement.setString(2, text);
            pstatement.executeUpdate();
        }
    }
}
