package ci.org.application.book.commands;

/**
 * Command (DTO) pour marquer un livre comme disponible.
 */
public record MarkBookAsAvailableCommand(
        String bookId
) {
    public MarkBookAsAvailableCommand {
        if (bookId == null || bookId.isBlank()) {
            throw new IllegalArgumentException("Book ID cannot be null or blank");
        }
    }
}
