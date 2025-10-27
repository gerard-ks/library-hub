package ci.org.domain.member.exceptions;

public class MemberCannotBorrowException extends MemberException{
    public MemberCannotBorrowException(String memberId) {
        super(String.format("Member %s cannot borrow books", memberId));
    }
}
