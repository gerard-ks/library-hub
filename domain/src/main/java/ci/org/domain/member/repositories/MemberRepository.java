package ci.org.domain.member.repositories;

import ci.org.domain.member.entities.Member;
import ci.org.domain.member.valueobjects.MemberId;
import ci.org.shared.kernel.Page;
import ci.org.shared.kernel.PageRequest;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(MemberId id);
    List<Member> findAll();
    Page<Member> findAll(PageRequest pageRequest);
    boolean existsById(MemberId id);
    void deleteById(MemberId id);
    long count();
}
