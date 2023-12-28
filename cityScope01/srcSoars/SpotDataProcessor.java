import java.sql.*;
import java.io.*;
import java.util.*;

public class SpotDataProcessor {
    private String dbFilePath;
    private String csvFilePath;

    public SpotDataProcessor(String dbFilePath, String csvFilePath) {
        this.dbFilePath = dbFilePath;
        this.csvFilePath = csvFilePath;
    }

    public void process() throws SQLException, IOException {
        String url = "jdbc:sqlite:" + dbFilePath;

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {

            // データベーステーブルの初期化
            stmt.execute("DROP TABLE IF EXISTS population_data");
            stmt.execute("CREATE TABLE population_data (" +
                    "CurrentTime TEXT, " +
                    "X INTEGER, " +
                    "Y INTEGER, " +
                    "TotalPopulation INTEGER)");

            // CSVからのデータ読み込みと集計
            Map<String, Integer> aggregatedData = new HashMap<>();
            String line;
            br.readLine(); // ヘッダースキップ

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                String key = values[0] + "," + values[1] + "," + values[2]; // CurrentTime,X,Y
                int population = Integer.parseInt(values[3]);
                aggregatedData.merge(key, population, Integer::sum);
            }

            // 集計結果をデータベースに挿入
            String sql = "INSERT INTO population_data (CurrentTime, X, Y, TotalPopulation) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            aggregatedData.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Collections.reverseOrder()))
                    .forEach(entry -> {
                        try {
                            String[] parts = entry.getKey().split(",");
                            pstmt.setString(1, parts[0]);
                            pstmt.setInt(2, Integer.parseInt(parts[1]));
                            pstmt.setInt(3, Integer.parseInt(parts[2]));
                            pstmt.setInt(4, entry.getValue());
                            pstmt.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }
}
