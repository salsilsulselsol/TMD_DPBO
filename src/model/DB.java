package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DB implements AutoCloseable { // Implement AutoCloseable
    private String url = "jdbc:mysql://localhost:3306/db_jellyfish_hunt"; // GANTI NAMA DB JIKA PERLU
    private String user = "root";
    private String password = "";
    
    private Statement stm = null;
    private ResultSet rs = null;
    private Connection conn = null;

    public DB() throws SQLException { // Disederhanakan, Exception generik tidak perlu
        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.err.println("Koneksi ke database gagal: " + e.getMessage());
            throw e;
        }
    }

    public void createQuery(String query) throws SQLException {
        // Tutup statement dan result set sebelumnya jika ada
        closeResultInternal(); 
        try {
            stm = conn.createStatement();
            rs = stm.executeQuery(query);
        } catch (SQLException e) {
            closeResultInternal(); 
            System.err.println("Gagal menjalankan query: " + query + " | Error: " + e.getMessage());
            throw e;
        }
    }

    public void createUpdate(String query) throws SQLException {
        Statement updateStm = null; // Gunakan statement lokal untuk update
        try {
            updateStm = conn.createStatement();
            updateStm.executeUpdate(query);
        } catch (SQLException e) {
            System.err.println("Gagal menjalankan update: " + query + " | Error: " + e.getMessage());
            throw e;
        } finally {
            if (updateStm != null) {
                try {
                    updateStm.close();
                } catch (SQLException e) { /* abaikan */ }
            }
        }
    }

    public ResultSet getResult() { // Tidak perlu throws Exception
        return rs;
    }
    
    // Metode internal untuk menutup rs dan stm yang terkait dengan createQuery
    private void closeResultInternal() throws SQLException {
        if (rs != null) {
            try {
                if (!rs.isClosed()) rs.close();
            } catch (SQLException e) { /* biarkan, mungkin sudah ditutup */ }
            rs = null;
        }
        if (stm != null) {
            try {
                if (!stm.isClosed()) stm.close();
            } catch (SQLException e) { /* biarkan */ }
            stm = null;
        }
    }
    
    // Untuk AutoCloseable, digunakan untuk menutup koneksi utama
    @Override
    public void close() throws SQLException {
        closeResultInternal(); // Pastikan rs dan stm dari createQuery juga ditutup
        if (conn != null) {
            try {
                if (!conn.isClosed()) conn.close();
            } catch (SQLException e) {
                // Biarkan, mungkin sudah ditutup atau ada masalah lain
            }
            conn = null;
        }
    }
}