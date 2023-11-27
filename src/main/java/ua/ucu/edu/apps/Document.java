package ua.ucu.edu.apps;

import java.io.IOException;
import java.sql.SQLException;

public interface Document {
    String parse() throws IOException, SQLException;
}
