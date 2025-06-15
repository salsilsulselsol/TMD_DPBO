package model;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

// Kelas ini berfungsi sebagai Data Access Object (DAO) untuk tabel 'thasil' di database.
// Merupakan turunan dari kelas DB untuk mewarisi fungsionalitas koneksi.
public class TableHasil extends DB {
    private String tableName; // Nama tabel yang dikelola oleh kelas ini.

    // Konstruktor, membuat koneksi database baru melalui parent class dan menetapkan nama tabel.
    public TableHasil() throws SQLException {
        super(); // Memanggil konstruktor DB untuk membuat koneksi.
        this.tableName = "thasil";
    }

    // Metode untuk memasukkan data baru atau mengupdate data yang sudah ada.
    public void insertOrUpdateHasil(GameData data) {
        ResultSet rsCheck = null;
        try {
            // Langkah 1: Cek apakah username sudah ada di database.
            String checkQuery = "SELECT skor, count FROM " + this.tableName + " WHERE username='" + data.getUsername() + "'";
            createQuery(checkQuery); 
            rsCheck = getResult();

            // Jika username sudah ada (rsCheck.next() mengembalikan true).
            if (rsCheck.next()) { 
                // Logika untuk mengakumulasi skor dan count pemain yang sudah ada.
                
                // Ambil skor dan count lama dari database.
                int oldSkor = rsCheck.getInt("skor");
                int oldCount = rsCheck.getInt("count");

                // Akumulasikan dengan skor dan count dari sesi permainan saat ini.
                int newSkor = oldSkor + data.getSkor();
                int newCount = oldCount + data.getCount();

                // Buat query UPDATE dengan nilai yang sudah diakumulasikan.
                String updateQuery = "UPDATE " + this.tableName + 
                                     " SET skor=" + newSkor + 
                                     ", count=" + newCount + 
                                     " WHERE username='" + data.getUsername() + "'";
                
                // Tutup hasil query SELECT sebelum menjalankan UPDATE.
                closeResultInternal(); 
                createUpdate(updateQuery); // Jalankan query UPDATE.

            } else { 
                // Jika username belum ada, masukkan sebagai data baru.
                closeResultInternal();
                String insertQuery = "INSERT INTO " + this.tableName + " (username, skor, count) VALUES ('" +
                                     data.getUsername() + "', " +
                                     data.getSkor() + ", " +
                                     data.getCount() + ")";
                createUpdate(insertQuery); // Jalankan query INSERT.
            }
        // Menangani jika terjadi error SQL.
        } catch (SQLException e) {
            System.err.println("Error di insertOrUpdateHasil: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Blok finally untuk memastikan koneksi dan result set selalu ditutup.
            try {
                if (rsCheck != null && !rsCheck.isClosed()) {
                    closeResultInternal();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Mengambil semua data dari tabel hasil untuk ditampilkan di leaderboard (JTable).
    public DefaultTableModel getAllHasilForTable() {
        // Membuat model tabel default sebagai 'wadah' data.
        DefaultTableModel dataTableModel = new DefaultTableModel(new Object[]{"Username", "Skor", "Count"}, 0);
        try {
            // Query untuk mengambil semua data, diurutkan berdasarkan skor tertinggi.
            String query = "SELECT username, skor, count FROM " + this.tableName + " ORDER BY skor DESC, count DESC";
            createQuery(query);
            ResultSet rs = getResult();
            // Looping untuk membaca setiap baris hasil query.
            while (rs.next()) {
                // Menambahkan setiap baris data dari database ke model tabel.
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
            // Selalu tutup koneksi setelah query selesai.
            try {
                closeResultInternal();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dataTableModel; // Mengembalikan model yang sudah terisi data.
    }
    
    // Metode internal untuk menutup ResultSet dan Statement.
    private void closeResultInternal() throws SQLException {
        if (getResult() != null && !getResult().isClosed()) {
            // Statement akan ditutup oleh super.close().
        }
        super.close(); // Memanggil metode close() dari kelas DB.
    }

    // Override metode close() dari DB untuk memastikan sumber daya ditutup dengan benar.
    // Berguna saat objek ini dibuat dalam blok try-with-resources.
    @Override
    public void close() throws SQLException {
        super.close(); // Panggil metode close() dari kelas DB untuk menutup koneksi.
    }
}