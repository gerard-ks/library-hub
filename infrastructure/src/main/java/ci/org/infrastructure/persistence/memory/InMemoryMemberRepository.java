package ci.org.infrastructure.persistence.memory;

import ci.org.domain.member.entities.Member;
import ci.org.domain.member.repositories.MemberRepository;
import ci.org.domain.member.valueobjects.MemberId;
import ci.org.shared.kernel.Page;
import ci.org.shared.kernel.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Repository
public class InMemoryMemberRepository implements MemberRepository {

    private final Map<String, Member> store = new ConcurrentHashMap<>();

    @Override
    public Member save(Member member) {
        Objects.requireNonNull(member, "Member cannot be null");
        store.put(member.getId().value(), member);
        return member;
    }

    @Override
    public Optional<Member> findById(MemberId id) {
        Objects.requireNonNull(id, "MemberId cannot be null");
        return Optional.ofNullable(store.get(id.value()));
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Page<Member> findAll(PageRequest pageRequest) {
        Objects.requireNonNull(pageRequest, "PageRequest cannot be null");

        List<Member> allMembers = new ArrayList<>(store.values());

        // Tri
        if (pageRequest.sort().isSorted()) {
            allMembers = sortMembers(allMembers, pageRequest);
        }

        // Pagination
        long totalElements = allMembers.size();
        int fromIndex = (int) pageRequest.getOffset();
        int toIndex = Math.min(fromIndex + pageRequest.pageSize(), allMembers.size());

        if (fromIndex > allMembers.size()) {
            return Page.of(Collections.emptyList(), pageRequest, totalElements);
        }

        List<Member> pageContent = allMembers.subList(fromIndex, toIndex);
        return Page.of(pageContent, pageRequest, totalElements);
    }

    @Override
    public boolean existsById(MemberId id) {
        Objects.requireNonNull(id, "MemberId cannot be null");
        return store.containsKey(id.value());
    }

    @Override
    public void deleteById(MemberId id) {
        Objects.requireNonNull(id, "MemberId cannot be null");
        store.remove(id.value());
    }

    @Override
    public long count() {
        return store.size();
    }

    /**
     * Trie les membres selon le PageRequest.
     */
    private List<Member> sortMembers(List<Member> members, PageRequest pageRequest) {
        Comparator<Member> comparator = null;

        for (var order : pageRequest.sort().getOrders()) {
            Comparator<Member> fieldComparator = switch (order.property()) {
                case "name" -> Comparator.comparing(Member::getNameValue);
                case "id" -> Comparator.comparing(m -> m.getId().value());
                default -> Comparator.comparing(Member::getNameValue);
            };

            if (order.direction() == ci.org.shared.kernel.Sort.Direction.DESC) {
                fieldComparator = fieldComparator.reversed();
            }

            comparator = (comparator == null) ? fieldComparator : comparator.thenComparing(fieldComparator);
        }

        if (comparator != null) {
            return members.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        }

        return members;
    }
}
