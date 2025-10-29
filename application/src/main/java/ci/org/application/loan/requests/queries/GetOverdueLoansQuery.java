package ci.org.application.loan.requests.queries;


import ci.org.shared.kernel.QueryConstants;

/**
 * Query (DTO) pour récupérer tous les emprunts en retard avec pagination.
 *
 * Input pour GetOverdueLoansUseCase.
 */
public record GetOverdueLoansQuery(
        int page,
        int size,
        String sortBy,
        String sortDirection
) {

    private static final String DEFAULT_SORT_BY = "dueDate";

    public GetOverdueLoansQuery {
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

    public static GetOverdueLoansQuery ofDefault() {
        return new GetOverdueLoansQuery(
                QueryConstants.DEFAULT_PAGE,
                QueryConstants.DEFAULT_SIZE,
                DEFAULT_SORT_BY,
                QueryConstants.SORT_DIRECTION_ASC  // ASC pour voir les plus anciens en premier
        );
    }
}