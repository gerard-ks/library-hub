package ci.org.application.member.usecases;


import ci.org.application.member.requests.commands.ForbidBorrowingCommand;
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

@UseCase(value = "ForbidMemberBorrowing", transactionMode = TransactionMode.READ_WRITE)
public class ForbidBorrowingUseCase implements CommandUseCase<ForbidBorrowingCommand, MemberResponse> {

    private final MemberRepository memberRepository;

    public ForbidBorrowingUseCase(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public MemberResponse execute(ForbidBorrowingCommand command) {
        return memberRepository.findById(MemberId.of(command.memberId()))
                .map(this::forbidBorrowingAndSave)
                .map(MemberMapper::toResponse)
                .orElseThrow(() -> new MemberNotFoundException(command.memberId()));
    }

    private Member forbidBorrowingAndSave(Member member) {
        member.forbidBorrowing();
        return memberRepository.save(member);
    }
}
