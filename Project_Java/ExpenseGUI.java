package Project_Java;

import java.awt.*;
import java.sql.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ExpenseGUI extends JFrame {
    private JTextField descriptionField, amountField, dateField, fromDateField, toDateField;
    private JComboBox<String> typeComboBox;
    private JLabel totalIncomeLabel, totalExpenseLabel, netBalanceLabel;
    private final DefaultTableModel tableModel;
    private JTable transactionTable;
    private int transactionIdCounter = 1;
    private double totalIncome = 0;
    private double totalExpense = 0;

    public ExpenseGUI() {
        setTitle("Expense and Income Manager");
        setSize(1100, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Top panel - totals
        JPanel totalPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        totalIncomeLabel = new JLabel("Total Income: 0.00", SwingConstants.CENTER);
        totalExpenseLabel = new JLabel("Total Expense: 0.00", SwingConstants.CENTER);
        netBalanceLabel = new JLabel("Net Balance: 0.00", SwingConstants.CENTER);
        totalPanel.add(netBalanceLabel);
        totalPanel.add(totalIncomeLabel);
        totalPanel.add(totalExpenseLabel);

        // Input Panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        typeComboBox = new JComboBox<>(new String[]{"Income", "Expense"});
        descriptionField = new JTextField(15);
        amountField = new JTextField(8);
        dateField = new JTextField(10);
        dateField.setToolTipText("Format: yyyy-mm-dd");

        JButton addButton = new JButton("Add Transaction");
        JButton removeButton = new JButton("Remove Selected");
        JButton saveButton = new JButton("Save to Database");

        inputPanel.add(new JLabel("Type"));
        inputPanel.add(typeComboBox);
        inputPanel.add(new JLabel("Description"));
        inputPanel.add(descriptionField);
        inputPanel.add(new JLabel("Amount"));
        inputPanel.add(amountField);
        inputPanel.add(new JLabel("Date (yyyy-mm-dd)"));
        inputPanel.add(dateField);
        inputPanel.add(addButton);
        inputPanel.add(removeButton);
        inputPanel.add(saveButton);

        // Table setup
        String[] columnNames = {"ID", "Date", "Type", "Description", "Amount"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(transactionTable);

        // Date Range Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        fromDateField = new JTextField(10);
        toDateField = new JTextField(10);
        JButton viewButton = new JButton("View By Date");
        filterPanel.add(new JLabel("From (yyyy-mm-dd):"));
        filterPanel.add(fromDateField);
        filterPanel.add(new JLabel("To:"));
        filterPanel.add(toDateField);
        filterPanel.add(viewButton);

        // Panel combining input and table
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        centerPanel.add(filterPanel, BorderLayout.SOUTH);

        // Layout
        setLayout(new BorderLayout(10, 10));
        add(totalPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        // Listeners
        addButton.addActionListener(e -> addTransaction());
        removeButton.addActionListener(e -> removeSelectedTransaction());
        saveButton.addActionListener(e -> saveToDatabase());
        viewButton.addActionListener(e -> viewTransactionsByDate());
    }

    private void addTransaction() {
        String type = (String) typeComboBox.getSelectedItem();
        String description = descriptionField.getText().trim();
        String amountText = amountField.getText().trim();
        String date = dateField.getText().trim();

        if (description.isEmpty() || amountText.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be positive.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount format.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Vector<Object> row = new Vector<>();
        row.add(transactionIdCounter++);
        row.add(date);
        row.add(type);
        row.add(description);
        row.add(String.format("%.2f", amount));
        tableModel.addRow(row);

        if ("Income".equals(type)) totalIncome += amount;
        else totalExpense += amount;

        updateTotals();
        descriptionField.setText("");
        amountField.setText("");
        dateField.setText("");
    }

    private void removeSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a transaction to remove.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String type = (String) tableModel.getValueAt(selectedRow, 2);
        double amount = Double.parseDouble((String) tableModel.getValueAt(selectedRow, 4));

        if ("Income".equals(type)) totalIncome -= amount;
        else totalExpense -= amount;

        tableModel.removeRow(selectedRow);
        updateTotals();
    }

    private void updateTotals() {
        totalIncomeLabel.setText(String.format("Total Income: %.2f", totalIncome));
        totalExpenseLabel.setText(String.format("Total Expense: %.2f", totalExpense));
        netBalanceLabel.setText(String.format("Net Balance: %.2f", totalIncome - totalExpense));
    }

    private void saveToDatabase() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:expenses.db")) {
            String sql = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY, date TEXT, type TEXT, description TEXT, amount REAL)";
            conn.createStatement().execute(sql);

            sql = "INSERT INTO transactions (id, date, type, description, amount) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                pstmt.setInt(1, (int) tableModel.getValueAt(i, 0));
                pstmt.setString(2, (String) tableModel.getValueAt(i, 1));
                pstmt.setString(3, (String) tableModel.getValueAt(i, 2));
                pstmt.setString(4, (String) tableModel.getValueAt(i, 3));
                pstmt.setDouble(5, Double.parseDouble((String) tableModel.getValueAt(i, 4)));
                pstmt.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Transactions saved to database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void viewTransactionsByDate() {
        String fromDate = fromDateField.getText().trim();
        String toDate = toDateField.getText().trim();
        if (fromDate.isEmpty() || toDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both from and to dates.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:expenses.db")) {
            String sql = "SELECT * FROM transactions WHERE date BETWEEN ? AND ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fromDate);
            pstmt.setString(2, toDate);
            ResultSet rs = pstmt.executeQuery();

            // Clear current table
            tableModel.setRowCount(0);
            totalIncome = 0;
            totalExpense = 0;

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("date"));
                row.add(rs.getString("type"));
                row.add(rs.getString("description"));
                row.add(String.format("%.2f", rs.getDouble("amount")));
                tableModel.addRow(row);

                if ("Income".equals(rs.getString("type"))) totalIncome += rs.getDouble("amount");
                else totalExpense += rs.getDouble("amount");
            }

            updateTotals();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ExpenseGUI manager = new ExpenseGUI();
            manager.setVisible(true);
        });
    }
}
