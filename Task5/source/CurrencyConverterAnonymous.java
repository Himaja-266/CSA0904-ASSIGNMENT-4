import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public final class CurrencyConverterAnonymous extends JFrame {
    private final JTextField amount = new JTextField();
    private final JComboBox<String> target = new JComboBox<>(new String[]{"EUR", "GBP", "JPY"});
    private final JLabel result = new JLabel("Result appears here");
    private final ActionListener handler = new ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent event) {
            convert();
        }
    };

    public CurrencyConverterAnonymous() {
        super("Currency Converter - Anonymous Inner Class");
        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        JButton convertButton = new JButton("Convert");
        form.add(new JLabel("Amount in USD:"));
        form.add(amount);
        form.add(new JLabel("Target currency:"));
        form.add(target);
        form.add(convertButton);
        form.add(result);
        add(form);
        convertButton.addActionListener(handler);
        amount.addActionListener(handler);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                dispose();
            }
        });
        setSize(380, 180);
        setLocationRelativeTo(null);
    }

    private void convert() {
        try {
            double v = Double.parseDouble(amount.getText().trim());
            double out = v * rate((String) target.getSelectedItem());
            result.setText(String.format("%.2f %s", out, target.getSelectedItem()));
        } catch (NumberFormatException exception) {
            result.setText("Invalid amount");
        }
        JOptionPane.showMessageDialog(this, result.getText(), "Conversion", JOptionPane.INFORMATION_MESSAGE);
    }

    private double rate(String currency) {
        return switch (currency) {
            case "EUR" -> 0.92;
            case "GBP" -> 0.79;
            case "JPY" -> 150.0;
            default -> throw new IllegalArgumentException("Unsupported currency");
        };
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CurrencyConverterAnonymous().setVisible(true));
    }
}
