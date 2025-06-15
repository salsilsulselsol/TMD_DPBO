package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Kelas ini mengelola koneksi tingkat rendah ke database MySQL menggunakan JDBC.
// Mengimplementasikan AutoCloseable agar bisa digunakan dengan try-with-resources.
public class DB implements AutoCloseable {
    // Parameter untuk koneksi ke database.
    private String url = "jdbc:mysql://localhost:3306/db_tmd";
    private String user = "root";
    private String password = "";
    
    // Objek-objek inti JDBC untuk mengelola query.
    private Statement stm = null;
    private ResultSet rs = null;
    private Connection conn = null;

    // Konstruktor yang akan langsung membuat koneksi saat objek DB dibuat.
    public DB() throws SQLException {
        System.out.println("=== MEMBUAT KONEKSI DATABASE ===");
        connect();
    }
    
    // Metode privat untuk membangun koneksi ke database.
    private void connect() throws SQLException {
        try {
            // Memuat driver JDBC untuk MySQL.
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Membuat koneksi ke database menggunakan parameter yang ada.
            conn = DriverManager.getConnection(url, user, password);
            
            // Cek apakah koneksi berhasil dibuat.
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Koneksi database berhasil dibuat");
            } else {
                throw new SQLException("Koneksi null atau tertutup");
            }

        // Menangani error jika driver tidak ditemukan.
        } catch (ClassNotFoundException e) {
            System.err.println("✗ Driver MySQL tidak ditemukan!");
            throw new SQLException("Driver tidak ditemukan", e);
        // Menangani error jika koneksi gagal.
        } catch (SQLException e) {
            System.err.println("✗ Gagal membuat koneksi: " + e.getMessage());
            throw e;
        }
    }
    
    // Metode untuk memastikan koneksi masih aktif sebelum menjalankan query.
    private void ensureConnection() throws SQLException {
        try {
            // Jika koneksi null, tertutup, atau tidak valid, coba hubungkan kembali.
            if (conn == null || conn.isClosed() || !conn.isValid(5)) {
                System.out.println("⚠ Koneksi terputus, mencoba reconnect...");
                connect();
            }
        } catch (SQLException e) {
            System.err.println("✗ Gagal cek/reconnect koneksi: " + e.getMessage());
            connect(); // Coba hubungkan kembali jika terjadi error.
        }
    }

    // Metode untuk mengeksekusi query SELECT (yang mengembalikan data).
    public void createQuery(String query) throws SQLException {
        ensureConnection(); // Pastikan koneksi aktif.
        closeResultInternal(); // Tutup hasil query sebelumnya.
        
        try {
            stm = conn.createStatement();
            rs = stm.executeQuery(query); // Eksekusi query dan simpan hasilnya di 'rs'.
            System.out.println("✓ Query berhasil: " + query);
        } catch (SQLException e) {
            closeResultInternal(); // Tutup sumber daya jika terjadi error.
            System.err.println("✗ Gagal menjalankan query: " + query + " | Error: " + e.getMessage());
            throw e;
        }
    }

    // Metode untuk mengeksekusi query DML (INSERT, UPDATE, DELETE).
    public void createUpdate(String query) throws SQLException {
        ensureConnection(); // Pastikan koneksi aktif.
        
        Statement updateStm = null; // Gunakan statement lokal agar tidak mengganggu 'stm' utama.
        try {
            updateStm = conn.createStatement();
            int result = updateStm.executeUpdate(query); // Eksekusi query update.
            System.out.println("✓ Update berhasil: " + query + " (Affected rows: " + result + ")");
        } catch (SQLException e) {
            System.err.println("✗ Gagal menjalankan update: " + query + " | Error: " + e.getMessage());
            throw e;
        } finally {
            // Pastikan statement lokal selalu ditutup.
            if (updateStm != null) {
                try {
                    updateStm.close();
                } catch (SQLException e) { /* abaikan error saat menutup */ }
            }
        }
    }

    // Getter untuk mendapatkan hasil dari query SELECT.
    public ResultSet getResult() {
        return rs;
    }
    
    // Metode untuk mengecek status koneksi.
    public boolean isConnected() {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
    
    // Metode internal untuk menutup ResultSet dan Statement.
    private void closeResultInternal() throws SQLException {
        if (rs != null) {
            try {
                if (!rs.isClosed()) rs.close();
            } catch (SQLException e) { /* biarkan */ }
            rs = null;
        }
        if (stm != null) {
            try {
                if (!stm.isClosed()) stm.close();
            } catch (SQLException e) { /* biarkan */ }
            stm = null;
        }
    }
    
    // Metode close() dari interface AutoCloseable, dipanggil oleh try-with-resources.
    @Override
    public void close() throws SQLException {
        closeResultInternal(); // Tutup ResultSet dan Statement.
        // Tutup koneksi ke database.
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                    System.out.println("✓ Koneksi database ditutup");
                }
            } catch (SQLException e) { 
                System.err.println("⚠ Error saat menutup koneksi: " + e.getMessage());
            }
            conn = null;
        }
    }
}