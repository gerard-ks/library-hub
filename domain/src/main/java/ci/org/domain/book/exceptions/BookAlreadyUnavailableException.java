package ci.org.domain.book.exceptions;

import ci.org.domain.book.valueobjects.BookId;

public class BookAlreadyUnavailableException extends BookException {
    public BookAlreadyUnavailableException(BookId bookId) {
        super(String.format("Book with id %s is already unavailable", bookId.value()));
    }
}
