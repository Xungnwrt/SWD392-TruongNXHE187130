package bookmanagement.model;

/**
 * Entity Book — domain model shared across layers.
 */
public class Book {

	private int bookId;
	private String bookCode;
	private String bookName;
	private String author;
	private String publisher;
	private int publishYear;
	private boolean forRent;

	public Book() {
	}

	public Book(String bookCode, String bookName, String author, String publisher, int publishYear, boolean forRent) {
		this.bookCode = bookCode;
		this.bookName = bookName;
		this.author = author;
		this.publisher = publisher;
		this.publishYear = publishYear;
		this.forRent = forRent;
	}

	public int getBookId() {
		return bookId;
	}

	public void setBookId(int bookId) {
		this.bookId = bookId;
	}

	public String getBookCode() {
		return bookCode;
	}

	public void setBookCode(String bookCode) {
		this.bookCode = bookCode;
	}

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public int getPublishYear() {
		return publishYear;
	}

	public void setPublishYear(int publishYear) {
		this.publishYear = publishYear;
	}

	public boolean isForRent() {
		return forRent;
	}

	public void setForRent(boolean forRent) {
		this.forRent = forRent;
	}

	/** Display name in JList (book name). */
	@Override
	public String toString() {
		return bookName == null ? "" : bookName;
	}
}
