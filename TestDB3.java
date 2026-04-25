import java.sql.*;
public class TestDB3 {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecommerce_db", "root", "Safak321*");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
        if(rs.next()) System.out.println("Users: " + rs.getInt(1));
        
        rs = stmt.executeQuery("SELECT COUNT(*) FROM stores");
        if(rs.next()) System.out.println("Stores: " + rs.getInt(1));
    }
}
