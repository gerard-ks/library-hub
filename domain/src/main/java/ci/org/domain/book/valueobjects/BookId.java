package ci.org.domain.book.valueobjects;

import java.util.UUID;

public record BookId(String value) {
    public BookId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Book id cannot be null or blank");
        }
    }

    public static BookId generate() {
        return new BookId(UUID.randomUUID().toString());
    }

    public static BookId of(String value) {
        return new BookId(value);
    }
}
