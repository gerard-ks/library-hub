package ci.org.application.book.responses;

/**
 * DTO de réponse pour un livre.
 *
 * Output de tous les Use Cases Book.
 */
public record BookResponse(
        String id,
        String isbn,
        String title,
        String author,
        boolean available
) {
}

