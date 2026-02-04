import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;

public class StudentRecordSystem extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField idField, firstNameField, lastNameField, lab1Field, lab2Field, lab3Field, prelimField, attendanceField;
    private JButton addButton, deleteButton, uploadButton, saveChangesButton;
    private final String CSV_FILE_PATH = "MOCK_DATA.csv"; // Target file for saving changes

    public StudentRecordSystem() {
        // UI Setup
        setTitle("Student Record System - Desktop Edition");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 1. Table Setup (Matching the MOCK_DATA structure)
        String[] columns = {"Student ID", "First Name", "Last Name", "Lab 1", "Lab 2", "Lab 3", "Prelim", "Attendance"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 2. Input Panel (South - North portion)
        JPanel inputPanel = new JPanel(new GridLayout(2, 8, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Student Information"));
        
        // Input Fields
        inputPanel.add(new JLabel("ID:"));
        idField = new JTextField(); inputPanel.add(idField);
        inputPanel.add(new JLabel("First Name:"));
        firstNameField = new JTextField(); inputPanel.add(firstNameField);
        inputPanel.add(new JLabel("Last Name:"));
        lastNameField = new JTextField(); inputPanel.add(lastNameField);
        inputPanel.add(new JLabel("Lab 1:"));
        lab1Field = new JTextField(); inputPanel.add(lab1Field);
        inputPanel.add(new JLabel("Lab 2:"));
        lab2Field = new JTextField(); inputPanel.add(lab2Field);
        inputPanel.add(new JLabel("Lab 3:"));
        lab3Field = new JTextField(); inputPanel.add(lab3Field);
        inputPanel.add(new JLabel("Prelim:"));
        prelimField = new JTextField(); inputPanel.add(prelimField);
        inputPanel.add(new JLabel("Attendance:"));
        attendanceField = new JTextField(); inputPanel.add(attendanceField);

        // 3. Control Panel (South - South portion)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        addButton = new JButton("➕ Add Record");
        deleteButton = new JButton("🗑️ Delete Selected");
        uploadButton = new JButton("📂 Upload CSV");
        saveChangesButton = new JButton("💾 Save Changes to CSV");

        controlPanel.add(addButton);
        controlPanel.add(deleteButton);
        controlPanel.add(uploadButton);
        controlPanel.add(saveChangesButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(inputPanel, BorderLayout.NORTH);
        southPanel.add(controlPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        // Action Listeners
        addButton.addActionListener(e -> addStudent());
        deleteButton.addActionListener(e -> deleteStudent());
        uploadButton.addActionListener(e -> uploadCSV());
        saveChangesButton.addActionListener(e -> saveToCSV());
    }

    // CREATE: Add student to the JTable
    private void addStudent() {
        if (idField.getText().isEmpty() || firstNameField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Student ID and First Name are required.");
            return;
        }
        Object[] row = {
            idField.getText(), firstNameField.getText(), lastNameField.getText(),
            lab1Field.getText(), lab2Field.getText(), lab3Field.getText(),
            prelimField.getText(), attendanceField.getText()
        };
        tableModel.addRow(row);
        clearFields();
    }

    // DELETE: Remove student from the JTable
    private void deleteStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
        }
    }

    // UPLOAD: Replace current table data with content from a CSV file
    private void uploadCSV() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                tableModel.setRowCount(0); // Clear table before uploading
                String line;
                boolean isHeader = true;
                while ((line = br.readLine()) != null) {
                    if (isHeader) { isHeader = false; continue; } // Skip CSV header
                    String[] data = line.split(",");
                    if (data.length >= 8) {
                        tableModel.addRow(data);
                    }
                }
                JOptionPane.showMessageDialog(this, "CSV Data uploaded to table successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage());
            }
        }
    }

    // SAVE CHANGES: Overwrite MOCK_DATA.csv with current table content
    private void saveToCSV() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE_PATH))) {
            // Write Header
            bw.write("StudentID,first_name,last_name,LAB WORK 1,LAB WORK 2,LAB WORK 3,PRELIM EXAM,ATTENDANCE GRADE");
            bw.newLine();

            // Write all table rows back to the file
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    Object val = tableModel.getValueAt(i, j);
                    sb.append(val != null ? val.toString() : "");
                    if (j < tableModel.getColumnCount() - 1) sb.append(",");
                }
                bw.write(sb.toString());
                bw.newLine();
            }
            JOptionPane.showMessageDialog(this, "Changes saved successfully to " + CSV_FILE_PATH);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving changes: " + ex.getMessage());
        }
    }

    private void clearFields() {
        idField.setText(""); firstNameField.setText(""); lastNameField.setText("");
        lab1Field.setText(""); lab2Field.setText(""); lab3Field.setText("");
        prelimField.setText(""); attendanceField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentRecordSystem().setVisible(true));
    }
}