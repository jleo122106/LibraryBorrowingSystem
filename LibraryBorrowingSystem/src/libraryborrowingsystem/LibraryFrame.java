package libraryborrowingsystem;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/** Library Book Borrowing System - dashboard + 4 working tabs. */
public class LibraryFrame extends JFrame {

    // ---- colors (same look as the dashboard) ----
    private static final Color NAVY  = new Color(31, 52, 82);
    private static final Color BG    = new Color(245, 247, 251);
    private static final Color BLUE  = new Color(59, 125, 216);
    private static final Color GREEN = new Color(34, 150, 110);
    private static final Color ORANGE = new Color(221, 145, 30);
    private static final Color PURPLE = new Color(130, 80, 180);

    // ---- dashboard ----
    private JLabel lblTotal, lblAvail, lblBorrowed, lblRecords;

    // ---- book management ----
    private JTextField tfBookId, tfTitle, tfAuthor, tfIsbn, tfTotal, tfAvailable, tfSearch;
    private DefaultTableModel bookModel;
    private JTable bookTable;

    // ---- borrow ----
    private JTextField tfStudent, tfBorrowBookId, tfBorrowDate, tfDueDate;
    private DefaultTableModel borrowableModel;
    private JTable borrowableTable;

    // ---- return ----
    private JTextField tfReturnBorrowId, tfReturnDate;
    private DefaultTableModel activeModel;
    private JTable activeTable;

    // ---- records ----
    private DefaultTableModel recordModel;
    private JTable recordTable;
    private TableRowSorter<DefaultTableModel> recordSorter;
    private JComboBox<String> cbFilter;

    public LibraryFrame() {
        super("Library Book Borrowing System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabs.addTab("Dashboard", buildDashboard());
        tabs.addTab("Book Management", buildBookPanel());
        tabs.addTab("Borrow Books", buildBorrowPanel());
        tabs.addTab("Return Books", buildReturnPanel());
        tabs.addTab("Borrowing Records", buildRecordsPanel());
        tabs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) { refreshAll(); }
        });
        add(tabs, BorderLayout.CENTER);

        JLabel footer = new JLabel("  Library Book Borrowing System");
        footer.setBorder(new EmptyBorder(6, 6, 6, 6));
        add(footer, BorderLayout.SOUTH);

        refreshAll();
    }

    // =====================================================================
    //  HEADER + DASHBOARD
    // =====================================================================
    private JPanel buildHeader() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(NAVY);
        p.setBorder(new EmptyBorder(22, 25, 22, 25));
        JLabel t = new JLabel("LIBRARY MANAGEMENT SYSTEM");
        t.setFont(new Font("SansSerif", Font.BOLD, 26));
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel("Book Borrowing and Returning");
        s.setFont(new Font("SansSerif", Font.PLAIN, 14));
        s.setForeground(Color.WHITE);
        p.add(t);
        p.add(Box.createVerticalStrut(6));
        p.add(s);
        return p;
    }

    private JPanel buildDashboard() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(30, 28, 20, 28));

        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(40, 40, 40));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(25));

        JPanel cards = new JPanel(new GridLayout(1, 4, 20, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        lblTotal = new JLabel("0");
        lblAvail = new JLabel("0");
        lblBorrowed = new JLabel("0");
        lblRecords = new JLabel("0");
        cards.add(statCard(lblTotal, "Total Books", BLUE));
        cards.add(statCard(lblAvail, "Available Copies", GREEN));
        cards.add(statCard(lblBorrowed, "Borrowed Copies", ORANGE));
        cards.add(statCard(lblRecords, "Borrowing Records", PURPLE));
        p.add(cards);
        p.add(Box.createVerticalStrut(40));

        JLabel w = new JLabel("Welcome to the Library!");
        w.setFont(new Font("SansSerif", Font.BOLD, 20));
        w.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel d = new JLabel("Manage books, student borrowing, and returns in one application.");
        d.setFont(new Font("SansSerif", Font.PLAIN, 15));
        d.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(w);
        p.add(Box.createVerticalStrut(8));
        p.add(d);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel statCard(JLabel number, String caption, Color color) {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(Color.WHITE);
        c.setBorder(new CompoundBorder(new LineBorder(new Color(230, 233, 240)),
                new EmptyBorder(20, 20, 20, 20)));
        number.setFont(new Font("SansSerif", Font.BOLD, 38));
        number.setForeground(color);
        JLabel cap = new JLabel(caption);
        cap.setFont(new Font("SansSerif", Font.PLAIN, 15));
        c.add(number);
        c.add(Box.createVerticalStrut(8));
        c.add(cap);
        return c;
    }

    // =====================================================================
    //  BOOK MANAGEMENT TAB
    // =====================================================================
    private JPanel buildBookPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(15, 20, 15, 20));

        tfBookId = field(); tfBookId.setEditable(false);
        tfTitle = field(); tfAuthor = field(); tfIsbn = field();
        tfTotal = field();
        tfAvailable = field(); tfAvailable.setEditable(false);

        JPanel form = formPanel("Book Details");
        addRow(form, 0, "Book ID:", tfBookId);
        addRow(form, 1, "Title:", tfTitle);
        addRow(form, 2, "Author:", tfAuthor);
        addRow(form, 3, "ISBN:", tfIsbn);
        addRow(form, 4, "Total Quantity:", tfTotal);
        addRow(form, 5, "Available:", tfAvailable);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        buttons.setOpaque(false);
        buttons.add(button("Add", e -> addBook()));
        buttons.add(button("Update", e -> updateBook()));
        buttons.add(button("Delete", e -> deleteBook()));
        buttons.add(button("Clear", e -> clearBookForm()));

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(form, BorderLayout.CENTER);
        north.add(buttons, BorderLayout.SOUTH);
        root.add(north, BorderLayout.NORTH);

        // search bar + table
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setOpaque(false);
        tfSearch = field();
        searchBar.add(new JLabel("Search title/author:"), BorderLayout.WEST);
        searchBar.add(tfSearch, BorderLayout.CENTER);
        searchBar.add(button("Search", e -> refreshAll()), BorderLayout.EAST);

        bookModel = model("ID", "Title", "Author", "ISBN", "Total Quantity", "Available");
        bookTable = table(bookModel);
        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && bookTable.getSelectedRow() >= 0) {
                int r = bookTable.getSelectedRow();
                tfBookId.setText(str(bookModel.getValueAt(r, 0)));
                tfTitle.setText(str(bookModel.getValueAt(r, 1)));
                tfAuthor.setText(str(bookModel.getValueAt(r, 2)));
                tfIsbn.setText(str(bookModel.getValueAt(r, 3)));
                tfTotal.setText(str(bookModel.getValueAt(r, 4)));
                tfAvailable.setText(str(bookModel.getValueAt(r, 5)));
            }
        });

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(searchBar, BorderLayout.NORTH);
        center.add(new JScrollPane(bookTable), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        return root;
    }

    private void addBook() {
        String title = tfTitle.getText().trim(), author = tfAuthor.getText().trim();
        if (title.isEmpty() || author.isEmpty()) { error("Title and Author are required."); return; }
        Integer total = positiveInt(tfTotal.getText(), "Total Quantity");
        if (total == null) return;
        String sql = "INSERT INTO books(title,author,isbn,total_quantity,available_quantity) VALUES(?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, tfIsbn.getText().trim());
            ps.setInt(4, total);
            ps.setInt(5, total);
            ps.executeUpdate();
            info("Book added.");
            clearBookForm();
            refreshAll();
        } catch (SQLException ex) { error("Database error: " + ex.getMessage()); }
    }

    private void updateBook() {
        if (tfBookId.getText().isEmpty()) { error("Select a book from the table first."); return; }
        String title = tfTitle.getText().trim(), author = tfAuthor.getText().trim();
        if (title.isEmpty() || author.isEmpty()) { error("Title and Author are required."); return; }
        Integer newTotal = positiveInt(tfTotal.getText(), "Total Quantity");
        if (newTotal == null) return;
        int id = Integer.parseInt(tfBookId.getText());
        try (Connection c = DBConnection.getConnection()) {
            int oldTotal, oldAvail;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT total_quantity, available_quantity FROM books WHERE book_id=?")) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) { error("Book no longer exists."); return; }
                oldTotal = rs.getInt(1);
                oldAvail = rs.getInt(2);
            }
            int newAvail = oldAvail + (newTotal - oldTotal);
            if (newAvail < 0) {
                error("Total quantity can't be lower than the copies currently borrowed ("
                        + (oldTotal - oldAvail) + ").");
                return;
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE books SET title=?, author=?, isbn=?, total_quantity=?, available_quantity=? WHERE book_id=?")) {
                ps.setString(1, title);
                ps.setString(2, author);
                ps.setString(3, tfIsbn.getText().trim());
                ps.setInt(4, newTotal);
                ps.setInt(5, newAvail);
                ps.setInt(6, id);
                ps.executeUpdate();
            }
            info("Book updated.");
            clearBookForm();
            refreshAll();
        } catch (SQLException ex) { error("Database error: " + ex.getMessage()); }
    }

    private void deleteBook() {
        if (tfBookId.getText().isEmpty()) { error("Select a book from the table first."); return; }
        int id = Integer.parseInt(tfBookId.getText());
        if (JOptionPane.showConfirmDialog(this, "Delete this book?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection c = DBConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM borrowings WHERE book_id=?")) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    error("This book has borrowing records and can't be deleted.");
                    return;
                }
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM books WHERE book_id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            info("Book deleted.");
            clearBookForm();
            refreshAll();
        } catch (SQLException ex) { error("Database error: " + ex.getMessage()); }
    }

    private void clearBookForm() {
        tfBookId.setText(""); tfTitle.setText(""); tfAuthor.setText("");
        tfIsbn.setText(""); tfTotal.setText(""); tfAvailable.setText("");
        bookTable.clearSelection();
    }

    // =====================================================================
    //  BORROW BOOKS TAB
    // =====================================================================
    private JPanel buildBorrowPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(15, 20, 15, 20));

        tfStudent = field();
        tfBorrowBookId = field();
        tfBorrowDate = field(); tfBorrowDate.setText(LocalDate.now().toString());
        tfDueDate = field(); tfDueDate.setText(LocalDate.now().plusDays(7).toString());

        JPanel form = formPanel("Borrow Details  (dates: yyyy-MM-dd)");
        addRow(form, 0, "Student Name:", tfStudent);
        addRow(form, 1, "Book ID:", tfBorrowBookId);
        addRow(form, 2, "Borrow Date:", tfBorrowDate);
        addRow(form, 3, "Due Date:", tfDueDate);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        buttons.setOpaque(false);
        buttons.add(button("Borrow", e -> borrowBook()));
        buttons.add(button("Clear", e -> clearBorrowForm()));

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(form, BorderLayout.CENTER);
        north.add(buttons, BorderLayout.SOUTH);
        root.add(north, BorderLayout.NORTH);

        borrowableModel = model("Book ID", "Title", "Author", "Available");
        borrowableTable = table(borrowableModel);
        borrowableTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && borrowableTable.getSelectedRow() >= 0)
                tfBorrowBookId.setText(str(borrowableModel.getValueAt(borrowableTable.getSelectedRow(), 0)));
        });
        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setOpaque(false);
        center.add(new JLabel("Available books (click a row to fill the Book ID):"), BorderLayout.NORTH);
        center.add(new JScrollPane(borrowableTable), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        return root;
    }

    private void borrowBook() {
        String student = tfStudent.getText().trim();
        if (student.isEmpty()) { error("Student name is required."); return; }
        Integer bookId = positiveInt(tfBorrowBookId.getText(), "Book ID");
        if (bookId == null) return;
        LocalDate borrow = date(tfBorrowDate.getText(), "Borrow Date");
        if (borrow == null) return;
        LocalDate due = date(tfDueDate.getText(), "Due Date");
        if (due == null) return;
        if (due.isBefore(borrow)) { error("Due date can't be before the borrow date."); return; }

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int avail;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT available_quantity FROM books WHERE book_id=? FOR UPDATE")) {
                    ps.setInt(1, bookId);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) { c.rollback(); error("Book ID " + bookId + " does not exist."); return; }
                    avail = rs.getInt(1);
                }
                if (avail <= 0) { c.rollback(); error("No copies of this book are available."); return; }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO borrowings(student_name,book_id,borrow_date,due_date) VALUES(?,?,?,?)")) {
                    ps.setString(1, student);
                    ps.setInt(2, bookId);
                    ps.setDate(3, Date.valueOf(borrow));
                    ps.setDate(4, Date.valueOf(due));
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE books SET available_quantity = available_quantity - 1 WHERE book_id=?")) {
                    ps.setInt(1, bookId);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            info("Book borrowed successfully.");
            clearBorrowForm();
            refreshAll();
        } catch (SQLException ex) { error("Database error: " + ex.getMessage()); }
    }

    private void clearBorrowForm() {
        tfStudent.setText(""); tfBorrowBookId.setText("");
        tfBorrowDate.setText(LocalDate.now().toString());
        tfDueDate.setText(LocalDate.now().plusDays(7).toString());
        borrowableTable.clearSelection();
    }

    // =====================================================================
    //  RETURN BOOKS TAB
    // =====================================================================
    private JPanel buildReturnPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(15, 20, 15, 20));

        tfReturnBorrowId = field();
        tfReturnDate = field(); tfReturnDate.setText(LocalDate.now().toString());

        JPanel form = formPanel("Return Details  (date: yyyy-MM-dd)");
        addRow(form, 0, "Borrow ID:", tfReturnBorrowId);
        addRow(form, 1, "Return Date:", tfReturnDate);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        buttons.setOpaque(false);
        buttons.add(button("Return", e -> returnBook()));
        buttons.add(button("Clear", e -> {
            tfReturnBorrowId.setText("");
            tfReturnDate.setText(LocalDate.now().toString());
            activeTable.clearSelection();
        }));

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(form, BorderLayout.CENTER);
        north.add(buttons, BorderLayout.SOUTH);
        root.add(north, BorderLayout.NORTH);

        activeModel = model("Borrow ID", "Student", "Book ID", "Title", "Borrow Date", "Due Date", "Status");
        activeTable = table(activeModel);
        activeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && activeTable.getSelectedRow() >= 0)
                tfReturnBorrowId.setText(str(activeModel.getValueAt(activeTable.getSelectedRow(), 0)));
        });
        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setOpaque(false);
        center.add(new JLabel("Books not yet returned (click a row to fill the Borrow ID):"), BorderLayout.NORTH);
        center.add(new JScrollPane(activeTable), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        return root;
    }

    private void returnBook() {
        Integer borrowId = positiveInt(tfReturnBorrowId.getText(), "Borrow ID");
        if (borrowId == null) return;
        LocalDate ret = date(tfReturnDate.getText(), "Return Date");
        if (ret == null) return;

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int bookId;
                LocalDate borrowDate, dueDate;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT book_id, borrow_date, due_date, return_date FROM borrowings WHERE borrow_id=? FOR UPDATE")) {
                    ps.setInt(1, borrowId);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) { c.rollback(); error("Borrow ID " + borrowId + " does not exist."); return; }
                    if (rs.getDate(4) != null) { c.rollback(); error("This book was already returned."); return; }
                    bookId = rs.getInt(1);
                    borrowDate = rs.getDate(2).toLocalDate();
                    dueDate = rs.getDate(3).toLocalDate();
                }
                if (ret.isBefore(borrowDate)) {
                    c.rollback();
                    error("Return date can't be before the borrow date (" + borrowDate + ").");
                    return;
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE borrowings SET return_date=? WHERE borrow_id=?")) {
                    ps.setDate(1, Date.valueOf(ret));
                    ps.setInt(2, borrowId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE books SET available_quantity = available_quantity + 1 WHERE book_id=?")) {
                    ps.setInt(1, bookId);
                    ps.executeUpdate();
                }
                c.commit();
                long late = ChronoUnit.DAYS.between(dueDate, ret);
                info(late > 0 ? "Book returned. It was " + late + " day(s) late."
                              : "Book returned on time.");
            } catch (SQLException ex) { c.rollback(); throw ex; }
            tfReturnBorrowId.setText("");
            tfReturnDate.setText(LocalDate.now().toString());
            refreshAll();
        } catch (SQLException ex) { error("Database error: " + ex.getMessage()); }
    }

    // =====================================================================
    //  BORROWING RECORDS TAB
    // =====================================================================
    private JPanel buildRecordsPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        top.add(new JLabel("Show:"));
        cbFilter = new JComboBox<>(new String[]{"All", "Borrowed", "Overdue", "Returned"});
        cbFilter.addActionListener(e -> applyRecordFilter());
        top.add(cbFilter);
        top.add(button("Refresh", e -> refreshAll()));
        root.add(top, BorderLayout.NORTH);

        recordModel = model("Borrow ID", "Student", "Book ID", "Title",
                "Borrow Date", "Due Date", "Return Date", "Status");
        recordTable = table(recordModel);
        recordSorter = new TableRowSorter<>(recordModel);
        recordTable.setRowSorter(recordSorter);
        recordTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    String s = String.valueOf(v);
                    comp.setForeground(s.equals("Overdue") ? new Color(200, 40, 40)
                            : s.equals("Returned") ? GREEN : Color.BLACK);
                }
                return comp;
            }
        });
        root.add(new JScrollPane(recordTable), BorderLayout.CENTER);
        return root;
    }

    private void applyRecordFilter() {
        String f = (String) cbFilter.getSelectedItem();
        if (f == null || f.equals("All")) recordSorter.setRowFilter(null);
        else recordSorter.setRowFilter(RowFilter.regexFilter("^" + f + "$", 7));
    }

    // =====================================================================
    //  DATA LOADING
    // =====================================================================
    private static final String STATUS_SQL =
            "CASE WHEN b.return_date IS NOT NULL THEN 'Returned' " +
            "WHEN b.due_date < CURDATE() THEN 'Overdue' ELSE 'Borrowed' END";

    private void refreshAll() {
        if (bookModel == null || recordModel == null || activeModel == null || borrowableModel == null) return;
        try (Connection c = DBConnection.getConnection()) {
            loadStats(c);
            loadBooks(c);
            loadBorrowable(c);
            loadActive(c);
            loadRecords(c);
        } catch (SQLException ex) { error("Database error: " + ex.getMessage()); }
    }

    private void loadStats(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            ResultSet rs = st.executeQuery(
                    "SELECT COALESCE(SUM(total_quantity),0), COALESCE(SUM(available_quantity),0) FROM books");
            rs.next();
            int total = rs.getInt(1), avail = rs.getInt(2);
            lblTotal.setText(String.valueOf(total));
            lblAvail.setText(String.valueOf(avail));
            lblBorrowed.setText(String.valueOf(total - avail));
            rs = st.executeQuery("SELECT COUNT(*) FROM borrowings");
            rs.next();
            lblRecords.setText(String.valueOf(rs.getInt(1)));
        }
    }

    private void loadBooks(Connection c) throws SQLException {
        bookModel.setRowCount(0);
        String like = "%" + (tfSearch == null ? "" : tfSearch.getText().trim()) + "%";
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT book_id,title,author,isbn,total_quantity,available_quantity FROM books " +
                "WHERE title LIKE ? OR author LIKE ? ORDER BY book_id")) {
            ps.setString(1, like);
            ps.setString(2, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                bookModel.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getInt(5), rs.getInt(6)});
        }
    }

    private void loadBorrowable(Connection c) throws SQLException {
        borrowableModel.setRowCount(0);
        try (Statement st = c.createStatement()) {
            ResultSet rs = st.executeQuery(
                    "SELECT book_id,title,author,available_quantity FROM books " +
                    "WHERE available_quantity > 0 ORDER BY title");
            while (rs.next())
                borrowableModel.addRow(new Object[]{rs.getInt(1), rs.getString(2),
                        rs.getString(3), rs.getInt(4)});
        }
    }

    private void loadActive(Connection c) throws SQLException {
        activeModel.setRowCount(0);
        try (Statement st = c.createStatement()) {
            ResultSet rs = st.executeQuery(
                    "SELECT b.borrow_id, b.student_name, b.book_id, k.title, b.borrow_date, b.due_date, " +
                    STATUS_SQL + " FROM borrowings b JOIN books k ON k.book_id=b.book_id " +
                    "WHERE b.return_date IS NULL ORDER BY b.due_date");
            while (rs.next())
                activeModel.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getInt(3),
                        rs.getString(4), rs.getDate(5), rs.getDate(6), rs.getString(7)});
        }
    }

    private void loadRecords(Connection c) throws SQLException {
        recordModel.setRowCount(0);
        try (Statement st = c.createStatement()) {
            ResultSet rs = st.executeQuery(
                    "SELECT b.borrow_id, b.student_name, b.book_id, k.title, b.borrow_date, b.due_date, " +
                    "b.return_date, " + STATUS_SQL + " FROM borrowings b " +
                    "JOIN books k ON k.book_id=b.book_id ORDER BY b.borrow_id DESC");
            while (rs.next()) {
                Date rd = rs.getDate(7);
                recordModel.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getInt(3),
                        rs.getString(4), rs.getDate(5), rs.getDate(6), rd == null ? "-" : rd,
                        rs.getString(8)});
            }
        }
    }

    // =====================================================================
    //  SMALL HELPERS
    // =====================================================================
    private JTextField field() {
        JTextField f = new JTextField();
        f.setPreferredSize(new Dimension(300, 30));
        return f;
    }

    private JButton button(String text, java.awt.event.ActionListener a) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.addActionListener(a);
        return b;
    }

    private JPanel formPanel(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
    }

    private void addRow(JPanel p, int row, String label, JComponent comp) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row;
        g.insets = new Insets(5, 10, 5, 10);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; g.weightx = 0;
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        p.add(l, g);
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(comp, g);
    }

    private DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JTable table(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(24);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        return t;
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }

    private Integer positiveInt(String text, String name) {
        try {
            int v = Integer.parseInt(text.trim());
            if (v <= 0) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException ex) {
            error(name + " must be a whole number greater than 0.");
            return null;
        }
    }

    private LocalDate date(String text, String name) {
        try { return LocalDate.parse(text.trim()); }
        catch (DateTimeParseException ex) {
            error(name + " must be in yyyy-MM-dd format (example: 2026-11-15).");
            return null;
        }
    }

    private void error(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
    private void info(String msg)  { JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryFrame().setVisible(true));
    }
}