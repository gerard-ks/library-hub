package ci.org.application.book.commands;

/**
 * Command (DTO) pour créer un livre.
 *
 * Input pour CreateBookUseCase.
 */
public record CreateBookCommand(
        String isbn,
        String title,
        String author
) {

    /**
     * Validation basique (non null).
     * La validation métier est dans le domain (Isbn, BookTitle, BookAuthor).
     */
    public CreateBookCommand {
        if (isbn == null) {
            throw new IllegalArgumentException("ISBN cannot be null");
        }
        if (title == null) {
            throw new IllegalArgumentException("Title cannot be null");
        }
        if (author == null) {
            throw new IllegalArgumentException("Author cannot be null");
        }
    }
}
