package model;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

public class TableHasil extends DB {
    private String tableName;

    public TableHasil() throws SQLException {
        super(); // Membuat koneksi DB baru
        this.tableName = "thasil";
    }

    public void insertOrUpdateHasil(GameData data) {
        ResultSet rsCheck = null;
        try {
            // Cek apakah username sudah ada di database
            String checkQuery = "SELECT skor, count FROM " + this.tableName + " WHERE username='" + data.getUsername() + "'";
            createQuery(checkQuery); 
            rsCheck = getResult();

            if (rsCheck.next()) { 
                // --- MODIFIKASI DIMULAI ---
                // Jika username sudah ada, ambil data lama dan akumulasikan.

                // 1. Ambil skor dan count yang sudah ada dari database.
                int oldSkor = rsCheck.getInt("skor");
                int oldCount = rsCheck.getInt("count");

                // 2. Akumulasikan dengan skor dan count dari sesi permainan saat ini.
                int newSkor = oldSkor + data.getSkor();
                int newCount = oldCount + data.getCount();

                // 3. Buat query UPDATE dengan nilai yang sudah diakumulasikan.
                String updateQuery = "UPDATE " + this.tableName + 
                                     " SET skor=" + newSkor + 
                                     ", count=" + newCount + 
                                     " WHERE username='" + data.getUsername() + "'";
                
                // Tutup result set dari query SELECT sebelum menjalankan UPDATE
                closeResultInternal(); 
                createUpdate(updateQuery);
                // --- MODIFIKASI SELESAI ---

            } else { 
                // Jika username belum ada, masukkan sebagai data baru.
                closeResultInternal();
                String insertQuery = "INSERT INTO " + this.tableName + " (username, skor, count) VALUES ('" +
                                     data.getUsername() + "', " +
                                     data.getSkor() + ", " +
                                     data.getCount() + ")";
                createUpdate(insertQuery);
            }
        } catch (SQLException e) {
            System.err.println("Error di insertOrUpdateHasil: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                // Pastikan rsCheck dan statement-nya ditutup jika belum
                 if (rsCheck != null && !rsCheck.isClosed()) {
                    closeResultInternal();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public DefaultTableModel getAllHasilForTable() {
        DefaultTableModel dataTableModel = new DefaultTableModel(new Object[]{"Username", "Skor", "Count"}, 0);
        try {
            String query = "SELECT username, skor, count FROM " + this.tableName + " ORDER BY skor DESC, count DESC";
            createQuery(query);
            ResultSet rs = getResult();
            while (rs.next()) {
                dataTableModel.addRow(new Object[]{
                    rs.getString("username"),
                    rs.getInt("skor"),
                    rs.getInt("count")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error di getAllHasilForTable: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                closeResultInternal();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dataTableModel;
    }
    
    private void closeResultInternal() throws SQLException {
        if (getResult() != null && !getResult().isClosed()) {
            // Statement akan ditutup oleh super.close()
        }
        super.close(); 
    }

    @Override
    public void close() throws SQLException {
        super.close();
    }
}