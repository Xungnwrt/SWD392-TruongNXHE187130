package bookmanagement.service;

/**
 * Observer — notified when the book collection changes.
 */
public interface BookChangeListener {

	void onBooksChanged();
}
