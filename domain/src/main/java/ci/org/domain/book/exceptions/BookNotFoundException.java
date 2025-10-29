package ci.org.domain.book.exceptions;

public class BookNotFoundException extends BookException {
    public BookNotFoundException(String bookId) {
        super(String.format("Book with id %s not found", bookId));
    }
}
