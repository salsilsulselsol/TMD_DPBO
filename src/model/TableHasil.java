package model;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

public class TableHasil extends DB { // Sebaiknya DB di-pass sebagai argumen atau pakai try-with-resources
    private String tableName;

    // Konstruktor yang menerima objek DB untuk dikelola dari luar
    // public TableHasil(DB database) { 
    //     super(); // Ini akan error jika DB tidak punya default constructor
    //     this.dbInstance = database; 
    //     this.tableName = "thasil";
    // }
    // Atau tetap seperti ini jika DB selalu membuat koneksi baru per instance TableHasil

    public TableHasil() throws SQLException { // Disederhanakan, Exception generik tidak perlu
        super(); // Membuat koneksi DB baru
        this.tableName = "thasil";
    }


    public void insertOrUpdateHasil(GameData data) {
        // PDF: "Jika sudah ada maka tidak perlu dimasukkan ke tabel thasil dan hanya menambah nilai score dan count." [cite: 32, 78]
        // Ini bisa diinterpretasikan sebagai: Jika user 'X' ada dengan skor 100, main lagi dapat 50, maka skor 'X' jadi 150.
        // Atau: Jika user 'X' ada skor 100, main lagi dapat 50, skor 'X' di-update jadi 50 (skor sesi ini).
        // Umumnya leaderboard game menyimpan skor tertinggi atau skor sesi terakhir.
        // Kode di bawah mengimplementasikan update/replace skor sesi terakhir.
        // Jika ingin akumulatif, logika SELECT skor lama + ADD + UPDATE diperlukan.
        
        ResultSet rsCheck = null;
        try {
            String checkQuery = "SELECT skor, count FROM " + this.tableName + " WHERE username='" + data.getUsername() + "'";
            createQuery(checkQuery); 
            rsCheck = getResult();

            if (rsCheck.next()) { 
                // Interpretasi: Update dengan skor baru jika lebih tinggi, atau selalu update.
                // Untuk tugas ini, kita akan selalu update dengan skor sesi ini.
                // Jika ingin akumulasi skor dan count dari DB:
                // int oldSkor = rsCheck.getInt("skor");
                // int oldCound = rsCheck.getInt("count");
                // data.setSkor(data.getSkor() + oldSkor);
                // data.setCount(data.getCount() + oldCound);

                String updateQuery = "UPDATE " + this.tableName + 
                                     " SET skor=" + data.getSkor() + 
                                     ", count=" + data.getCount() + 
                                     " WHERE username='" + data.getUsername() + "'";
                // closeResult() akan dipanggil sebelum createUpdate jika createQuery dieksekusi di try-with-resource.
                // Karena tidak, kita panggil manual.
                closeResultInternal(); // Menutup rsCheck dan statement-nya
                createUpdate(updateQuery);
            } else { 
                closeResultInternal();
                String insertQuery = "INSERT INTO " + this.tableName + " (username, skor, count) VALUES ('" +
                                     data.getUsername() + "', " +
                                     data.getSkor() + ", " +
                                     data.getCount() + ")";
                createUpdate(insertQuery);
            }
        } catch (SQLException e) { // Lebih spesifik dari Exception
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
            String query = "SELECT username, skor, count FROM " + this.tableName + " ORDER BY skor DESC, count DESC"; // Order by skor, lalu count
            createQuery(query);
            ResultSet rs = getResult();
            while (rs.next()) {
                dataTableModel.addRow(new Object[]{
                    rs.getString("username"),
                    rs.getInt("skor"),
                    rs.getInt("count")
                });
            }
        } catch (SQLException e) { // Lebih spesifik
            System.err.println("Error di getAllHasilForTable: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                closeResultInternal(); // Selalu tutup setelah query selesai
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dataTableModel;
    }
    
    // Metode internal untuk menutup rs dan stm dari createQuery() di TableHasil
    private void closeResultInternal() throws SQLException {
        if (getResult() != null && !getResult().isClosed()) { // Gunakan getter untuk ResultSet
            // Statement akan ditutup oleh super.closeResult() jika dipanggil
        }
        super.close(); // Panggil metode closeResult dari kelas DB
    }

    // Override close() dari DB untuk memastikan koneksi ditutup jika TableHasil dibuat dengan new
    @Override
    public void close() throws SQLException {
        super.close(); // Panggil close() dari kelas DB untuk menutup koneksi
    }
}