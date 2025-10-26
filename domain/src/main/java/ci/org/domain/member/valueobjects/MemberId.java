package ci.org.domain.member.valueobjects;

import java.util.UUID;

public record MemberId(String value) {
    public MemberId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Member id cannot be null or blank");
        }
    }

    public static MemberId generate() {
        return new MemberId(UUID.randomUUID().toString());
    }

    public static MemberId of(String value) {
        return new MemberId(value);
    }
}
