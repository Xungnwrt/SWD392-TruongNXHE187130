package bookmanagement.presentation;

import bookmanagement.controller.BookController;
import bookmanagement.dao.BookDAO;
import bookmanagement.model.Book;
import bookmanagement.service.BookChangeListener;
import bookmanagement.service.BookService;
import bookmanagement.service.IBookService;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Calendar;
import java.util.List;

/**
 * Presentation Layer — Swing UI (matches lab mockup). Observer for list refresh.
 */
public class BookManagementFrame extends JFrame implements BookChangeListener {

	private BookController controller;

	private final DefaultListModel<Book> listModel = new DefaultListModel<>();
	private final JList<Book> lstBooks = new JList<>(listModel);
	private final JTextField txtBookCode = new JTextField(18);
	private final JTextField txtBookName = new JTextField(18);
	private final JTextField txtAuthor = new JTextField(18);
	private final JTextField txtPublisher = new JTextField(18);
	private final JComboBox<Integer> cboPublishYear = new JComboBox<>();
	private final JCheckBox chkForRent = new JCheckBox("For rent");

	private final JButton btnNew = new JButton("New");
	private final JButton btnSave = new JButton("Save");
	private final JButton btnRemove = new JButton("Remove");
	private final JButton btnExit = new JButton("Exit");

	private boolean suppressSelectionEvent;

	public BookManagementFrame() {
		super("Book list");
		initYearCombo();
		buildUi();
		wireEvents();

		IBookService bookService = new BookService(new BookDAO());
		controller = new BookController(this, bookService);
		bookService.addBookChangeListener(this);
		controller.initialize();
	}

	private void initYearCombo() {
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		for (int year = currentYear; year >= 1980; year--) {
			cboPublishYear.addItem(year);
		}
	}

	private void buildUi() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout(8, 8));

		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.setBorder(BorderFactory.createTitledBorder("Book list"));
		lstBooks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listPanel.add(new JScrollPane(lstBooks), BorderLayout.CENTER);

		JPanel formPanel = new JPanel(new GridBagLayout());
		formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		addFormRow(formPanel, gbc, 0, "Book Code:", txtBookCode);
		addFormRow(formPanel, gbc, 1, "Book Name:", txtBookName);
		addFormRow(formPanel, gbc, 2, "Author:", txtAuthor);
		addFormRow(formPanel, gbc, 3, "Publisher:", txtPublisher);
		addFormRow(formPanel, gbc, 4, "Published year:", cboPublishYear);

		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 2;
		formPanel.add(chkForRent, gbc);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(btnNew);
		buttonPanel.add(btnSave);
		buttonPanel.add(btnRemove);
		buttonPanel.add(btnExit);

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(formPanel, BorderLayout.CENTER);
		rightPanel.add(buttonPanel, BorderLayout.SOUTH);

		add(listPanel, BorderLayout.WEST);
		add(rightPanel, BorderLayout.CENTER);

		listPanel.setPreferredSize(new java.awt.Dimension(220, 320));
		pack();
		setLocationRelativeTo(null);
	}

	private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		panel.add(new JLabel(label), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(field, gbc);
	}

	private void wireEvents() {
		btnNew.addActionListener(e -> controller.onNew());
		btnSave.addActionListener(e -> controller.onSave());
		btnRemove.addActionListener(e -> controller.onRemove());
		btnExit.addActionListener(e -> controller.onExit());

		lstBooks.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && !suppressSelectionEvent) {
				controller.onBookSelected(lstBooks.getSelectedIndex());
			}
		});
	}

	@Override
	public void onBooksChanged() {
		// Keep reload synchronous on EDT so Controller can selectFirstBook right after Remove
		if (SwingUtilities.isEventDispatchThread()) {
			controller.reloadBooks();
		} else {
			SwingUtilities.invokeLater(() -> controller.reloadBooks());
		}
	}

	public void refreshBookList(List<Book> books) {
		String selectedCode = null;
		Book current = lstBooks.getSelectedValue();
		if (current != null) {
			selectedCode = current.getBookCode();
		}

		suppressSelectionEvent = true;
		listModel.clear();
		if (books != null) {
			for (Book book : books) {
				listModel.addElement(book);
			}
		}
		suppressSelectionEvent = false;

		if (selectedCode != null) {
			selectBookByCode(selectedCode);
		}
	}

	public void selectFirstBook() {
		if (listModel.isEmpty()) {
			clearForm();
			return;
		}
		suppressSelectionEvent = true;
		lstBooks.setSelectedIndex(0);
		suppressSelectionEvent = false;
		controller.onBookSelected(0);
	}

	public void selectBookByCode(String bookCode) {
		if (bookCode == null) {
			return;
		}
		for (int i = 0; i < listModel.size(); i++) {
			Book book = listModel.get(i);
			if (bookCode.equalsIgnoreCase(book.getBookCode())) {
				suppressSelectionEvent = true;
				lstBooks.setSelectedIndex(i);
				suppressSelectionEvent = false;
				controller.onBookSelected(i);
				return;
			}
		}
	}

	public void clearListSelection() {
		suppressSelectionEvent = true;
		lstBooks.clearSelection();
		suppressSelectionEvent = false;
	}

	public void clearForm() {
		txtBookCode.setText("");
		txtBookName.setText("");
		txtAuthor.setText("");
		txtPublisher.setText("");
		if (cboPublishYear.getItemCount() > 0) {
			cboPublishYear.setSelectedIndex(0);
		}
		chkForRent.setSelected(false);
	}

	public void fillForm(Book book) {
		if (book == null) {
			clearForm();
			return;
		}
		txtBookCode.setText(book.getBookCode());
		txtBookName.setText(book.getBookName());
		txtAuthor.setText(book.getAuthor());
		txtPublisher.setText(book.getPublisher());
		cboPublishYear.setSelectedItem(book.getPublishYear());
		chkForRent.setSelected(book.isForRent());
	}

	public Book getFormBook() {
		Book book = new Book();
		book.setBookCode(txtBookCode.getText());
		book.setBookName(txtBookName.getText());
		book.setAuthor(txtAuthor.getText());
		book.setPublisher(txtPublisher.getText());
		Integer year = (Integer) cboPublishYear.getSelectedItem();
		book.setPublishYear(year == null ? 0 : year);
		book.setForRent(chkForRent.isSelected());
		return book;
	}

	public Book getSelectedBook() {
		return lstBooks.getSelectedValue();
	}

	public void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public boolean confirm(String message) {
		int result = JOptionPane.showConfirmDialog(this, message, "Confirm",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		return result == JOptionPane.YES_OPTION;
	}

	public void setBookCodeEditable(boolean editable) {
		txtBookCode.setEditable(editable);
		txtBookCode.setEnabled(editable);
	}
}
