import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LibraryApp extends JFrame implements ActionListener {
    private JTextField authorField, yearField, titleField, genreField;
    private JButton addButton, editButton, deleteButton;
    private JTable table;
    private DefaultTableModel tableModel;
    private Connection connection;

    public LibraryApp() {
        setTitle("Библиотека");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1920, 1080);

        // Создание полей ввода
        authorField = new JTextField(20);
        yearField = new JTextField(20);
        titleField = new JTextField(20);
        genreField = new JTextField(20);

        // Создание кнопок
        addButton = new JButton("Добавить");
        editButton = new JButton("Редактировать");
        deleteButton = new JButton("Удалить");

        // Добавление слушателя событий для кнопок
        addButton.addActionListener(this);
        editButton.addActionListener(this);
        deleteButton.addActionListener(this);

        // Создание таблицы
        tableModel = new DefaultTableModel();
        tableModel.addColumn("Автор");
        tableModel.addColumn("Год издания");
        tableModel.addColumn("Название");
        tableModel.addColumn("Жанр");
        table = new JTable(tableModel);

        // Создание панели для полей ввода и кнопок
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2));
        inputPanel.add(new JLabel(""));
        inputPanel.add(new JLabel(""));
        inputPanel.add(new JLabel(""));
        inputPanel.add(new JLabel(""));
        inputPanel.add(new JLabel("Автор: "));
        inputPanel.add(authorField);
        inputPanel.add(new JLabel("Год издания: "));
        inputPanel.add(yearField);
        inputPanel.add(new JLabel("Название: "));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Жанр: "));
        inputPanel.add(genreField);

        // Создание панели для кнопок
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        // Создание панели с таблицей
        JScrollPane scrollPane = new JScrollPane(table);

        // Добавление компонентов на форму
        Container container = getContentPane();
        container.add(inputPanel, BorderLayout.NORTH);
        container.add(buttonPanel, BorderLayout.CENTER);
        container.add(scrollPane, BorderLayout.SOUTH);

        // Подключение к базе данных
        connectToDatabase();
        loadBooks();
    }

    // Подключение к базе данных
    private void connectToDatabase() {
        String url = "jdbc:mysql://localhost:3306/library";
        String username = "root";
        String password = "481987237";

        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Загрузка данных из базы данных
    private void loadBooks() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM book");

            while (resultSet.next()) {
                String author = resultSet.getString("author");
                int year = resultSet.getInt("publishingYear");
                String title = resultSet.getString("title");
                String genre = resultSet.getString("genre");

                tableModel.addRow(new Object[]{author, year, title, genre});
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Очистка полей ввода
    private void clearFields() {
        authorField.setText("");
        yearField.setText("");
        titleField.setText("");
        genreField.setText("");
    }

    // Обработчик событий кнопок
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            addBook();
        } else if (e.getSource() == editButton) {
            editBook();
        } else if (e.getSource() == deleteButton) {
            deleteBook();
        }
    }

    // Добавление записи в базу данных
    private void addBook() {
        String author = authorField.getText();

        int year;
        try {
            year = Integer.parseInt(yearField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Неправильный формат ввода для года издания. Введите целое число.");
            return;
        }

        String title = titleField.getText();
        String genre = genreField.getText();


        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO book (author, publishingYear, title, genre) VALUES (?, ?, ?, ?)");
            statement.setString(1, author);
            statement.setInt(2, year);
            statement.setString(3, title);
            statement.setString(4, genre);

            statement.executeUpdate();
            statement.close();

            // Обновление таблицы
            tableModel.addRow(new Object[]{author, year, title, genre});

            // Очистка полей ввода
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Редактирование записи в базе данных
    private void editBook() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите запись для редактирования.");
            return;
        }

        String author = authorField.getText();
        int year = Integer.parseInt(yearField.getText());
        String title = titleField.getText();
        String genre = genreField.getText();

        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE book SET author = ?, publishingYear = ?, title = ?, genre = ? WHERE author = ? AND publishingYear = ? AND title = ? AND genre = ?");
            statement.setString(1, author);
            statement.setInt(2, year);
            statement.setString(3, title);
            statement.setString(4, genre);
            statement.setString(5, table.getValueAt(selectedRow, 0).toString());
            statement.setInt(6, Integer.parseInt(table.getValueAt(selectedRow, 1).toString()));
            statement.setString(7, table.getValueAt(selectedRow, 2).toString());
            statement.setString(8, table.getValueAt(selectedRow, 3).toString());

            statement.executeUpdate();
            statement.close();

            // Обновление таблицы
            tableModel.setValueAt(author, selectedRow, 0);
            tableModel.setValueAt(year, selectedRow, 1);
            tableModel.setValueAt(title, selectedRow, 2);
            tableModel.setValueAt(genre, selectedRow, 3);

            // Очистка полей ввода
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Удаление записи из базы данных
    private void deleteBook() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите запись для удаления.");
            return;
        }

        String author = table.getValueAt(selectedRow, 0).toString();
        int year = Integer.parseInt(table.getValueAt(selectedRow, 1).toString());
        String title = table.getValueAt(selectedRow, 2).toString();
        String genre = table.getValueAt(selectedRow, 3).toString();

        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM book WHERE author = ? AND publishingYear = ? AND title = ? AND genre = ?");
            statement.setString(1, author);
            statement.setInt(2, year);
            statement.setString(3, title);
            statement.setString(4, genre);

            statement.executeUpdate();
            statement.close();

            // Удаление строки из таблицы
            tableModel.removeRow(selectedRow);

            // Очистка полей ввода
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibraryApp app = new LibraryApp();
            app.setVisible(true);
        });
    }
}