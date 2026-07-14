package bookmanagement.controller;

import bookmanagement.model.Book;
import bookmanagement.model.BookNotFoundException;
import bookmanagement.model.DuplicateBookException;
import bookmanagement.model.ValidationException;
import bookmanagement.presentation.BookManagementFrame;
import bookmanagement.service.IBookService;

import java.util.ArrayList;
import java.util.List;

/**
 * Application/Controller layer — coordinates UI events, no business rules / no JDBC.
 */
public class BookController {

	private final BookManagementFrame view;
	private final IBookService bookService;
	private List<Book> books = new ArrayList<>();
	private boolean creatingNew;

	public BookController(BookManagementFrame view, IBookService bookService) {
		this.view = view;
		this.bookService = bookService;
	}

	public void initialize() {
		creatingNew = false;
		reloadBooks();
		if (!books.isEmpty()) {
			view.selectFirstBook();
		} else {
			view.clearForm();
			view.setBookCodeEditable(true);
		}
	}

	public void onNew() {
		creatingNew = true;
		view.clearForm();
		view.setBookCodeEditable(true);
		view.clearListSelection();
	}

	public void onSave() {
		Book formBook = view.getFormBook();
		try {
			if (creatingNew || !bookExistsInList(formBook.getBookCode())) {
				bookService.addBook(formBook);
				creatingNew = false;
				view.setBookCodeEditable(false);
				view.selectBookByCode(formBook.getBookCode());
			} else {
				bookService.updateBook(formBook);
				view.selectBookByCode(formBook.getBookCode());
			}
		} catch (ValidationException | DuplicateBookException | BookNotFoundException e) {
			view.showError(e.getMessage());
		} catch (RuntimeException e) {
			view.showError(e.getMessage());
		}
	}

	public void onRemove() {
		Book selected = view.getSelectedBook();
		if (selected == null) {
			view.showError("Please select a book to remove.");
			return;
		}
		if (!view.confirm("Are you sure you want to remove this book?")) {
			return;
		}
		try {
			bookService.removeBook(selected.getBookCode());
			creatingNew = false;
			// Observer refreshes list; then auto-select first (lab requirement)
			if (!books.isEmpty()) {
				view.selectFirstBook();
			} else {
				view.clearForm();
				view.setBookCodeEditable(true);
			}
		} catch (BookNotFoundException e) {
			view.showError(e.getMessage());
		} catch (RuntimeException e) {
			view.showError(e.getMessage());
		}
	}

	public void onExit() {
		view.dispose();
		System.exit(0);
	}

	public void onBookSelected(int index) {
		if (index < 0 || index >= books.size()) {
			return;
		}
		creatingNew = false;
		Book book = books.get(index);
		view.fillForm(book);
		view.setBookCodeEditable(false);
	}

	/** Called by the view after Observer notification (or by initialize). */
	public void reloadBooks() {
		books = bookService.getAllBooks();
		view.refreshBookList(books);
	}

	public List<Book> getBooks() {
		return books;
	}

	private boolean bookExistsInList(String bookCode) {
		if (bookCode == null) {
			return false;
		}
		String code = bookCode.trim();
		for (Book book : books) {
			if (code.equalsIgnoreCase(book.getBookCode())) {
				return true;
			}
		}
		return false;
	}
}
