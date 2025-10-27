package ci.org.domain.member.exceptions;

import ci.org.domain.member.valueobjects.MemberId;

public class BorrowingLimitReachedException extends MemberException {

    public BorrowingLimitReachedException(MemberId memberId, int currentLoans, int maxLoans) {
        super(String.format(
                "Member %s has reached their borrowing limit (%d/%d active loans)",
                memberId.value(),
                currentLoans,
                maxLoans
        ));
    }
}
