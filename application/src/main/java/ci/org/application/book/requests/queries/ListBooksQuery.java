package ci.org.application.book.requests.queries;

import ci.org.shared.kernel.QueryConstants;

/**
 * Query (DTO) pour récupérer tous les livres avec pagination.
 *
 * Input pour GetAllBooksUseCase.
 */
public record ListBooksQuery(
        int page,
        int size,
        String sortBy,
        String sortDirection  // "ASC" ou "DESC"
) {

    private static final String DEFAULT_SORT_BY = "title";

    /**
     * Validation.
     */
    public ListBooksQuery {
        if (!QueryConstants.isValidPageNumber(page)) {
            throw new IllegalArgumentException(QueryConstants.PAGE_NEGATIVE_ERROR);
        }
        if (!QueryConstants.isValidPageSize(size)) {
            throw new IllegalArgumentException(
                    String.format(QueryConstants.SIZE_OUT_OF_RANGE_ERROR,
                            QueryConstants.MIN_SIZE,
                            QueryConstants.MAX_SIZE)
            );
        }
        if (sortBy == null || sortBy.isBlank()) {
            throw new IllegalArgumentException(QueryConstants.SORT_BY_BLANK_ERROR);
        }
        if (!QueryConstants.isValidSortDirection(sortDirection)) {
            throw new IllegalArgumentException(QueryConstants.SORT_DIRECTION_INVALID_ERROR);
        }
    }

    /**
     * Crée une query avec valeurs par défaut.
     */
    public static ListBooksQuery ofDefault() {
        return new ListBooksQuery(
                QueryConstants.DEFAULT_PAGE,
                QueryConstants.DEFAULT_SIZE,
                DEFAULT_SORT_BY,
                QueryConstants.DEFAULT_SORT_DIRECTION
        );
    }
}