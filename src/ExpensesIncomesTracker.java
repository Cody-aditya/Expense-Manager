import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class ExpensesIncomesTracker extends JFrame {
  private DefaultTableModel tableModel;
  private JTable table;
  private JTextField dateField;
  private JTextField descriptionField;
  private JTextField amountField;
  private JComboBox<String> typeCombobox;
  private JButton addButton;
  private JButton saveButton; // New Save Button
  private JLabel balanceLabel;
  private double balance;

  public ExpensesIncomesTracker() {
    try {
      UIManager.setLookAndFeel(new FlatLightLaf());
    } catch (Exception ex) {
      System.err.println("Failed to Set FlatLaf LookAndFeel");
    }

    balance = 0.0;
    tableModel = new DefaultTableModel(new String[]{"Date", "Description", "Amount", "Type"}, 0);
    table = new JTable(tableModel);
    table.setFillsViewportHeight(true);
    table.getTableHeader().setBackground(new Color(44, 62, 80));
    table.getTableHeader().setForeground(Color.WHITE);

    JScrollPane scrollPane = new JScrollPane(table);

    dateField = createStyledTextField(10);
    descriptionField = createStyledTextField(20);
    amountField = createStyledTextField(10);

    typeCombobox = new JComboBox<>(new String[]{"Expense", "Income"});
    typeCombobox.setForeground(Color.WHITE);
    typeCombobox.setBackground(new Color(39, 55, 70));

    addButton = createStyledButton("Add", new Color(34, 167, 240));
    addButton.addActionListener(e -> addEntry());
    addButton.setFont(new Font("Arial", Font.BOLD, 16));

    saveButton = createStyledButton("Save", new Color(0, 128, 0)); // New Save Button
    saveButton.addActionListener(e -> saveEntriesToCSV());
    saveButton.setFont(new Font("Arial", Font.BOLD, 16)); // New Save Button

    balanceLabel = createStyledLabel("Balance: ₹" + balance, new Color(34, 167, 240), 18);

    JPanel inputPanel = new JPanel(new GridBagLayout());
    inputPanel.setBackground(new Color(44, 62, 80));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;

    createAndAddLabelAndField(inputPanel, "Date", dateField, gbc, 0, 0);
    createAndAddLabelAndField(inputPanel, "Description", descriptionField, gbc, 1, 0);
    createAndAddLabelAndField(inputPanel, "Amount", amountField, gbc, 2, 0);
    createAndAddLabelAndComponent(inputPanel, "Type", typeCombobox, gbc, 3, 0);

    // Add the Add button
    gbc.gridy = 0;
    gbc.gridx = 4;
    gbc.gridwidth = 1;
    inputPanel.add(addButton, gbc);

    // Add the Save button
    gbc.gridy = 1;
    gbc.gridx = 4;
    gbc.gridwidth = 1;
    inputPanel.add(saveButton, gbc);

    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    bottomPanel.add(balanceLabel);
    bottomPanel.setBackground(new Color(44, 62, 80));

    setLayout(new BorderLayout());
    add(inputPanel, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);
    add(bottomPanel, BorderLayout.SOUTH);

        setTitle("Expenses Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    class TypeColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String type = (String) value;
            if (type.equals("Income")) {
                setBackground(new Color(46, 204, 113));
            } else if (type.equals("Expense")) {
                setBackground(new Color(231, 76, 60));
            } else {
                setBackground(table.getBackground());
            }

            return this;
        }
    }

    private void addEntry() {
        String date = dateField.getText();
        String description = descriptionField.getText();
        String amountStr = amountField.getText();
        String type = (String) typeCombobox.getSelectedItem();
        double amount;

        if (amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter the Amount", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Amount Format", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (type.equals("Expense")) {
            amount *= -1;
        }

        tableModel.addRow(new Object[]{date, description, amount, type});

        balance += amount;
        balanceLabel.setText("Balance: ₹" + balance);

        clearInputFields();
    }

    private void saveEntriesToCSV() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Save CSV File");
    int userSelection = fileChooser.showSaveDialog(this);

    if (userSelection == JFileChooser.APPROVE_OPTION) {
        File fileToSave = fileChooser.getSelectedFile();

        try (FileWriter writer = new FileWriter(fileToSave + ".csv")) {
            writer.write("Date,Description,Amount,Type\n");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Vector row = (Vector) tableModel.getDataVector().elementAt(i);
                String date = (String) row.elementAt(0);
                String description = (String) row.elementAt(1);
                double amount = (double) row.elementAt(2);
                String type = (String) row.elementAt(3);

                writer.write(date + "," + description + "," + amount + "," + type + "\n");
            }

            // Calculate the total balance
            double totalBalance = 0.0;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                double amount = (double) tableModel.getValueAt(i, 2);
                totalBalance += amount;
            }

            // Append the balance to the CSV
            writer.write("Balance,," + totalBalance + ",");

            writer.close();
            JOptionPane.showMessageDialog(this, "Entries saved to " + fileToSave.getAbsolutePath(), "Save Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving the file", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}


    private void clearInputFields() {
        dateField.setText("");
        descriptionField.setText("");
        amountField.setText("");
        typeCombobox.setSelectedIndex(0);
    }

    private JTextField createStyledTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBackground(new Color(220, 220, 220));
        return textField;
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(backgroundColor);
        button.setFocusPainted(false);
        return button;
    }

    private JLabel createStyledLabel(String text, Color color, int fontSize) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, fontSize));
        label.setForeground(color);
        return label;
    }

    private void createAndAddLabelAndField(JPanel panel, String labelText, JTextField textField, GridBagConstraints gbc, int gridY, int gridX) {
        JLabel label = createStyledLabel(labelText, Color.WHITE, 14);
        gbc.gridy = gridY;
        gbc.gridx = gridX;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(label, gbc);
        gbc.gridx = gridX + 1;
        panel.add(textField, gbc);
    }

    private void createAndAddLabelAndComponent(JPanel panel, String labelText, JComponent component, GridBagConstraints gbc, int gridY, int gridX) {
        JLabel label = createStyledLabel(labelText, Color.WHITE, 14);
        gbc.gridy = gridY;
        gbc.gridx = gridX;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(label, gbc);
        gbc.gridx = gridX + 1;
        panel.add(component, gbc);
    }

    private void createAndAddButton(JPanel panel, JButton button, GridBagConstraints gbc, int gridY, int gridX) {
        gbc.gridy = gridY;
        gbc.gridx = gridX;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(button, gbc);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ExpensesIncomesTracker();
        });
    }
}