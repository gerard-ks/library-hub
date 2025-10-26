package ci.org.domain.book.exceptions;

import ci.org.shared.exception.DomainException;

public class BookException extends DomainException {
    protected BookException(String message) {
        super(message);
    }

    protected BookException(String message, Throwable cause) {
        super(message, cause);
    }
}
