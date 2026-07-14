package bookmanagement.service;

import bookmanagement.model.Book;
import bookmanagement.model.BookNotFoundException;
import bookmanagement.model.DuplicateBookException;
import bookmanagement.model.ValidationException;

import java.util.List;

/**
 * Business service contract (DIP).
 */
public interface IBookService {

	List<Book> getAllBooks();

	Book getBookByCode(String bookCode) throws BookNotFoundException;

	void addBook(Book book) throws ValidationException, DuplicateBookException;

	void updateBook(Book book) throws ValidationException, BookNotFoundException;

	void removeBook(String bookCode) throws BookNotFoundException;

	void addBookChangeListener(BookChangeListener listener);

	void removeBookChangeListener(BookChangeListener listener);
}
