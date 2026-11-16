import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.concurrent.ExecutionException;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public final class MainFrame extends JFrame {
    private final RegistrationPanel registrationPanel;

    public MainFrame(RegistrationService service, String connectionNotice) {
        super("Event Registration");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        registrationPanel = new RegistrationPanel(service, connectionNotice);
        setJMenuBar(createMenuBar());
        add(registrationPanel, BorderLayout.CENTER);
        setSize(900, 600);
        setLocationRelativeTo(null);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportItem = new JMenuItem("Export CSV");
        exportItem.addActionListener(event -> registrationPanel.exportCsv());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(event -> dispose());
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu viewMenu = new JMenu("View");
        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.addActionListener(event -> registrationPanel.refresh());
        viewMenu.add(refreshItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(event -> JOptionPane.showMessageDialog(this,
                "Event Registration Desk", "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private void setService(RegistrationService service, String notice) {
        registrationPanel.setService(service, notice);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            MainFrame frame = new MainFrame(
                    new RegistrationService(new InMemoryRegistrationDao()),
                    "Connecting to configured database...");
            frame.setVisible(true);
            new SwingWorker<Database.DaoSelection, Void>() {
                @Override
                protected Database.DaoSelection doInBackground() {
                    return Database.createDao();
                }

                @Override
                protected void done() {
                    try {
                        Database.DaoSelection selection = get();
                        frame.setService(new RegistrationService(selection.dao()), selection.notice());
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        JOptionPane.showMessageDialog(frame, "Database selection was interrupted.",
                                "Startup error", JOptionPane.ERROR_MESSAGE);
                    } catch (ExecutionException exception) {
                        Throwable cause = exception.getCause();
                        JOptionPane.showMessageDialog(frame,
                                cause == null ? "Could not select a data source." : cause.getMessage(),
                                "Startup error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });
    }
}
