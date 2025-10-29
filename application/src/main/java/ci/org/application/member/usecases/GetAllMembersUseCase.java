package ci.org.application.member.usecases;

import ci.org.application.member.responses.MemberResponse;
import ci.org.application.member.mappers.MemberMapper;
import ci.org.application.member.requests.queries.ListMembersQuery;
import ci.org.domain.member.entities.Member;
import ci.org.domain.member.repositories.MemberRepository;
import ci.org.shared.kernel.*;

import java.util.Objects;

@UseCase(value = "GetAllMembers", transactionMode = TransactionMode.READ_ONLY)
public class GetAllMembersUseCase implements QueryUseCase<ListMembersQuery, Page<MemberResponse>> {

    private final MemberRepository memberRepository;

    public GetAllMembersUseCase(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Page<MemberResponse> execute(ListMembersQuery query) {
        return memberRepository.findAll(toPageRequest(query))
                .map(MemberMapper::toResponse);
    }

    private PageRequest toPageRequest(ListMembersQuery query) {
        return PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(query.sortBy(), Sort.Direction.valueOf(query.sortDirection()))
        );
    }
}
