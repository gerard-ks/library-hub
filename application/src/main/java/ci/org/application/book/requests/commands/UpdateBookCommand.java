package ci.org.application.book.requests.commands;

/**
 * Command (DTO) pour mettre à jour un livre.
 *
 * Input pour UpdateBookUseCase.
 */
public record UpdateBookCommand(
        String bookId,
        String newTitle,
        String newAuthor
) {

    public UpdateBookCommand {
        if (bookId == null || bookId.isBlank()) {
            throw new IllegalArgumentException("Book ID cannot be null or blank");
        }
        if (newTitle == null) {
            throw new IllegalArgumentException("New title cannot be null");
        }
        if (newAuthor == null) {
            throw new IllegalArgumentException("New author cannot be null");
        }
    }
}
