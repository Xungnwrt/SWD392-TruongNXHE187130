package bookmanagement.service;

import bookmanagement.dao.IBookDAO;
import bookmanagement.model.Book;
import bookmanagement.model.BookNotFoundException;
import bookmanagement.model.DuplicateBookException;
import bookmanagement.model.ValidationException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Business Logic Layer — validation + rules. Subject in Observer pattern.
 */
public class BookService implements IBookService {

	private static final int MIN_YEAR = 1980;

	private final IBookDAO bookDAO;
	private final List<BookChangeListener> listeners = new ArrayList<>();

	public BookService(IBookDAO bookDAO) {
		this.bookDAO = bookDAO;
	}

	@Override
	public List<Book> getAllBooks() {
		return bookDAO.findAll();
	}

	@Override
	public Book getBookByCode(String bookCode) throws BookNotFoundException {
		Book book = bookDAO.findByCode(bookCode);
		if (book == null) {
			throw new BookNotFoundException("Book not found: " + bookCode);
		}
		return book;
	}

	@Override
	public void addBook(Book book) throws ValidationException, DuplicateBookException {
		validate(book);
		if (bookDAO.existsByCode(book.getBookCode().trim())) {
			throw new DuplicateBookException("Book code already exists: " + book.getBookCode());
		}
		normalize(book);
		bookDAO.insert(book);
		notifyListeners();
	}

	@Override
	public void updateBook(Book book) throws ValidationException, BookNotFoundException {
		validate(book);
		if (!bookDAO.existsByCode(book.getBookCode().trim())) {
			throw new BookNotFoundException("Cannot update. Book not found: " + book.getBookCode());
		}
		normalize(book);
		bookDAO.update(book);
		notifyListeners();
	}

	@Override
	public void removeBook(String bookCode) throws BookNotFoundException {
		if (bookCode == null || bookCode.trim().isEmpty()) {
			throw new BookNotFoundException("Book code is required for remove.");
		}
		if (!bookDAO.existsByCode(bookCode.trim())) {
			throw new BookNotFoundException("Cannot remove. Book not found: " + bookCode);
		}
		bookDAO.delete(bookCode.trim());
		notifyListeners();
	}

	@Override
	public void addBookChangeListener(BookChangeListener listener) {
		if (listener != null && !listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeBookChangeListener(BookChangeListener listener) {
		listeners.remove(listener);
	}

	private void validate(Book book) throws ValidationException {
		if (book == null) {
			throw new ValidationException("Book data is required.");
		}
		if (isBlank(book.getBookCode())) {
			throw new ValidationException("Book Code is required.");
		}
		if (isBlank(book.getBookName())) {
			throw new ValidationException("Book Name is required.");
		}
		if (isBlank(book.getAuthor())) {
			throw new ValidationException("Author is required.");
		}
		if (isBlank(book.getPublisher())) {
			throw new ValidationException("Publisher is required.");
		}
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		int year = book.getPublishYear();
		if (year < MIN_YEAR || year > currentYear) {
			throw new ValidationException("Publish Year must be between " + MIN_YEAR + " and " + currentYear + ".");
		}
	}

	private void normalize(Book book) {
		book.setBookCode(book.getBookCode().trim());
		book.setBookName(book.getBookName().trim());
		book.setAuthor(book.getAuthor().trim());
		book.setPublisher(book.getPublisher().trim());
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private void notifyListeners() {
		for (BookChangeListener listener : new ArrayList<>(listeners)) {
			listener.onBooksChanged();
		}
	}
}
