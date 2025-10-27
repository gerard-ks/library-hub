package ci.org.application.member.usecases;

import ci.org.application.member.dtos.MemberResponse;
import ci.org.application.member.mappers.MemberMapper;
import ci.org.application.member.queries.GetMemberByIdQuery;
import ci.org.domain.member.entities.Member;
import ci.org.domain.member.exceptions.MemberNotFoundException;
import ci.org.domain.member.repositories.MemberRepository;
import ci.org.domain.member.valueobjects.MemberId;
import ci.org.shared.kernel.QueryUseCase;
import ci.org.shared.kernel.TransactionMode;
import ci.org.shared.kernel.UseCase;

import java.util.Objects;

@UseCase(value = "GetMemberById", transactionMode = TransactionMode.READ_ONLY)
public class GetMemberByIdUseCase implements QueryUseCase<GetMemberByIdQuery, MemberResponse> {

    private final MemberRepository memberRepository;

    public GetMemberByIdUseCase(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public MemberResponse execute(GetMemberByIdQuery query) {
        Objects.requireNonNull(query, "GetMemberByIdQuery cannot be null");

        // 1. Rechercher le membre
        Member member = memberRepository.findById(MemberId.of(query.memberId()))
                .orElseThrow(() -> new MemberNotFoundException(query.memberId()));

        // 2. Mapper vers DTO et retourner
        return MemberMapper.toResponse(member);
    }
}
