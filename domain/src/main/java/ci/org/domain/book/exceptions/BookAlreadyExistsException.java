package ci.org.domain.book.exceptions;

import ci.org.domain.book.valueobjects.Isbn;

public class BookAlreadyExistsException extends BookException {
    public BookAlreadyExistsException(Isbn isbn) {
        super(String.format("Book with isbn %s is already available", isbn.value()));
    }
}
