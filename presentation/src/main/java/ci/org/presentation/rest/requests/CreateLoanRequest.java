package ci.org.presentation.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO de requête pour créer un emprunt.
 */
public record CreateLoanRequest(
        @NotBlank(message = "Member ID cannot be blank")
        String memberId,

        @NotBlank(message = "Book ID cannot be blank")
        String bookId,

        @NotNull(message = "Borrow date cannot be null")
        LocalDate borrowDate,

        @NotNull(message = "Due date cannot be null")
        LocalDate dueDate
) {
}