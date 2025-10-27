package ci.org.infrastructure.persistence.memory;

import ci.org.domain.loan.enitites.Loan;
import ci.org.domain.loan.repositories.LoanRepository;
import ci.org.domain.loan.valueobjects.LoanId;
import ci.org.domain.loan.valueobjects.LoanStatus;
import ci.org.shared.kernel.Page;
import ci.org.shared.kernel.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implémentation en mémoire du LoanRepository.
 *
 * Thread-safe grâce à ConcurrentHashMap.
 */
@Repository
public class InMemoryLoanRepository implements LoanRepository {

    private final Map<String, Loan> store = new ConcurrentHashMap<>();

    @Override
    public Loan save(Loan loan) {
        Objects.requireNonNull(loan, "Loan cannot be null");
        store.put(loan.getId().value(), loan);
        return loan;
    }

    @Override
    public Optional<Loan> findById(LoanId id) {
        Objects.requireNonNull(id, "LoanId cannot be null");
        return Optional.ofNullable(store.get(id.value()));
    }

    @Override
    public List<Loan> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Page<Loan> findActiveLoans(PageRequest pageRequest) {
        Objects.requireNonNull(pageRequest, "PageRequest cannot be null");

        List<Loan> activeLoans = store.values().stream()
                .filter(Loan::isActive)
                .collect(Collectors.toList());

        return paginateAndSort(activeLoans, pageRequest);
    }

    @Override
    public Page<Loan> findOverdueLoans(PageRequest pageRequest) {
        Objects.requireNonNull(pageRequest, "PageRequest cannot be null");

        LocalDate currentDate = LocalDate.now();
        List<Loan> overdueLoans = store.values().stream()
                .filter(loan -> loan.getStatus() == LoanStatus.OVERDUE || loan.isOverdue(currentDate))
                .collect(Collectors.toList());

        return paginateAndSort(overdueLoans, pageRequest);
    }

    @Override
    public Page<Loan> findByMemberId(String memberId, PageRequest pageRequest) {
        Objects.requireNonNull(memberId, "MemberId cannot be null");
        Objects.requireNonNull(pageRequest, "PageRequest cannot be null");

        List<Loan> memberLoans = store.values().stream()
                .filter(loan -> loan.getMemberId().equals(memberId))
                .collect(Collectors.toList());

        return paginateAndSort(memberLoans, pageRequest);
    }

    @Override
    public List<Loan> findActiveByMemberId(String memberId) {
        Objects.requireNonNull(memberId, "MemberId cannot be null");

        return store.values().stream()
                .filter(loan -> loan.getMemberId().equals(memberId))
                .filter(Loan::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public long countActiveByMemberId(String memberId) {
        Objects.requireNonNull(memberId, "MemberId cannot be null");

        return store.values().stream()
                .filter(loan -> loan.getMemberId().equals(memberId))
                .filter(Loan::isActive)
                .count();
    }

    @Override
    public boolean existsById(LoanId id) {
        Objects.requireNonNull(id, "LoanId cannot be null");
        return store.containsKey(id.value());
    }

    @Override
    public void deleteById(LoanId id) {
        Objects.requireNonNull(id, "LoanId cannot be null");
        store.remove(id.value());
    }

    @Override
    public long count() {
        return store.size();
    }

    /**
     * Pagine et trie une liste d'emprunts.
     */
    private Page<Loan> paginateAndSort(List<Loan> loans, PageRequest pageRequest) {
        // Tri
        if (pageRequest.sort().isSorted()) {
            loans = sortLoans(loans, pageRequest);
        }

        // Pagination
        long totalElements = loans.size();
        int fromIndex = (int) pageRequest.getOffset();
        int toIndex = Math.min(fromIndex + pageRequest.pageSize(), loans.size());

        if (fromIndex > loans.size()) {
            return Page.of(Collections.emptyList(), pageRequest, totalElements);
        }

        List<Loan> pageContent = loans.subList(fromIndex, toIndex);
        return Page.of(pageContent, pageRequest, totalElements);
    }

    /**
     * Trie les emprunts selon le PageRequest.
     */
    private List<Loan> sortLoans(List<Loan> loans, PageRequest pageRequest) {
        Comparator<Loan> comparator = null;

        for (var order : pageRequest.sort().getOrders()) {
            Comparator<Loan> fieldComparator = switch (order.property()) {
                case "borrowDate" -> Comparator.comparing(Loan::getBorrowDate);
                case "dueDate" -> Comparator.comparing(Loan::getDueDate);
                case "status" -> Comparator.comparing(l -> l.getStatus().name());
                case "memberId" -> Comparator.comparing(Loan::getMemberId);
                case "bookId" -> Comparator.comparing(Loan::getBookId);
                case "id" -> Comparator.comparing(l -> l.getId().value());
                default -> Comparator.comparing(Loan::getBorrowDate);
            };

            if (order.direction() == ci.org.shared.kernel.Sort.Direction.DESC) {
                fieldComparator = fieldComparator.reversed();
            }

            comparator = (comparator == null) ? fieldComparator : comparator.thenComparing(fieldComparator);
        }

        if (comparator != null) {
            return loans.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        }

        return loans;
    }
}