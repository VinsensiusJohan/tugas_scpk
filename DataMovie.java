import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

class MovieModel {
    private final String DBurl = "jdbc:mysql://localhost:3306/reviewfilm";
    private final String DBuser = "root";
    private final String DBpassword = "";

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(DBurl, DBuser, DBpassword);
    }

    public Object[][] getAllMovies() throws SQLException {
        Object[][] data = new Object[25][5];
        try (Connection koneksi = connect();
             Statement syntax = koneksi.createStatement()) {
            String sql = "SELECT * FROM movie";
            ResultSet result_set = syntax.executeQuery(sql);

            int i = 0;
            while (result_set.next()) {
                data[i][0] = result_set.getString("judul");
                data[i][1] = result_set.getDouble("alur");
                data[i][2] = result_set.getDouble("penokohan");
                data[i][3] = result_set.getDouble("akting");
                data[i][4] = result_set.getDouble("nilai");
                i++;
            }
        }
        return data;
    }

    public void addMovie(Object[] data) throws SQLException {
        try (Connection koneksi = connect();
             Statement syntax = koneksi.createStatement()) {

            String judul = (String) data[0];
            Double alur = (Double) data[1];
            Double tokoh = (Double) data[2];
            Double akting = (Double) data[3];
            double rating = (alur + tokoh + akting) / 3;

            String sql = "INSERT INTO movie VALUES('" + judul + "', " + alur + ", " + tokoh + ", " + akting + ", " + rating + ")";
            syntax.executeUpdate(sql);
        }
    }

    public void updateMovie(Object[] data) throws SQLException {
        try (Connection koneksi = connect();
             Statement syntax = koneksi.createStatement()) {

            String judul = (String) data[0];
            Double alur = (Double) data[1];
            Double tokoh = (Double) data[2];
            Double akting = (Double) data[3];
            double rating = (alur + tokoh + akting) / 3;

            String sql = "UPDATE movie SET alur = " + alur + ", penokohan = " + tokoh + ", akting = " + akting + ", nilai = " + rating + " WHERE judul = '" + judul + "'";
            int update_check = syntax.executeUpdate(sql);

            if (update_check == 0) {
                throw new SQLException("Tidak ada judul yang sesuai");
            }
        }
    }

    public void deleteMovie(String judul) throws SQLException {
        try (Connection koneksi = connect();
             Statement syntax = koneksi.createStatement()) {

            String sql = "DELETE FROM movie WHERE judul = '" + judul + "'";
            int update_check = syntax.executeUpdate(sql);

            if (update_check == 0) {
                throw new SQLException("Tidak ada judul yang sesuai");
            }
        }
    }
}

class MovieView extends JFrame {
    JPanel control_panel_1 = new JPanel();
    JPanel control_panel_2 = new JPanel();
    JPanel main_panel = new JPanel();
    JButton tambah_button = new JButton("Tambah");
    JButton update_button = new JButton("Update");
    JButton delete_button = new JButton("Delete");
    JButton clear_button = new JButton("Clear");
    JLabel label_judul = new JLabel("Judul");
    JLabel label_alur = new JLabel("Alur");
    JLabel label_tokoh = new JLabel("Penokohan");
    JLabel label_akting = new JLabel("Akting");
    JTextField field_judul = new JTextField();
    JTextField field_alur = new JTextField();
    JTextField field_tokoh = new JTextField();
    JTextField field_akting = new JTextField();
    String[] kolom = {"Judul", "Alur", "Penokohan", "Akting", "Rating"};
    JTable tabel_data = new JTable(new Object[25][5], kolom);
    JScrollPane scroll_table = new JScrollPane(tabel_data);

    public MovieView() {
        setTitle("Data Movie");
        setSize(650, 500);
        setLocation(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        control_panel_1.setLayout(new GridLayout(4, 2));
        control_panel_1.add(label_judul);
        control_panel_1.add(field_judul);
        control_panel_1.add(label_alur);
        control_panel_1.add(field_alur);
        control_panel_1.add(label_tokoh);
        control_panel_1.add(field_tokoh);
        control_panel_1.add(label_akting);
        control_panel_1.add(field_akting);

        control_panel_2.setLayout(new FlowLayout());
        control_panel_2.add(tambah_button);
        control_panel_2.add(update_button);
        control_panel_2.add(delete_button);
        control_panel_2.add(clear_button);

        main_panel.setLayout(new BorderLayout());
        main_panel.add(scroll_table, BorderLayout.CENTER);
        main_panel.add(control_panel_1, BorderLayout.NORTH);
        main_panel.add(control_panel_2, BorderLayout.SOUTH);
        add(main_panel);

        setVisible(true);
    }

    public String getJudul() {
        return field_judul.getText();
    }

    public Double getAlur() throws NumberFormatException {
        return Double.valueOf(field_alur.getText());
    }

    public Double getTokoh() throws NumberFormatException {
        return Double.valueOf(field_tokoh.getText());
    }

    public Double getAkting() throws NumberFormatException {
        return Double.valueOf(field_akting.getText());
    }

    public void clearFields() {
        field_judul.setText("");
        field_alur.setText("");
        field_tokoh.setText("");
        field_akting.setText("");
    }

    public void setTableData(Object[][] data) {
        tabel_data.setModel(new javax.swing.table.DefaultTableModel(data, kolom));
    }

    public void addTambahButtonListener(ActionListener listener) {
        tambah_button.addActionListener(listener);
    }

    public void addUpdateButtonListener(ActionListener listener) {
        update_button.addActionListener(listener);
    }

    public void addDeleteButtonListener(ActionListener listener) {
        delete_button.addActionListener(listener);
    }

    public void addClearButtonListener(ActionListener listener) {
        clear_button.addActionListener(listener);
    }
}

class MovieController {
    private final MovieModel model;
    private final MovieView view;

    public MovieController(MovieModel model, MovieView view) {
        this.model = model;
        this.view = view;

        this.view.addTambahButtonListener(new AddButtonListener());
        this.view.addUpdateButtonListener(new UpdateButtonListener());
        this.view.addDeleteButtonListener(new DeleteButtonListener());
        this.view.addClearButtonListener(new ClearButtonListener());

        refreshTable();
    }

    private void refreshTable() {
        try {
            Object[][] data = model.getAllMovies();
            view.setTableData(data);
        } catch (SQLException e) {
            view.setTableData(new Object[25][5]);
            JOptionPane.showMessageDialog(view, "Data Gagal Ditampilkan", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Object[] getInputData() {
        try {
            String judul = view.getJudul();
            Double alur = view.getAlur();
            Double tokoh = view.getTokoh();
            Double akting = view.getAkting();
            return new Object[]{judul, alur, tokoh, akting};
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Data yang dimasukan salah !!!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    class AddButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object[] data = getInputData();
            if (data != null) {
                try {
                    model.addMovie(data);
                    refreshTable();
                    JOptionPane.showMessageDialog(view, "Input Berhasil Dilakukan !!!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(view, "Query Input Gagal !!!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    class UpdateButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object[] data = getInputData();
            if (data != null) {
                try {
                    model.updateMovie(data);
                    refreshTable();
                    JOptionPane.showMessageDialog(view, "Update Berhasil Dilakukan !!!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(view, "Query Update Gagal !!!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    class DeleteButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String judul = view.getJudul();
            try {
                model.deleteMovie(judul);
                refreshTable();
                JOptionPane.showMessageDialog(view, "Hapus Berhasil Dilakukan !!!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(view, "Query Hapus Gagal !!!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    class ClearButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            view.clearFields();
        }
    }
}

public class DataMovie {
    public static void main(String[] args) {
        MovieModel model = new MovieModel();
        MovieView view = new MovieView();
        new MovieController(model, view);
    }
}
