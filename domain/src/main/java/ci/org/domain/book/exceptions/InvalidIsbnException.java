package ci.org.domain.book.exceptions;

public class InvalidIsbnException extends BookException{
    public InvalidIsbnException(String isbn) {
        super(String.format("Invalid ISBN: %s", isbn));
    }
}
