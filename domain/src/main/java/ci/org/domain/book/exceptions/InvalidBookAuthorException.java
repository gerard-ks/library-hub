package ci.org.domain.book.exceptions;

public class InvalidBookAuthorException extends BookException {
   public InvalidBookAuthorException(String author) {
       super(String.format("Invalid book author: %s", author));
   }
}
