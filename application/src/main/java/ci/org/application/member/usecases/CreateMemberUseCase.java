package ci.org.application.member.usecases;

import ci.org.application.member.requests.commands.CreateMemberCommand;
import ci.org.application.member.responses.MemberResponse;
import ci.org.application.member.mappers.MemberMapper;
import ci.org.domain.member.entities.Member;
import ci.org.domain.member.repositories.MemberRepository;
import ci.org.shared.kernel.CommandUseCase;
import ci.org.shared.kernel.TransactionMode;
import ci.org.shared.kernel.UseCase;

@UseCase(value = "CreateMember", transactionMode = TransactionMode.READ_WRITE)
public class CreateMemberUseCase implements CommandUseCase<CreateMemberCommand, MemberResponse> {

    private final MemberRepository memberRepository;

    public CreateMemberUseCase(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public MemberResponse execute(CreateMemberCommand command) {
        Member savedMember = memberRepository.save(Member.create(command.name()));
        return MemberMapper.toResponse(savedMember);
    }
}
