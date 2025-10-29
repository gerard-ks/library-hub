package ci.org.application.book.requests.queries;

/**
 * Query (DTO) pour récupérer un livre par son ISBN.
 *
 * Input pour GetBookByIsbnUseCase.
 */
public record FindBookQuery(
        String isbn
) {

    public FindBookQuery {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN cannot be null or blank");
        }
    }
}
