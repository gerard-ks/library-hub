package ci.org.application.loan.requests.queries;

import ci.org.shared.kernel.QueryConstants;

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

    private static final String DEFAULT_SORT_BY = "borrowDate";

    public GetActiveLoansQuery {
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

    public static GetActiveLoansQuery ofDefault() {
        return new GetActiveLoansQuery(
                QueryConstants.DEFAULT_PAGE,
                QueryConstants.DEFAULT_SIZE,
                DEFAULT_SORT_BY,
                QueryConstants.SORT_DIRECTION_DESC
        );
    }
}