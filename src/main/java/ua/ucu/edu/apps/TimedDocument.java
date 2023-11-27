package ua.ucu.edu.apps;

import java.io.IOException;
import java.sql.*;

public class TimedDocument implements Document {
    private final Document document;
    public TimedDocument(Document document) {
        this.document = document;
    }

    @Override
    public String parse() throws IOException, SQLException {
        long startTime = System.currentTimeMillis();
        String result = document.parse();
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        System.out.println("Parsing time: " 
        + Long.toString(duration) + " milliseconds");
        return result;
    }
}
