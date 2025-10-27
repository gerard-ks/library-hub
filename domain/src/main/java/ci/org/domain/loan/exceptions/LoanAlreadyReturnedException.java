package ci.org.domain.loan.exceptions;

import ci.org.domain.loan.valueobjects.LoanId;

public class LoanAlreadyReturnedException extends LoanException {
    public LoanAlreadyReturnedException(LoanId loanId) {
        super(String.format("Loan %s has already been returned", loanId.value()));
    }
}
