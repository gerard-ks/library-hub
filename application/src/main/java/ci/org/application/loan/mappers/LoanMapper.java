package ci.org.application.loan.mappers;

import ci.org.application.loan.responses.LoanResponse;
import ci.org.domain.loan.enitites.Loan;

/**
 * Mapper pour convertir entre Loan (domain) et LoanResponse (DTO).
 *
 * Pattern : Anti-Corruption Layer (ACL)
 */
public class LoanMapper {

    /**
     * Convertit un Loan (domain) en LoanResponse (DTO).
     */
    public static LoanResponse toResponse(Loan loan) {
        if (loan == null) {
            return null;
        }

        return new LoanResponse(
                loan.getId().value(),
                loan.getMemberId(),
                loan.getBookId(),
                loan.getBorrowDate(),
                loan.getDueDate(),
                loan.getStatus().name(),
                loan.getReturnDate()
        );
    }
}

