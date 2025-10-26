package ci.org.domain.loan.exceptions;

public class LoanNotFoundException extends LoanException {
   public LoanNotFoundException(String loanId) {
       super(String.format("Loan %s not found", loanId));
   }
}
