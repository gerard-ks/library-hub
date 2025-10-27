package ci.org.shared.kernel;

/**
 * Value Object représentant une demande de pagination.
 *
 * Abstraction PURE du domain - ne dépend d'AUCUNE technologie.
 * L'infrastructure traduira ce VO vers Spring Pageable, SQL LIMIT/OFFSET, etc.
 */
public record PageRequest(
        int pageNumber,
        int pageSize,
        Sort sort
) {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * Constructeur compact avec validation.
     */
    public PageRequest {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    /**
     * Crée une PageRequest avec tri par défaut.
     */
    public static PageRequest of(int pageNumber, int pageSize) {
        return new PageRequest(pageNumber, pageSize, Sort.unsorted());
    }

    /**
     * Crée une PageRequest avec tri.
     */
    public static PageRequest of(int pageNumber, int pageSize, Sort sort) {
        return new PageRequest(pageNumber, pageSize, sort);
    }

    /**
     * Crée une PageRequest par défaut (page 0, size 20).
     */
    public static PageRequest ofDefault() {
        return new PageRequest(0, DEFAULT_PAGE_SIZE, Sort.unsorted());
    }

    /**
     * Calcule l'offset (pour SQL LIMIT/OFFSET).
     */
    public long getOffset() {
        return (long) pageNumber * pageSize;
    }
}

