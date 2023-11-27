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
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cache.sqlite");
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS documents (id INTEGER PRIMARY KEY, gcsPath TEXT, text TEXT)");

            String text = getCachedText(conn);
            if (text != null) {
                return text;
            }
            text = new SmartDocument(gcsPath).parse();
            cacheText(conn, text);
            return text;
        }
    }

    private String getCachedText(Connection conn) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT text FROM documents WHERE gcsPath = ?")) {
            pstmt.setString(1, gcsPath);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("text");
                }
            }
        }
        return null;
    }

    private void cacheText(Connection conn, String text) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO documents (gcsPath, text) VALUES (?, ?)")) {
            pstmt.setString(1, gcsPath);
            pstmt.setString(2, text);
            pstmt.executeUpdate();
        }
    }
}
