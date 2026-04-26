import java.sql.*;
public class FixDB {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecommerce_db", "root", "Safak321*");
             Statement stmt = conn.createStatement()) {
            
            try { stmt.execute("ALTER TABLE users MODIFY id BIGINT AUTO_INCREMENT PRIMARY KEY"); System.out.println("users table fixed!"); } catch (Exception e) { e.printStackTrace(); }
            try { stmt.execute("ALTER TABLE stores MODIFY id BIGINT AUTO_INCREMENT PRIMARY KEY"); System.out.println("stores table fixed!"); } catch (Exception e) { e.printStackTrace(); }
            try { stmt.execute("ALTER TABLE products MODIFY id BIGINT AUTO_INCREMENT PRIMARY KEY"); System.out.println("products table fixed!"); } catch (Exception e) { e.printStackTrace(); }
            try { stmt.execute("ALTER TABLE orders MODIFY id BIGINT AUTO_INCREMENT PRIMARY KEY"); System.out.println("orders table fixed!"); } catch (Exception e) { e.printStackTrace(); }
            try { stmt.execute("ALTER TABLE categories MODIFY id BIGINT AUTO_INCREMENT PRIMARY KEY"); System.out.println("categories table fixed!"); } catch (Exception e) { e.printStackTrace(); }
            try { stmt.execute("ALTER TABLE order_items MODIFY id BIGINT AUTO_INCREMENT PRIMARY KEY"); System.out.println("order_items table fixed!"); } catch (Exception e) { e.printStackTrace(); }
            try { stmt.execute("ALTER TABLE reviews MODIFY id BIGINT AUTO_INCREMENT PRIMARY KEY"); System.out.println("reviews table fixed!"); } catch (Exception e) { e.printStackTrace(); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
