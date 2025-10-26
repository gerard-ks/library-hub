package ci.org.domain.member.exceptions;

import ci.org.shared.exception.DomainException;

public class MemberException extends DomainException {
    protected MemberException(String message) {
        super(message);
    }

    protected MemberException(String message, Throwable cause) {
        super(message, cause);
    }
}
