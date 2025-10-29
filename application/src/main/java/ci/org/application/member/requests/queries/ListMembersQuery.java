package ci.org.application.member.requests.queries;

import ci.org.shared.kernel.QueryConstants;

/**
 * Query pour récupérer tous les membres avec pagination.
 *
 * Utilise des primitives simples, PAS de concepts Spring.
 */
public record ListMembersQuery(
        int page,
        int size,
        String sortBy,
        String sortDirection
) {

    private static final String DEFAULT_SORT_BY = "name";

    /**
     * Validation.
     */
    public ListMembersQuery {
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
    public static ListMembersQuery ofDefault() {
        return new ListMembersQuery(
                QueryConstants.DEFAULT_PAGE,
                QueryConstants.DEFAULT_SIZE,
                DEFAULT_SORT_BY,
                QueryConstants.DEFAULT_SORT_DIRECTION
        );
    }
}
