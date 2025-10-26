package ci.org.domain.loan.exceptions;

import ci.org.shared.exception.DomainException;

public class LoanException extends DomainException {
    protected LoanException(String message) {
        super(message);
    }

    protected LoanException(String message, Throwable cause) {
        super(message, cause);
    }
}
