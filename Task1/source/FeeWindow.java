import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

public final class FeeWindow extends JFrame {
    private final FeeService service;
    private final JTextField studentIdField = new JTextField(18);
    private final JTextField amountField = new JTextField(18);
    private final JButton payButton = new JButton("Pay");
    private final JButton receiptButton = new JButton("Receipt");
    private final JButton clearButton = new JButton("Clear");

    public FeeWindow(FeeService service) {
        super("Fee Payment");
        this.service = service;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        JPanel fields = new JPanel(new GridLayout(2, 2, 8, 8));
        fields.add(new JLabel("Student ID:"));
        fields.add(studentIdField);
        fields.add(new JLabel("Amount:"));
        fields.add(amountField);

        JPanel buttons = new JPanel();
        buttons.add(payButton);
        buttons.add(receiptButton);
        buttons.add(clearButton);

        add(fields, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        payButton.addActionListener(event -> pay());
        receiptButton.addActionListener(event -> showReceipt());
        clearButton.addActionListener(event -> clearFields());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                dispose();
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void pay() {
        final String studentId = studentIdField.getText();
        final BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.getText().trim());
        } catch (NumberFormatException exception) {
            showError("Enter a valid amount.");
            return;
        }

        payButton.setEnabled(false);
        new SwingWorker<FeeReceipt, Void>() {
            @Override
            protected FeeReceipt doInBackground() throws SQLException {
                return service.pay(studentId, amount);
            }

            @Override
            protected void done() {
                payButton.setEnabled(true);
                try {
                    FeeReceipt receipt = get();
                    JOptionPane.showMessageDialog(FeeWindow.this,
                            "Payment received: " + receipt.amount() + " for " + receipt.studentId(),
                            "Payment complete", JOptionPane.INFORMATION_MESSAGE);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("Payment was interrupted.");
                } catch (ExecutionException exception) {
                    showError(messageFor(exception.getCause()));
                }
            }
        }.execute();
    }

    private void showReceipt() {
        String studentId = studentIdField.getText();
        receiptButton.setEnabled(false);
        new SwingWorker<Optional<FeeReceipt>, Void>() {
            @Override
            protected Optional<FeeReceipt> doInBackground() throws SQLException {
                return service.latestReceipt(studentId);
            }

            @Override
            protected void done() {
                receiptButton.setEnabled(true);
                try {
                    Optional<FeeReceipt> result = get();
                    result.ifPresentOrElse(
                            receipt -> JOptionPane.showMessageDialog(FeeWindow.this,
                                    "Latest payment: " + receipt.amount() + " at " + receipt.paidAt(),
                                    "Receipt", JOptionPane.INFORMATION_MESSAGE),
                            () -> JOptionPane.showMessageDialog(FeeWindow.this,
                                    "No payment found for this student.",
                                    "Receipt", JOptionPane.INFORMATION_MESSAGE));
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("Receipt lookup was interrupted.");
                } catch (ExecutionException exception) {
                    showError(messageFor(exception.getCause()));
                }
            }
        }.execute();
    }

    private void clearFields() {
        studentIdField.setText("");
        amountField.setText("");
    }

    private String messageFor(Throwable cause) {
        return cause instanceof IllegalArgumentException
                ? cause.getMessage()
                : "Payment failed: " + cause.getMessage();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() ->
                new FeeWindow(new FeeService(new InMemoryFeeRepository())).setVisible(true));
    }
}
