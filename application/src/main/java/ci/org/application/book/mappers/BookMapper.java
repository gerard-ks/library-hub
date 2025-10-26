package ci.org.application.book.mappers;

import ci.org.application.book.dtos.BookResponse;
import ci.org.domain.book.entities.Book;

/**
 * Mapper pour convertir entre Book (domain) et BookResponse (DTO).
 *
 * Pattern : Anti-Corruption Layer (ACL)
 */
public class BookMapper {

    /**
     * Convertit un Book (domain) en BookResponse (DTO).
     */
    public static BookResponse toResponse(Book book) {
        if (book == null) {
            return null;
        }

        return new BookResponse(
                book.getId().value(),
                book.getIsbn().value(),
                book.getTitle().value(),
                book.getAuthor().value(),
                book.isAvailable()
        );
    }
}