package ci.org.application.book.requests.commands;

/**
 * Command (DTO) pour marquer un livre comme indisponible.
 */
public record MarkBookAsUnavailableCommand(
        String bookId
) {
    public MarkBookAsUnavailableCommand {
        if (bookId == null || bookId.isBlank()) {
            throw new IllegalArgumentException("Book ID cannot be null or blank");
        }
    }
}

