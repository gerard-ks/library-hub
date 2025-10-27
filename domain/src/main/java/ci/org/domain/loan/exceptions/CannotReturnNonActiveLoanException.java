package ci.org.domain.loan.exceptions;

import ci.org.domain.loan.valueobjects.LoanId;
import ci.org.domain.loan.valueobjects.LoanStatus;

public class CannotReturnNonActiveLoanException extends LoanException{
    public CannotReturnNonActiveLoanException(LoanId loanId, LoanStatus currentStatus) {
        super(String.format(
                "Cannot return loan %s with status %s. Only ACTIVE or OVERDUE loans can be returned.",
                loanId.value(),
                currentStatus
        ));
    }
}
