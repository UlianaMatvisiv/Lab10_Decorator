package ua.ucu.edu.apps;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.sql.*;


@AllArgsConstructor
public class CachedDocument implements Document {
    private String gcsPath;

    @Override
    public String parse() throws SQLException, IOException {
        try (Connection connection = DriverManager
                    .getConnection("jdbc:sqlite:cache.sqlite");
             Statement sqlStatement = connection.createStatement()) {
                sqlStatement.execute(
                "CREATE TABLE IF NOT EXISTS documents "
                + "(id INTEGER PRIMARY KEY, gcsPath TEXT, text TEXT)"
            );
            
            String text = getCachedText(connection);
            if (text != null) {
                return text;
            }
            text = new SmartDocument(gcsPath).parse();
            cacheText(connection, text);
            return text;
        }
    }

    private String getCachedText(Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection
        .prepareStatement("SELECT text FROM"
        + "documents WHERE gcsPath = ?")) {
            preparedStatement.setString(1, gcsPath);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("text");
                }
            }
        }
        return null;
    }

    private void cacheText(Connection connect, String text) throws SQLException {
        try (PreparedStatement preparedStatement = connect
        .prepareStatement("INSERT INTO documents"
        + "(gcsPath, text) VALUES (?, ?)")) {
            preparedStatement.setString(1, gcsPath);
            preparedStatement.setString(2, text);
            preparedStatement.executeUpdate();
        }
    }
}
