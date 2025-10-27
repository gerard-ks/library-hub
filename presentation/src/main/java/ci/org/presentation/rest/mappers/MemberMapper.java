package ci.org.presentation.rest.mappers;

import ci.org.application.member.commands.*;
import ci.org.presentation.rest.requests.AdjustMemberLimitRequest;
import ci.org.presentation.rest.requests.CreateMemberRequest;
import ci.org.presentation.rest.requests.UpdateMemberRequest;

/**
 * Mapper pour convertir les Request REST en Commands Application.
 */
public class MemberMapper {

    public static CreateMemberCommand toCommand(CreateMemberRequest request) {
        return new CreateMemberCommand(request.name());
    }

    public static UpdateMemberCommand toCommand(String memberId, UpdateMemberRequest request) {
        return new UpdateMemberCommand(memberId, request.name());
    }

    public static ForbidBorrowingCommand toForbidCommand(String memberId) {
        return new ForbidBorrowingCommand(memberId);
    }

    public static AllowBorrowingCommand toAllowCommand(String memberId) {
        return new AllowBorrowingCommand(memberId);
    }

    public static AdjustBorrowingLimitCommand toCommand(String memberId, AdjustMemberLimitRequest request) {
        return new AdjustBorrowingLimitCommand(memberId, request.newLimit());
    }
}