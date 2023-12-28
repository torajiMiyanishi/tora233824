import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseReader {
    private String dbFilePath;

    public DatabaseReader(String dbFilePath) {
        this.dbFilePath = dbFilePath;
    }

    public void readAndPrintData() {
        String url = "jdbc:sqlite:" + dbFilePath;

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM population_data ORDER BY CurrentTime DESC, X DESC, Y DESC")) {

            System.out.println("CurrentTime\tX\tY\tTotalPopulation");
            while (rs.next()) {
                String currentTime = rs.getString("CurrentTime");
                int x = rs.getInt("X");
                int y = rs.getInt("Y");
                int totalPopulation = rs.getInt("TotalPopulation");

                System.out.println(currentTime + "\t" + x + "\t" + y + "\t" + totalPopulation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
