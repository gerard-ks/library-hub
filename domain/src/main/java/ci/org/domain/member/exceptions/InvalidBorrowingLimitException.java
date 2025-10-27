package ci.org.domain.member.exceptions;

public class InvalidBorrowingLimitException extends MemberException {

    public InvalidBorrowingLimitException(int limit) {
        super(String.format(
                "Invalid borrowing limit: %d. Limit must be between 1 and 20.",
                limit
        ));
    }
}
