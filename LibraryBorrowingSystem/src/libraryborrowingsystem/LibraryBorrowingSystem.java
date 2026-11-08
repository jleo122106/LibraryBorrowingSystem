package libraryborrowingsystem;

import javax.swing.SwingUtilities;

public class LibraryBorrowingSystem {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryFrame().setVisible(true));
    }
}