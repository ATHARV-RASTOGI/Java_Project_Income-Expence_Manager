package Project_Java;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ExpenseGUI extends JFrame{
    private JTextField descriptionField,amountField;
    private JComboBox<String> typeComboBox;
    private JLabel totalIncomeLabel,totalExpenseLabel;
    private final DefaultTableModel tableModel;
    private JTable transactionTable;
    private int transactionIdCounter=1;
    private double totalIncome=0;
    private double totalExpense=0;
    private JLabel netBalanceLabel;



    public ExpenseGUI(){
        setTitle("Expence and Income Manager");
        setSize(1000,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel totalPanel=new JPanel(new GridLayout(1,3,10,10));
        totalIncomeLabel=new JLabel("Total Income :0.00");
        totalIncomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalExpenseLabel = new JLabel("Total Expense: 0.00");
        totalExpenseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        netBalanceLabel = new JLabel("Net Balance: 0.00");
        netBalanceLabel.setHorizontalAlignment(SwingConstants.CENTER);

        totalPanel.add(netBalanceLabel);
        totalPanel.add(totalIncomeLabel);
        totalPanel.add(totalExpenseLabel);

        JPanel inputPanel=new JPanel(new FlowLayout(FlowLayout.LEFT, 10 ,10));
        // for the drop down declaration of values
        typeComboBox=new JComboBox<>(new String[]{"Income","Expence"});
        descriptionField=new JTextField(15);
        amountField=new JTextField(8);
        JButton addButton=new JButton("Add Transaction");
        JButton removeButton= new JButton("Remove Selected");

         // for the drop down 
        inputPanel.add(new JLabel("Type"));
        inputPanel.add(typeComboBox);

        //for amount feild
        inputPanel.add(descriptionField);
        inputPanel.add(new JLabel("Amount:"));
        inputPanel.add(amountField);
        inputPanel.add(addButton);
        inputPanel.add(removeButton);


        


        String[] columnNames={"ID","Type","Description","Amount"};
        tableModel =new DefaultTableModel(columnNames,0){
            public boolean isCellEditable(int row,int column){
                return false;
    
            }
        };

        addButton.addActionListener(e -> addTransaction());
        removeButton.addActionListener(e -> removeSelectedTransaction());

        transactionTable=new JTable(tableModel);
        JScrollPane tableScrollPane=new JScrollPane(transactionTable);

        JPanel centerPanel=new JPanel(new BorderLayout(10, 10));
        centerPanel.add(inputPanel,BorderLayout.NORTH);
        centerPanel.add(tableScrollPane,BorderLayout.CENTER);

        setLayout(new BorderLayout(10,10));
        add(totalPanel,BorderLayout.NORTH);
        add(centerPanel,BorderLayout.CENTER);
    }

        private void addTransaction(){
            String type=(String) typeComboBox.getSelectedItem();
            String description = descriptionField.getText().trim();
            String amountText = amountField.getText().trim();

            if(description.isEmpty()||amountText.isEmpty()){
                JOptionPane.showMessageDialog(this,"Please enter description and amount","Input Error",JOptionPane.ERROR_MESSAGE);
                return;
            }

            double amount;
            try {
                amount=Double.parseDouble(amountText);
                if(amount<=0){
                    JOptionPane.showMessageDialog(this, "Amount must be positive.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount format.","Input error",JOptionPane.ERROR_MESSAGE);
                return;
            }

            Vector<Object>row=new Vector<>();
            row.add(transactionIdCounter++);
            row.add(type);
            row.add(description);
            row.add(String.format("%.2f",amount));
            tableModel.addRow(row);

            if("Income".equals(type)){
                totalIncome+=amount;
            }
            else{
                totalExpense+=amount;
            }
            updateTotals();

            descriptionField.setText("");
            amountField.setText("");
        }

        private void removeSelectedTransaction(){
            int selectedRow=transactionTable.getSelectedRow();
            if(selectedRow==-1){
                JOptionPane.showMessageDialog(this, "Please select a transaction to remove.", "Selection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String type = (String) tableModel.getValueAt(selectedRow, 1);
            String amountStr = (String) tableModel.getValueAt(selectedRow, 3);
            double amount = Double.parseDouble(amountStr);

              // Update totals
        if ("Income".equals(type)) {
            totalIncome -= amount;
        } else {
            totalExpense -= amount;
        }
        updateTotals();

        // Remove row from table
        tableModel.removeRow(selectedRow);
    }

    private void updateTotals() {
        totalIncomeLabel.setText(String.format("Total Income: %.2f", totalIncome));
        totalExpenseLabel.setText(String.format("Total Expense: %.2f", totalExpense));
        double netBalance = totalIncome - totalExpense;
        netBalanceLabel.setText(String.format("Net Balance: %.2f", netBalance));
        
        
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(()-> {
            ExpenseGUI manager = new ExpenseGUI();
            manager.setVisible(true);
        });
    }
}

