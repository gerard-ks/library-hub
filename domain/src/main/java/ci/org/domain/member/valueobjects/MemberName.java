package ci.org.domain.member.valueobjects;

import ci.org.domain.member.exceptions.InvalidMemberNameException;

public record MemberName(String value) {

    private static final int MIN_NAME_LENGTH = 2;

    public MemberName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Member name cannot be null or blank");
        }

        String cleanedName = value.trim();

        if (cleanedName.length() < MIN_NAME_LENGTH) {
            throw new InvalidMemberNameException(value);
        }

        // Assigner la valeur nettoyée
        value = cleanedName;
    }

    /**
     * Factory method pour créer le VO (si tu préfères)
     */
    public static MemberName of(String name) {
        return new MemberName(name);
    }
}
