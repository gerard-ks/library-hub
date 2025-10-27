package ci.org.domain.loan.exceptions;

import java.time.LocalDate;

public class InvalidLoanPeriodException extends LoanException{
    public InvalidLoanPeriodException(String message) {
        super(message);
    }

    public InvalidLoanPeriodException(LocalDate borrowDate, LocalDate dueDate) {
        super(String.format(
                "Invalid loan period: due date (%s) must be after borrow date (%s)",
                dueDate,
                borrowDate
        ));
    }
}
