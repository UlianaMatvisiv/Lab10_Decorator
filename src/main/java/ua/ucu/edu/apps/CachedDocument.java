package ua.ucu.edu.apps;

@AllArgsConstructor
public class CachedDocument implements Document {
    private String gcsPath;

    @Override
    public String parse() throws SQLException, IOException {
        try (Connection CONNECTION = DriverManager
                    .getConnection("jdbc:sqlite:cache.sqlite");
             Statement SQLSTATEMENT = CONNECTION.createStatement()) {
                SQLSTATEMENT.execute(
                "CREATE TABLE IF NOT EXISTS documents "
                + "(id INTEGER PRIMARY KEY, gcsPath TEXT, text TEXT)"
            );
            
            String text = getCachedText(CONNECTION);
            if (text != null) {
                return text;
            }
            text = new SmartDocument(gcsPath).parse();
            cacheText(CONNECTION, text);
            return text;
        }
    }

    private String getCachedText(Connection connection) throws SQLException {
        try (PreparedStatement STATEMENT = connection
        .prepareStatement("SELECT text FROM"
        + "documents WHERE gcsPath = ?")) {
            STATEMENT.setString(1, gcsPath);

            try (ResultSet RESULT = STATEMENT.executeQuery()) {
                if (RESULT.next()) {
                    return RESULT.getString("text");
                }
            }
        }
        return null;
    }

    private void cacheText(
        Connection connection, String text) throws SQLException {
        try (PreparedStatement STATEMENT = connection.prepareStatement(
        "INSERT INTO documents"
        + "(gcsPath, text) VALUES (?, ?)")) {
            STATEMENT.setString(1, gcsPath);
            STATEMENT.setString(2, text);
            STATEMENT.executeUpdate();
        }
    }
}
