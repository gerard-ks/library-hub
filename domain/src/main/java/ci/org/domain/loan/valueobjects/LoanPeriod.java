package ci.org.domain.loan.valueobjects;

import ci.org.domain.loan.exceptions.InvalidLoanPeriodException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public record LoanPeriod(LocalDate borrowDate, LocalDate dueDate) {

    /**
     * Constructeur compact avec validation.
     *
     * @throws InvalidLoanPeriodException si la période est invalide
     */
    public LoanPeriod {
        Objects.requireNonNull(borrowDate, "Borrow date cannot be null");
        Objects.requireNonNull(dueDate, "Due date cannot be null");

        if (!dueDate.isAfter(borrowDate)) {
            throw new InvalidLoanPeriodException(borrowDate, dueDate);
        }
    }

    /**
     * Factory method pour créer une période d'emprunt.
     *
     * @param borrowDate la date d'emprunt
     * @param dueDate la date de retour prévue
     * @return une période d'emprunt valide
     */
    public static LoanPeriod of(LocalDate borrowDate, LocalDate dueDate) {
        return new LoanPeriod(borrowDate, dueDate);
    }

    /**
     * Calcule la durée de l'emprunt en jours.
     *
     * @return le nombre de jours entre borrowDate et dueDate
     */
    public long getDurationInDays() {
        return ChronoUnit.DAYS.between(borrowDate, dueDate);
    }
}

