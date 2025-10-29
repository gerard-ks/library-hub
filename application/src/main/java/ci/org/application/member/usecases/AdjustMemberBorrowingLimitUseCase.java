package ci.org.application.member.usecases;

import ci.org.application.member.requests.commands.AdjustBorrowingLimitCommand;
import ci.org.application.member.requests.commands.UpdateMemberCommand;
import ci.org.application.member.responses.MemberResponse;
import ci.org.application.member.mappers.MemberMapper;
import ci.org.domain.member.entities.Member;
import ci.org.domain.member.exceptions.MemberNotFoundException;
import ci.org.domain.member.repositories.MemberRepository;
import ci.org.domain.member.valueobjects.MemberId;
import ci.org.shared.kernel.CommandUseCase;
import ci.org.shared.kernel.TransactionMode;
import ci.org.shared.kernel.UseCase;

import java.util.Objects;

@UseCase(value = "AdjustMemberLimit", transactionMode = TransactionMode.READ_WRITE)
public class AdjustMemberBorrowingLimitUseCase implements CommandUseCase<AdjustBorrowingLimitCommand, MemberResponse> {

    private final MemberRepository memberRepository;

    public AdjustMemberBorrowingLimitUseCase(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public MemberResponse execute(AdjustBorrowingLimitCommand command) {
        return memberRepository.findById(MemberId.of(command.memberId()))
                .map(member -> adjustLimitAndSave(member, command.newLimit()))
                .map(MemberMapper::toResponse)
                .orElseThrow(() -> new MemberNotFoundException(command.memberId()));
    }

    private Member adjustLimitAndSave(Member member, int newLimit) {
        member.adjustBorrowingLimit(newLimit);
        return memberRepository.save(member);
    }
}
