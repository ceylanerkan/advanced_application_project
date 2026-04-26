import java.sql.*;
public class TestDB2 {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecommerce_db", "root", "Safak321*");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE users");
        if(rs.next()) {
            System.out.println(rs.getString(2));
        }
        
        rs = stmt.executeQuery("SHOW CREATE TABLE stores");
        if(rs.next()) {
            System.out.println(rs.getString(2));
        }
    }
}
