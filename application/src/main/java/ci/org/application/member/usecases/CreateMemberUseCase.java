package ci.org.application.member.usecases;

import ci.org.application.member.commands.CreateMemberCommand;
import ci.org.application.member.dtos.MemberResponse;
import ci.org.application.member.mappers.MemberMapper;
import ci.org.domain.member.entities.Member;
import ci.org.domain.member.repositories.MemberRepository;
import ci.org.shared.kernel.CommandUseCase;
import ci.org.shared.kernel.TransactionMode;
import ci.org.shared.kernel.UseCase;

import java.util.Objects;

@UseCase(value = "CreateMember", transactionMode = TransactionMode.READ_WRITE)
public class CreateMemberUseCase implements CommandUseCase<CreateMemberCommand, MemberResponse> {

    private final MemberRepository memberRepository;

    public CreateMemberUseCase(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public MemberResponse execute(CreateMemberCommand command) {
        Objects.requireNonNull(command, "CreateMemberCommand cannot be null");

        // 1. Créer l'entité domain (validation métier automatique)
        Member member = Member.create(command.name());

        // 2. Persister
        Member savedMember = memberRepository.save(member);

        // 3. Mapper vers DTO et retourner
        return MemberMapper.toResponse(savedMember);
    }
}
