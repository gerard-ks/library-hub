package ci.org.domain.book.exceptions;

public class InvalidBookTitleException extends BookException {
   public InvalidBookTitleException(String title) {
       super(String.format("Invalid book title: %s", title));
   }
}
