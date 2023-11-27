package ua.ucu.edu.apps;

import java.io.IOException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException, IOException {
        Document sd = new SmartDocument("gs://oop_10/eco_banner_design_with_3d_contrasted_earth_illustration_6825293.jpg");
        System.out.println(sd.parse());
        Document timeDocument = new TimedDocument(null);
        System.out.println(timeDocument.parse());
    }
}