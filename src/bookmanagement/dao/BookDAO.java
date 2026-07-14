package bookmanagement.dao;

import bookmanagement.model.Book;
import bookmanagement.util.DBConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC DAO implementation — PreparedStatement + try-with-resources.
 */
public class BookDAO implements IBookDAO {

	private static final String SQL_FIND_ALL =
			"SELECT BookId, BookCode, BookName, Author, Publisher, PublishYear, ForRent "
					+ "FROM Books ORDER BY BookId";

	private static final String SQL_FIND_BY_CODE =
			"SELECT BookId, BookCode, BookName, Author, Publisher, PublishYear, ForRent "
					+ "FROM Books WHERE BookCode = ?";

	private static final String SQL_EXISTS =
			"SELECT COUNT(1) FROM Books WHERE BookCode = ?";

	private static final String SQL_INSERT =
			"INSERT INTO Books (BookCode, BookName, Author, Publisher, PublishYear, ForRent) "
					+ "VALUES (?, ?, ?, ?, ?, ?)";

	private static final String SQL_UPDATE =
			"UPDATE Books SET BookName = ?, Author = ?, Publisher = ?, PublishYear = ?, ForRent = ? "
					+ "WHERE BookCode = ?";

	private static final String SQL_DELETE =
			"DELETE FROM Books WHERE BookCode = ?";

	@Override
	public List<Book> findAll() {
		List<Book> books = new ArrayList<>();
		try (Connection conn = DBConnectionManager.getInstance().getConnection();
			 PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				books.add(mapRow(rs));
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to load books: " + e.getMessage(), e);
		}
		return books;
	}

	@Override
	public Book findByCode(String bookCode) {
		try (Connection conn = DBConnectionManager.getInstance().getConnection();
			 PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_CODE)) {
			ps.setString(1, bookCode);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
				return null;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to find book: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean existsByCode(String bookCode) {
		try (Connection conn = DBConnectionManager.getInstance().getConnection();
			 PreparedStatement ps = conn.prepareStatement(SQL_EXISTS)) {
			ps.setString(1, bookCode);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to check book code: " + e.getMessage(), e);
		}
	}

	@Override
	public void insert(Book book) {
		try (Connection conn = DBConnectionManager.getInstance().getConnection();
			 PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
			ps.setString(1, book.getBookCode());
			ps.setString(2, book.getBookName());
			ps.setString(3, book.getAuthor());
			ps.setString(4, book.getPublisher());
			ps.setInt(5, book.getPublishYear());
			ps.setBoolean(6, book.isForRent());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Failed to insert book: " + e.getMessage(), e);
		}
	}

	@Override
	public void update(Book book) {
		try (Connection conn = DBConnectionManager.getInstance().getConnection();
			 PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
			ps.setString(1, book.getBookName());
			ps.setString(2, book.getAuthor());
			ps.setString(3, book.getPublisher());
			ps.setInt(4, book.getPublishYear());
			ps.setBoolean(5, book.isForRent());
			ps.setString(6, book.getBookCode());
			int rows = ps.executeUpdate();
			if (rows == 0) {
				throw new RuntimeException("No book updated for code: " + book.getBookCode());
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to update book: " + e.getMessage(), e);
		}
	}

	@Override
	public void delete(String bookCode) {
		try (Connection conn = DBConnectionManager.getInstance().getConnection();
			 PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
			ps.setString(1, bookCode);
			int rows = ps.executeUpdate();
			if (rows == 0) {
				throw new RuntimeException("No book deleted for code: " + bookCode);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to delete book: " + e.getMessage(), e);
		}
	}

	private Book mapRow(ResultSet rs) throws SQLException {
		Book book = new Book();
		book.setBookId(rs.getInt("BookId"));
		book.setBookCode(rs.getString("BookCode"));
		book.setBookName(rs.getString("BookName"));
		book.setAuthor(rs.getString("Author"));
		book.setPublisher(rs.getString("Publisher"));
		book.setPublishYear(rs.getInt("PublishYear"));
		book.setForRent(rs.getBoolean("ForRent"));
		return book;
	}
}
