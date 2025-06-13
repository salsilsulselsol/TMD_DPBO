// Simpan sebagai TestConnection.java di folder src/
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        // Konfigurasi untuk XAMPP
        String url = "jdbc:mysql://localhost:3306/db_tmd?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String password = ""; // XAMPP default
        
        System.out.println("=== TEST KONEKSI DATABASE XAMPP ===");
        System.out.println("URL: " + url);
        System.out.println("User: " + user);
        System.out.println("Password: " + (password.isEmpty() ? "[KOSONG - Default XAMPP]" : "[ADA]"));
        
        try {
            // Load driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ Driver MySQL berhasil dimuat");
            
            // Test koneksi
            System.out.println("Mencoba koneksi ke XAMPP MySQL...");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✓ KONEKSI KE XAMPP BERHASIL!");
            System.out.println("Database: " + conn.getCatalog());
            System.out.println("Server Info: " + conn.getMetaData().getDatabaseProductName() + " " + conn.getMetaData().getDatabaseProductVersion());
            
            conn.close();
            System.out.println("✓ Koneksi ditutup");
            
        } catch (ClassNotFoundException e) {
            System.err.println("✗ Driver MySQL tidak ditemukan!");
            System.err.println("Pastikan mysql-connector-j-9.3.0.jar ada di folder lib/ dan ada di classpath");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("✗ Koneksi ke XAMPP gagal!");
            System.err.println("Pastikan:");
            System.err.println("1. XAMPP Control Panel terbuka");
            System.err.println("2. MySQL service di XAMPP sudah START (lampu hijau)");
            System.err.println("3. Database 'db_tmd' sudah dibuat di phpMyAdmin");
            System.err.println("4. Port 3306 tidak diblokir");
            System.err.println("");
            System.err.println("Detail Error: " + e.getMessage());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            e.printStackTrace();
        }
    }
}