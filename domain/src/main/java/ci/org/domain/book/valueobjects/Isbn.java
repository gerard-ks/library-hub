package ci.org.domain.book.valueobjects;

import ci.org.domain.book.exceptions.InvalidIsbnException;

public record Isbn(String value) {
    private static final int ISBN_10_LENGTH = 10;
    private static final int ISBN_13_LENGTH = 13;

    public Isbn {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ISBN cannot be null or blank");
        }

        // Nettoyer : enlever espaces et tirets
        String cleaned = value.replaceAll("[\\s-]", "");

        // Valider le format (10 ou 13 chiffres)
        if (!cleaned.matches("\\d{10}") && !cleaned.matches("\\d{13}")) {
            throw new InvalidIsbnException(
                    "ISBN must be 10 or 13 digits. Got: " + value
            );
        }

        // Réassigner la valeur nettoyée
        value = cleaned;
    }

    public static Isbn of(String isbn) {
        return new Isbn(isbn);
    }

    public String formatted() {
        if (value.length() == ISBN_10_LENGTH) {
            // Format ISBN-10: 1-23-456789-0
            // Structure : Groupe(1) - Éditeur(2) - Titre(6) - Checksum(1)
            return String.format("%s-%s-%s-%s",
                    value.substring(0, 1),
                    value.substring(1, 3),
                    value.substring(3, 9),
                    value.substring(9)
            );
        } else {
            // Format ISBN-13: 978-1-23-456789-0
            // Structure : Préfixe(3) - Groupe(1) - Éditeur(2) - Titre(6) - Checksum(1)
            return String.format("%s-%s-%s-%s-%s",
                    value.substring(0, 3),
                    value.substring(3, 4),
                    value.substring(4, 6),
                    value.substring(6, 12),
                    value.substring(12)
            );
        }
    }

    public boolean isIsbn10() {
        return value.length() == ISBN_10_LENGTH;
    }

    public boolean isIsbn13() {
        return value.length() == ISBN_13_LENGTH;
    }
}
