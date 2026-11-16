import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

public final class LibrarySearch extends JFrame {
    private record Book(String title, String author) {
    }

    private static final int PAGE_SIZE = 8;
    private final List<Book> catalog = sampleBooks();
    private final List<Book> matches = new ArrayList<>();
    private final JTextField queryField = new JTextField(24);
    private final JComboBox<String> searchField = new JComboBox<>(new String[]{"Title", "Author", "Both"});
    private final DefaultListModel<String> resultModel = new DefaultListModel<>();
    private final JList<String> results = new JList<>(resultModel);
    private final JLabel status = new JLabel("Enter a title or author.");
    private final JButton previousButton = new JButton("Prev");
    private final JButton nextButton = new JButton("Next");
    private int page;

    public LibrarySearch() {
        super("Library Search");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JLabel title = new JLabel("Library catalog search", JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(20f));

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton searchButton = new JButton("Search");
        searchRow.add(queryField);
        searchRow.add(searchField);
        searchRow.add(searchButton);

        results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton reserveButton = new JButton("Reserve");
        footer.add(previousButton);
        footer.add(nextButton);
        footer.add(reserveButton);
        footer.add(status);

        JPanel north = new JPanel(new BorderLayout());
        north.add(title, BorderLayout.NORTH);
        north.add(searchRow, BorderLayout.SOUTH);
        matches.addAll(catalog);
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(results), BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        searchButton.addActionListener(this::search);
        queryField.addActionListener(this::search);
        previousButton.addActionListener(event -> changePage(-1));
        nextButton.addActionListener(event -> changePage(1));
        reserveButton.addActionListener(this::reserve);
        searchField.addActionListener(this::search);

        setSize(650, 420);
        setLocationRelativeTo(null);
        renderPage();
    }

    private static List<Book> sampleBooks() {
        return List.of(
                new Book("Effective Java", "Joshua Bloch"),
                new Book("Clean Code", "Robert C. Martin"),
                new Book("The Pragmatic Programmer", "David Thomas"),
                new Book("Design Patterns", "Erich Gamma"),
                new Book("Java Concurrency in Practice", "Brian Goetz"),
                new Book("Refactoring", "Martin Fowler"),
                new Book("Head First Java", "Kathy Sierra"),
                new Book("Algorithms", "Robert Sedgewick"),
                new Book("Domain-Driven Design", "Eric Evans"),
                new Book("The Mythical Man-Month", "Frederick Brooks"),
                new Book("Code Complete", "Steve McConnell"),
                new Book("Introduction to Algorithms", "Thomas Cormen"));
    }

    private void search(ActionEvent event) {
        String query = queryField.getText().trim().toLowerCase(java.util.Locale.ROOT);
        String field = (String) searchField.getSelectedItem();
        matches.clear();
        for (Book book : catalog) {
            boolean titleMatch = book.title().toLowerCase(java.util.Locale.ROOT).contains(query);
            boolean authorMatch = book.author().toLowerCase(java.util.Locale.ROOT).contains(query);
            if (query.isEmpty()
                    || ("Title".equals(field) && titleMatch)
                    || ("Author".equals(field) && authorMatch)
                    || ("Both".equals(field) && (titleMatch || authorMatch))) {
                matches.add(book);
            }
        }
        page = 0;
        renderPage();
    }

    private void changePage(int delta) {
        int lastPage = Math.max(0, (matches.size() - 1) / PAGE_SIZE);
        page = Math.max(0, Math.min(lastPage, page + delta));
        renderPage();
    }

    private void renderPage() {
        resultModel.clear();
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, matches.size());
        for (int i = start; i < end; i++) {
            Book book = matches.get(i);
            resultModel.addElement(book.title() + " - " + book.author());
        }
        previousButton.setEnabled(page > 0);
        nextButton.setEnabled(end < matches.size());
        status.setText(matches.size() + " results | page " + (page + 1));
    }

    private void reserve(ActionEvent event) {
        String selection = results.getSelectedValue();
        status.setText(selection == null ? "Select a book to reserve." : "Reservation requested: " + selection);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibrarySearch().setVisible(true));
    }
}
