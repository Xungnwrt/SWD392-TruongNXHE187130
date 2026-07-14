package bookmanagement.dao;

import bookmanagement.model.Book;

import java.util.List;

/**
 * DAO contract — Data Access Layer abstraction (DIP).
 */
public interface IBookDAO {

	List<Book> findAll();

	Book findByCode(String bookCode);

	boolean existsByCode(String bookCode);

	void insert(Book book);

	void update(Book book);

	void delete(String bookCode);
}
