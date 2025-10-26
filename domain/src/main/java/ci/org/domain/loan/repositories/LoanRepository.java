package ci.org.domain.loan.repositories;

import ci.org.domain.loan.enitites.Loan;
import ci.org.domain.loan.valueobjects.LoanId;
import ci.org.shared.kernel.Page;
import ci.org.shared.kernel.PageRequest;

import java.util.List;
import java.util.Optional;

public interface LoanRepository {
    Loan save(Loan loan);
    Optional<Loan> findById(LoanId id);
    Page<Loan> findByMemberId(String memberId, PageRequest pageRequest);
    Page<Loan> findOverdueLoans(PageRequest pageRequest);
    List<Loan> findAll();

    List<Loan> findActiveByMemberId(String memberId);

    long countActiveByMemberId(String memberId);

    boolean existsById(LoanId id);
    void deleteById(LoanId id);
    long count();

    Page<Loan> findActiveLoans(PageRequest pageRequest);
}
