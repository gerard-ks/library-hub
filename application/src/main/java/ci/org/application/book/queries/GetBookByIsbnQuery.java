package ci.org.application.book.queries;

/**
 * Query (DTO) pour récupérer un livre par son ISBN.
 *
 * Input pour GetBookByIsbnUseCase.
 */
public record GetBookByIsbnQuery(
        String isbn
) {

    public GetBookByIsbnQuery {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN cannot be null or blank");
        }
    }
}
