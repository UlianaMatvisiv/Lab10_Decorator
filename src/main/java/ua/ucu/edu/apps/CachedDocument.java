package ua.ucu.edu.apps;

@AllArgsConstructor
public class CachedDocument implements Document {
    private String gcsPath;

    @Override
    public String parse() throws SQLException, IOException {
        try (Connection CONNECTION = DriverManager
                    .getConnection("jdbc:sqlite:cache.sqlite");
             Statement SQLStatement = CONNECTION.createStatement()) {
                SQLStatement.execute(
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

    private String getCachedText(Connection CONNECTION) throws SQLException {
        try (PreparedStatement STATEMENT = CONNECTION
        .prepareStatement("SELECT text FROM"
        + "documents WHERE gcsPath = ?")) {
            STATEMENT.setString(1, gcsPath);

            try (ResultSet resultSet = STATEMENT.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("text");
                }
            }
        }
        return null;
    }

    private void cacheText(
        Connection CONNECTION, String text) throws SQLException {
        try (PreparedStatement STATEMENT = CONNECTION.prepareStatement(
        "INSERT INTO documents"
        + "(gcsPath, text) VALUES (?, ?)")) {
            STATEMENT.setString(1, gcsPath);
            STATEMENT.setString(2, text);
            STATEMENT.executeUpdate();
        }
    }
}
