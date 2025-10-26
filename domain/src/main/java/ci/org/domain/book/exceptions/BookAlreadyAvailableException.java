package ci.org.domain.book.exceptions;

import ci.org.domain.book.valueobjects.BookId;

public class BookAlreadyAvailableException extends BookException {
    public BookAlreadyAvailableException(BookId bookId) {
        super(String.format("Book with id %s is already available", bookId.value()));
    }
}
