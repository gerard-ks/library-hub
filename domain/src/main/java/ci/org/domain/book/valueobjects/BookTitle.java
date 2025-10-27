package ci.org.domain.book.valueobjects;

import ci.org.domain.book.exceptions.InvalidBookTitleException;

/**
 * Value Object représentant le titre d'un livre.
 * Garantit la validation (longueur) et la normalisation (trim).
 */
public record BookTitle(String value) {

    private static final int MIN_TITLE_LENGTH = 1;

    public BookTitle {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Book title cannot be null or blank");
        }

        String cleanedTitle = value.trim();

        if (cleanedTitle.length() < MIN_TITLE_LENGTH) {
            throw new InvalidBookTitleException("Book title must be at least " + MIN_TITLE_LENGTH + " character(s)");
        }

        // Assigner la valeur nettoyée
        value = cleanedTitle;
    }

    /**
     * Factory method pour une création facile.
     */
    public static BookTitle of(String title) {
        return new BookTitle(title);
    }
}