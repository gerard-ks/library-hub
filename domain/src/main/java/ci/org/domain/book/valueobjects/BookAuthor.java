package ci.org.domain.book.valueobjects;

import ci.org.domain.book.exceptions.InvalidBookAuthorException;

/**
 * Value Object représentant l'auteur d'un livre.
 * Garantit la validation (longueur) et la normalisation (trim).
 */
public record BookAuthor(String value) {

    private static final int MIN_AUTHOR_LENGTH = 1;

    public BookAuthor {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Book author cannot be null or blank");
        }

        String cleanedAuthor = value.trim();

        if (cleanedAuthor.length() < MIN_AUTHOR_LENGTH) {
            throw new InvalidBookAuthorException("Book author must be at least " + MIN_AUTHOR_LENGTH + " character(s)");
        }

        // Assigner la valeur nettoyée
        value = cleanedAuthor;
    }

    /**
     * Factory method pour une création facile.
     */
    public static BookAuthor of(String author) {
        return new BookAuthor(author);
    }
}