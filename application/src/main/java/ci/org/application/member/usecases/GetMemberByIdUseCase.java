package ci.org.application.member.usecases;

import ci.org.application.member.responses.MemberResponse;
import ci.org.application.member.mappers.MemberMapper;
import ci.org.application.member.requests.queries.FindMemberQuery;
import ci.org.domain.member.entities.Member;
import ci.org.domain.member.exceptions.MemberNotFoundException;
import ci.org.domain.member.repositories.MemberRepository;
import ci.org.domain.member.valueobjects.MemberId;
import ci.org.shared.kernel.QueryUseCase;
import ci.org.shared.kernel.TransactionMode;
import ci.org.shared.kernel.UseCase;

import java.util.Objects;

@UseCase(value = "GetMemberById", transactionMode = TransactionMode.READ_ONLY)
public class GetMemberByIdUseCase implements QueryUseCase<FindMemberQuery, MemberResponse> {

    private final MemberRepository memberRepository;

    public GetMemberByIdUseCase(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public MemberResponse execute(FindMemberQuery query) {
      return memberRepository.findById(MemberId.of(query.memberId()))
              .map(MemberMapper::toResponse)
              .orElseThrow(() -> new MemberNotFoundException(query.memberId()));
    }
}
