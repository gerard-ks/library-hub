package ci.org.domain.member.exceptions;

public class InvalidMemberNameException extends MemberException {
    public InvalidMemberNameException(String name) {
        super(String.format("Invalid member name: %s", name));
    }
}
