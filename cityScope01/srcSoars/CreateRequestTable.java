import java.sql.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CreateRequestTable {
    private String dbFilePath;
    private String csvFilePath;

    public CreateRequestTable(String dbFilePath, String csvFilePath) {
        this.dbFilePath = dbFilePath;
        this.csvFilePath = csvFilePath;
    }

    public void initializeTableFromCSV() {
        String url = "jdbc:sqlite:" + dbFilePath;

        String dropTableSQL = "DROP TABLE IF EXISTS BuildingRequestTable;";
        String createTableSQL = "CREATE TABLE BuildingRequestTable (\n"
                + " GridCode TEXT NOT NULL,\n"
                + " RequestNo INTEGER,\n"
                + " RequestCode INTEGER NOT NULL,\n"
                + " BuildingTypeCode TEXT NOT NULL\n"
                + ");";

        String insertSQL = "INSERT INTO BuildingRequestTable (GridCode, RequestNo, RequestCode, BuildingTypeCode) VALUES (?, ?, ?, ?);";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            // Drop existing table and create a new one
            stmt.execute(dropTableSQL);
            stmt.execute(createTableSQL);

            // Insert data using PreparedStatement
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL);
                 BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
                // Skip the header line
                br.readLine();

                // Read CSV and insert data
                String line;
                while ((line = br.readLine()) != null) {
                    String[] fields = line.split(",");
                    insertStmt.setString(1, fields[0]); // GridCode as a string
                    insertStmt.setInt(2, Integer.parseInt(fields[1])); // RequestNo
                    insertStmt.setInt(3, Integer.parseInt(fields[2])); // RequestCode
                    insertStmt.setString(4, fields[3]); // BuildingTypeCode
                    insertStmt.executeUpdate();
                }

                System.out.println("Table initialized with data from CSV successfully.");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

}

