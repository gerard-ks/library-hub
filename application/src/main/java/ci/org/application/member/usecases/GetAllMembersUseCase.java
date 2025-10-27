package ci.org.application.member.usecases;

import ci.org.application.member.dtos.MemberResponse;
import ci.org.application.member.mappers.MemberMapper;
import ci.org.application.member.queries.GetAllMembersQuery;
import ci.org.domain.member.entities.Member;
import ci.org.domain.member.repositories.MemberRepository;
import ci.org.shared.kernel.*;

import java.util.Objects;

@UseCase(value = "GetAllMembers", transactionMode = TransactionMode.READ_ONLY)
public class GetAllMembersUseCase implements QueryUseCase<GetAllMembersQuery, Page<MemberResponse>> {

    private final MemberRepository memberRepository;

    public GetAllMembersUseCase(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Page<MemberResponse> execute(GetAllMembersQuery query) {
        Objects.requireNonNull(query, "GetAllMembersQuery cannot be null");

        // 1. Créer PageRequest (domain)
        PageRequest pageRequest = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(query.sortBy(), Sort.Direction.valueOf(query.sortDirection()))
        );

        // 2. Récupérer Page<Member> (domain)
        Page<Member> memberPage = memberRepository.findAll(pageRequest);

        // 3. Mapper vers Page<MemberResponse>
        return memberPage.map(MemberMapper::toResponse);
    }
}
