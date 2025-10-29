package ci.org.application.loan.requests.queries;

import ci.org.shared.kernel.QueryConstants;

public record SearchLoansQuery(
    String memberId,
    int page,
    int size,
    String sortBy,
    String sortDirection
) {
    private static final String DEFAULT_SORT_BY = "borrowDate";

    public SearchLoansQuery {
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
     * Recherche tous les emprunts actifs.
     */
    public static SearchLoansQuery forActiveLoans() {
        return new SearchLoansQuery(
                null,
                QueryConstants.DEFAULT_PAGE,
                QueryConstants.DEFAULT_SIZE,
                DEFAULT_SORT_BY,
                QueryConstants.SORT_DIRECTION_DESC
        );
    }

    /**
     * Query pour récupérer tous les emprunts en retard avec pagination.
     */
    public static SearchLoansQuery forOverdueLoans() {
        return new SearchLoansQuery(
                null,
                QueryConstants.DEFAULT_PAGE,
                QueryConstants.DEFAULT_SIZE,
                "dueDate",
                QueryConstants.SORT_DIRECTION_ASC
        );
    }

    /**
     * Query pour récupérer les emprunts d'un membre spécifique avec pagination.
     */
    public static SearchLoansQuery forMember(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("Member ID cannot be null or blank");
        }
        return new SearchLoansQuery(
                memberId,
                QueryConstants.DEFAULT_PAGE,
                QueryConstants.DEFAULT_SIZE,
                DEFAULT_SORT_BY,
                QueryConstants.SORT_DIRECTION_DESC
        );
    }

    /**
     * Crée une query par défaut.
     */
    public static SearchLoansQuery ofDefault() {
        return new SearchLoansQuery(
                null,
                QueryConstants.DEFAULT_PAGE,
                QueryConstants.DEFAULT_SIZE,
                DEFAULT_SORT_BY,
                QueryConstants.DEFAULT_SORT_DIRECTION
        );
    }

    /**
     * Vérifie si cette query filtre par membre.
     */
    public boolean hasMemberFilter() {
        return memberId != null && !memberId.isBlank();
    }
}
