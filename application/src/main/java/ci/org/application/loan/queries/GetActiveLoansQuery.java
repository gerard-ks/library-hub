package ci.org.application.loan.queries;

/**
 * Query (DTO) pour récupérer tous les emprunts actifs avec pagination.
 *
 * Input pour GetActiveLoansUseCase.
 */
public record GetActiveLoansQuery(
        int page,
        int size,
        String sortBy,
        String sortDirection
) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "borrowDate";
    private static final String DEFAULT_DIRECTION = "DESC";

    public GetActiveLoansQuery {
        if (page < 0) {
            throw new IllegalArgumentException("Page cannot be negative");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
        if (sortBy == null || sortBy.isBlank()) {
            throw new IllegalArgumentException("Sort by cannot be null or blank");
        }
        if (!sortDirection.equals("ASC") && !sortDirection.equals("DESC")) {
            throw new IllegalArgumentException("Sort direction must be ASC or DESC");
        }
    }

    public static GetActiveLoansQuery ofDefault() {
        return new GetActiveLoansQuery(DEFAULT_PAGE, DEFAULT_SIZE, DEFAULT_SORT_BY, DEFAULT_DIRECTION);
    }
}

