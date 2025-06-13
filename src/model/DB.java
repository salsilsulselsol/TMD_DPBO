package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DB implements AutoCloseable {
    private String url = "jdbc:mysql://localhost:3306/db_tmd";
    private String user = "root";
    private String password = "";
    
    private Statement stm = null;
    private ResultSet rs = null;
    private Connection conn = null;

    public DB() throws SQLException {
        System.out.println("=== MEMBUAT KONEKSI DATABASE ===");
        connect();
    }
    
    private void connect() throws SQLException {
        try {
            // Load driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Create connection
            conn = DriverManager.getConnection(url, user, password);
            
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Koneksi database berhasil dibuat");
            } else {
                throw new SQLException("Koneksi null atau tertutup");
            }

        } catch (ClassNotFoundException e) {
            System.err.println("✗ Driver MySQL tidak ditemukan!");
            throw new SQLException("Driver tidak ditemukan", e);
        } catch (SQLException e) {
            System.err.println("✗ Gagal membuat koneksi: " + e.getMessage());
            throw e;
        }
    }
    
    // Method untuk memastikan koneksi masih aktif
    private void ensureConnection() throws SQLException {
        try {
            if (conn == null || conn.isClosed() || !conn.isValid(5)) {
                System.out.println("⚠ Koneksi terputus, mencoba reconnect...");
                connect();
            }
        } catch (SQLException e) {
            System.err.println("✗ Gagal cek/reconnect koneksi: " + e.getMessage());
            connect(); // Try to reconnect anyway
        }
    }

    public void createQuery(String query) throws SQLException {
        ensureConnection(); // Pastikan koneksi aktif
        closeResultInternal(); 
        
        try {
            stm = conn.createStatement();
            rs = stm.executeQuery(query);
            System.out.println("✓ Query berhasil: " + query);
        } catch (SQLException e) {
            closeResultInternal(); 
            System.err.println("✗ Gagal menjalankan query: " + query + " | Error: " + e.getMessage());
            throw e;
        }
    }

    public void createUpdate(String query) throws SQLException {
        ensureConnection(); // Pastikan koneksi aktif
        
        Statement updateStm = null;
        try {
            updateStm = conn.createStatement();
            int result = updateStm.executeUpdate(query);
            System.out.println("✓ Update berhasil: " + query + " (Affected rows: " + result + ")");
        } catch (SQLException e) {
            System.err.println("✗ Gagal menjalankan update: " + query + " | Error: " + e.getMessage());
            throw e;
        } finally {
            if (updateStm != null) {
                try {
                    updateStm.close();
                } catch (SQLException e) { /* abaikan */ }
            }
        }
    }

    public ResultSet getResult() {
        return rs;
    }
    
    public boolean isConnected() {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
    
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
    
    @Override
    public void close() throws SQLException {
        closeResultInternal();
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