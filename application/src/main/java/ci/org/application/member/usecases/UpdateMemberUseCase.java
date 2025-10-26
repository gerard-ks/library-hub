package ci.org.application.member.usecases;

import ci.org.application.member.commands.UpdateMemberCommand;
import ci.org.application.member.dtos.MemberResponse;
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
        Objects.requireNonNull(command, "UpdateMemberCommand cannot be null");

        // 1. Rechercher le membre
        Member member = memberRepository.findById(MemberId.of(command.memberId()))
                .orElseThrow(() -> new MemberNotFoundException(command.memberId()));

        // 2. Appliquer la modification (validation métier automatique)
        member.updateName(command.newName());

        // 3. Persister
        Member updatedMember = memberRepository.save(member);

        // 4. Mapper vers DTO et retourner
        return MemberMapper.toResponse(updatedMember);
    }
}
