package ci.org.application.member.usecases;


import ci.org.application.member.commands.ForbidBorrowingCommand;
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

@UseCase(value = "ForbidMemberBorrowing", transactionMode = TransactionMode.READ_WRITE)
public class ForbidBorrowingUseCase implements CommandUseCase<ForbidBorrowingCommand, MemberResponse> {

    private final MemberRepository memberRepository;

    public ForbidBorrowingUseCase(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public MemberResponse execute(ForbidBorrowingCommand command) {
        Objects.requireNonNull(command, "ForbidMemberBorrowingCommand cannot be null");

        // 1. Rechercher le membre
        Member member = memberRepository.findById(MemberId.of(command.memberId()))
                .orElseThrow(() -> new MemberNotFoundException(command.memberId()));

        // 2. Appliquer la règle métier
        member.forbidBorrowing();

        // 3. Persister
        Member updatedMember = memberRepository.save(member);

        // 4. Mapper vers DTO et retourner
        return MemberMapper.toResponse(updatedMember);
    }
}
