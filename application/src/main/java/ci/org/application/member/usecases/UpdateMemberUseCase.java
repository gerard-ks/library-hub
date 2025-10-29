package ci.org.application.member.usecases;

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

@UseCase(value = "UpdateMember", transactionMode = TransactionMode.READ_WRITE)
public class UpdateMemberUseCase implements CommandUseCase<UpdateMemberCommand, MemberResponse> {

    private final MemberRepository memberRepository;

    public UpdateMemberUseCase(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public MemberResponse execute(UpdateMemberCommand command) {
        return memberRepository.findById(MemberId.of(command.memberId()))
                .map(member -> updateAndSave(member, command))
                .map(MemberMapper::toResponse)
                .orElseThrow(() -> new MemberNotFoundException(command.memberId()));
    }

    private Member updateAndSave(Member member, UpdateMemberCommand command) {
        member.updateName(command.newName());
        return memberRepository.save(member);
    }
}
