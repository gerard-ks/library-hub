package ci.org.presentation.rest.mappers;

import ci.org.application.loan.commands.*;
import ci.org.presentation.rest.requests.*;

/**
 * Mapper pour convertir les Request REST en Commands Application.
 */
public class LoanMapper {

    public static CreateLoanCommand toCommand(CreateLoanRequest request) {
        return new CreateLoanCommand(
                request.memberId(),
                request.bookId(),
                request.borrowDate(),
                request.dueDate()
        );
    }

    public static MarkLoanAsReturnedCommand toCommand(String loanId, ReturnLoanRequest request) {
        return new MarkLoanAsReturnedCommand(loanId, request.returnDate());
    }

    public static MarkLoanAsOverdueCommand toOverdueCommand(String loanId) {
        return new MarkLoanAsOverdueCommand(loanId);
    }
}