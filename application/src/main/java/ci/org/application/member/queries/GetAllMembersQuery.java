package ci.org.application.member.queries;

/**
 * Query pour récupérer tous les membres avec pagination.
 *
 * Utilise des primitives simples, PAS de concepts Spring.
 */
public record GetAllMembersQuery(
        int page,
        int size,
        String sortBy,
        String sortDirection  // "ASC" ou "DESC"
) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "name";
    private static final String DEFAULT_DIRECTION = "ASC";

    /**
     * Validation.
     */
    public GetAllMembersQuery {
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

    /**
     * Crée une query avec valeurs par défaut.
     */
    public static GetAllMembersQuery ofDefault() {
        return new GetAllMembersQuery(DEFAULT_PAGE, DEFAULT_SIZE, DEFAULT_SORT_BY, DEFAULT_DIRECTION);
    }
}
