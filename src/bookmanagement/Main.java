package bookmanagement;

import bookmanagement.presentation.BookManagementFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Application entry point.
 */
public class Main {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ignored) {
				// keep default L&F
			}
			BookManagementFrame frame = new BookManagementFrame();
			frame.setVisible(true);
		});
	}
}
