package ci.org.domain.member.exceptions;

public class MemberNotFoundException extends MemberException {
   public MemberNotFoundException(String memberId) {
       super(String.format("Member with id %s not found", memberId));
   }
}
