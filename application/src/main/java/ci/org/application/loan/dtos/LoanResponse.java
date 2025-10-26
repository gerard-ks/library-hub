package ci.org.application.loan.dtos;

import java.time.LocalDate;

/**
 * DTO de réponse pour un emprunt.
 *
 * Output de tous les Use Cases Loan.
 */
public record LoanResponse(
        String id,
        String memberId,
        String bookId,
        LocalDate borrowDate,
        LocalDate dueDate,
        String status,  // ACTIVE, OVERDUE, RETURNED
        LocalDate returnDate  // null si pas encore retourné
) {
}