package ci.org.domain.loan.exceptions;

public class LoanNotAllowedException extends LoanException {
   public LoanNotAllowedException(String message) {
       super(String.format("Loan not allowed: %s", message));
   }
}
