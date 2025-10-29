package ci.org.application.loan.requests.commands;

import java.time.LocalDate;

/**
 * Command (DTO) pour créer un emprunt.
 *
 * Input pour CreateLoanUseCase.
 */
public record CreateLoanCommand(
        String memberId,
        String bookId,
        LocalDate borrowDate,
        LocalDate dueDate
) {

    /**
     * Validation basique (non null).
     * La validation métier est dans le domain (LoanPeriod).
     */
    public CreateLoanCommand {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("Member ID cannot be null or blank");
        }
        if (bookId == null || bookId.isBlank()) {
            throw new IllegalArgumentException("Book ID cannot be null or blank");
        }
        if (borrowDate == null) {
            throw new IllegalArgumentException("Borrow date cannot be null");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date cannot be null");
        }
    }
}
