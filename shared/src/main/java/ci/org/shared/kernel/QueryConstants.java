package ci.org.shared.kernel;

/**
 * Constantes partagées pour les queries avec pagination.
 */
public final class QueryConstants {

    private QueryConstants() {
        throw new UnsupportedOperationException("Cannot instantiate constants class");
    }

    /**
     * Numéro de page par défaut (première page).
     */
    public static final int DEFAULT_PAGE = 0;

    /**
     * Taille de page par défaut.
     */
    public static final int DEFAULT_SIZE = 20;

    /**
     * Taille minimale de page.
     */
    public static final int MIN_SIZE = 1;

    /**
     * Taille maximale de page.
     */
    public static final int MAX_SIZE = 100;

    /**
     * Direction de tri ascendant.
     */
    public static final String SORT_DIRECTION_ASC = "ASC";

    /**
     * Direction de tri descendant.
     */
    public static final String SORT_DIRECTION_DESC = "DESC";

    /**
     * Direction de tri par défaut.
     */
    public static final String DEFAULT_SORT_DIRECTION = SORT_DIRECTION_ASC;


    public static final String PAGE_NEGATIVE_ERROR = "Page cannot be negative";
    public static final String SIZE_OUT_OF_RANGE_ERROR = "Size must be between %d and %d";
    public static final String SORT_BY_BLANK_ERROR = "Sort by cannot be null or blank";
    public static final String SORT_DIRECTION_INVALID_ERROR = "Sort direction must be ASC or DESC";

    /**
     * Vérifie si une direction de tri est valide.
     * @param direction la direction à vérifier
     * @return true si valide (ASC ou DESC)
     */
    public static boolean isValidSortDirection(String direction) {
        return SORT_DIRECTION_ASC.equals(direction) || SORT_DIRECTION_DESC.equals(direction);
    }

    /**
     * Vérifie si la taille de page est valide.
     * @param size la taille à vérifier
     * @return true si entre MIN_SIZE et MAX_SIZE
     */
    public static boolean isValidPageSize(int size) {
        return size >= MIN_SIZE && size <= MAX_SIZE;
    }

    /**
     * Vérifie si le numéro de page est valide.
     * @param page le numéro de page à vérifier
     * @return true si >= 0
     */
    public static boolean isValidPageNumber(int page) {
        return page >= 0;
    }
}
