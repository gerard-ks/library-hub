package ci.org.domain.loan.valueobjects;

import java.util.UUID;

public record LoanId(String value) {
    public LoanId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Loan id cannot be null or blank");
        }
    }

    public static LoanId generate() {
        return new LoanId(UUID.randomUUID().toString());
    }

    public static LoanId of(String value) {
        return new LoanId(value);
    }
}
