import java.sql.*;
public class TestDB {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecommerce_db", "root", "Safak321*");
        ResultSet rs = conn.getMetaData().getColumns(null, null, "users", null);
        while(rs.next()) {
            System.out.println(rs.getString("COLUMN_NAME") + " - " + rs.getString("TYPE_NAME") + " - " + rs.getString("IS_AUTOINCREMENT"));
        }
    }
}
