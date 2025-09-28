package ui;

import model.Transaction;
import utils.DatabaseHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.plot.PiePlot;

public class MainFrame extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;

    public MainFrame() {
        setTitle(" EXPENSE TRACKER ");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== Header =====
        JLabel header = new JLabel("Expense Tracker", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 28));
        header.setOpaque(true);
        header.setBackground(new Color(65, 105, 225)); // Royal Blue
        header.setForeground(Color.WHITE);
        add(header, BorderLayout.NORTH);

        // ===== Table =====
        String[] columns = {"Type", "Category", "Amount", "Date"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);

        // Custom Renderer for Income/Expense coloring
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 0) { // "Type" column
                    String type = value.toString();
                    if (type.equalsIgnoreCase("Income")) {
                        c.setForeground(new Color(0, 128, 0)); // Green
                    } else {
                        c.setForeground(Color.RED); // Red
                    }
                } else {
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        };
        table.getColumnModel().getColumn(0).setCellRenderer(renderer);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // ===== Buttons =====
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton addButton = new JButton("➕ Add Transaction");
        addButton.setBackground(new Color(60, 179, 113)); // MediumSeaGreen
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);

        JButton summaryButton = new JButton("📊 View Summary");
        summaryButton.setBackground(new Color(255, 165, 0)); // Orange
        summaryButton.setForeground(Color.WHITE);
        summaryButton.setFocusPainted(false);

        buttonPanel.add(addButton);
        buttonPanel.add(summaryButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // ===== Load Data on Startup =====
        loadTableData();

        // ===== Button Actions =====
        addButton.addActionListener(e -> addTransaction());
        summaryButton.addActionListener(e -> showSummary());

        setVisible(true);
    }

    // ===== Add Transaction =====
    private void addTransaction() {
        String type = JOptionPane.showInputDialog(this, "Enter type (Income/Expense):");
        if (type == null || type.trim().isEmpty()) return;

        String category = JOptionPane.showInputDialog(this, "Enter category:");
        if (category == null || category.trim().isEmpty()) return;

        double amount;
        try {
            amount = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter amount:"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Invalid amount!");
            return;
        }

        LocalDate date = LocalDate.now();

        Transaction t = new Transaction(type, category, amount, date);
        DatabaseHandler.saveTransaction(t);

        tableModel.addRow(new Object[]{t.getType(), t.getCategory(), t.getAmount(), t.getDate()});
        JOptionPane.showMessageDialog(this, "✅ Transaction Added!");
    }

    // ===== Load Table Data =====
    private void loadTableData() {
        tableModel.setRowCount(0); // clear old rows
        List<Transaction> transactions = DatabaseHandler.loadTransactions();
        for (Transaction t : transactions) {
            tableModel.addRow(new Object[]{t.getType(), t.getCategory(), t.getAmount(), t.getDate()});
        }
    }

    // ===== Show Summary =====
    private void showSummary() {
        List<Transaction> transactions = DatabaseHandler.loadTransactions();
        double income = 0, expense = 0;

        for (Transaction t : transactions) {
            if (t.getType().equalsIgnoreCase("Income")) {
                income += t.getAmount();
            } else {
                expense += t.getAmount();
            }
        }

        double balance = income - expense;

        // === Pie Chart Dataset ===
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Income 💵", income);
        dataset.setValue("Expense 💸", expense);

        // === Create Pie Chart ===
        JFreeChart chart = ChartFactory.createPieChart(
                "Financial Summary",   // chart title
                dataset,               // dataset
                true,                  // legend
                true,                  // tooltips
                false                  // URLs
        );

        // Beautify Chart
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Income 💵", new Color(60, 179, 113)); // Green
        plot.setSectionPaint("Expense 💸", new Color(220, 20, 60)); // Red
        plot.setBackgroundPaint(Color.WHITE);

        // Show Chart in Frame
        ChartPanel chartPanel = new ChartPanel(chart);
        JDialog chartDialog = new JDialog(this, "📊 Summary Chart", true);
        chartDialog.setSize(500, 400);
        chartDialog.setLocationRelativeTo(this);
        chartDialog.setContentPane(chartPanel);
        chartDialog.setVisible(true);

        // Also show text summary
        JOptionPane.showMessageDialog(this,
                "💵 Income: " + income +
                        "\n💸 Expense: " + expense +
                        "\n💲 Balance: " + balance,
                "📊 Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    // ===== Main Entry =====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
