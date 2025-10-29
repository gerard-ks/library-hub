package ci.org.application.member.usecases;

import ci.org.application.member.requests.commands.AllowBorrowingCommand;
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

@UseCase(value = "AllowMemberBorrowing", transactionMode = TransactionMode.READ_WRITE)
public class AllowBorrowingUseCase implements CommandUseCase<AllowBorrowingCommand, MemberResponse> {

    private final MemberRepository memberRepository;

    public AllowBorrowingUseCase(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public MemberResponse execute(AllowBorrowingCommand command) {
        return memberRepository.findById(MemberId.of(command.memberId()))
                .map(this::allowBorrowingAndSave)
                .map(MemberMapper::toResponse)
                .orElseThrow(() -> new MemberNotFoundException(command.memberId()));
    }

    private Member allowBorrowingAndSave(Member member) {
        member.allowBorrowing();
        return memberRepository.save(member);
    }
}
