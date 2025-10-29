package ci.org.application.loan.requests.queries;

import ci.org.shared.kernel.QueryConstants;

/**
 * Query (DTO) pour récupérer tous les emprunts d'un membre avec pagination.
 *
 * Input pour GetLoansByMemberIdUseCase.
 */
public record GetLoansByMemberIdQuery(
        String memberId,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {

    private static final String DEFAULT_SORT_BY = "borrowDate";
    private static final String MEMBER_ID_BLANK_ERROR = "Member ID cannot be null or blank";

    public GetLoansByMemberIdQuery {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException(MEMBER_ID_BLANK_ERROR);
        }
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

    public static GetLoansByMemberIdQuery ofDefault(String memberId) {
        return new GetLoansByMemberIdQuery(
                memberId,
                QueryConstants.DEFAULT_PAGE,
                QueryConstants.DEFAULT_SIZE,
                DEFAULT_SORT_BY,
                QueryConstants.SORT_DIRECTION_DESC
        );
    }
}