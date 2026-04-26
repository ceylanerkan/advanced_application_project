import java.sql.*;
public class TestDB4 {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecommerce_db", "root", "Safak321*");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE id IS NULL");
        if(rs.next()) System.out.println("Null IDs in users: " + rs.getInt(1));
        
        rs = stmt.executeQuery("SELECT id, COUNT(*) FROM users GROUP BY id HAVING COUNT(*) > 1");
        int duplicates = 0;
        while(rs.next()) duplicates++;
        System.out.println("Duplicate IDs in users: " + duplicates);
    }
}
