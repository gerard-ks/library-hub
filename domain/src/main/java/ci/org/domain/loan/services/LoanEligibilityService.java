package ci.org.domain.loan.services;

import ci.org.domain.book.entities.Book;
import ci.org.domain.member.entities.Member;
import ci.org.shared.kernel.DomainService;

/**
 * Domain Service : Service d'éligibilité pour les emprunts.
 *
 * Responsabilité : Coordonner les règles métier entre Member et Book
 * pour déterminer si un emprunt est possible.
 *
 * Pourquoi un Domain Service ?
 * - Cette logique ne peut pas appartenir à Member seul
 * - Cette logique ne peut pas appartenir à Book seul
 * - Elle nécessite la coordination de DEUX aggregates
 *
 * Règles métier :
 * - Le membre doit être éligible pour emprunter (Member.isEligibleToBorrow)
 * - Le livre doit être disponible (Book.canBeBorrowed)
 */

@DomainService
public class LoanEligibilityService {

    /**
     * Vérifie si un membre peut emprunter un livre.
     *
     * Conditions :
     * 1. Le membre doit être autorisé à emprunter (Member.canBorrow = true)
     * 2. Le membre ne doit pas avoir atteint sa limite d'emprunts
     * 3. Le livre doit être disponible
     *
     * @param member le membre qui souhaite emprunter
     * @param book le livre à emprunter
     * @param currentActiveLoansByMember le nombre d'emprunts actifs du membre
     * @return true si l'emprunt est possible, false sinon
     */
    public boolean canMemberBorrowBook(Member member, Book book, int currentActiveLoansByMember) {
        // Validation des paramètres
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }

        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }

        if (currentActiveLoansByMember < 0) {
            throw new IllegalArgumentException("Current active loans cannot be negative");
        }

        // Règle 1 & 2 : Le membre doit être éligible
        boolean memberIsEligible = member.isEligibleToBorrow(currentActiveLoansByMember);

        // Règle 3 : Le livre doit être disponible
        boolean bookIsAvailable = book.canBeBorrowed();

        // Les deux conditions doivent être vraies
        return memberIsEligible && bookIsAvailable;
    }

    /**
     * Vérifie uniquement si le membre peut emprunter (sans vérifier le livre).
     *
     * Utile pour des vérifications préliminaires.
     *
     * @param member le membre
     * @param currentActiveLoansByMember le nombre d'emprunts actifs
     * @return true si le membre peut emprunter
     */
    public boolean isMemberEligibleToBorrow(Member member, int currentActiveLoansByMember) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }

        if (currentActiveLoansByMember < 0) {
            throw new IllegalArgumentException("Current active loans cannot be negative");
        }

        return member.isEligibleToBorrow(currentActiveLoansByMember);
    }

    /**
     * Vérifie uniquement si le livre peut être emprunté (sans vérifier le membre).
     *
     * Utile pour des vérifications préliminaires.
     *
     * @param book le livre
     * @return true si le livre est disponible
     */
    public boolean isBookAvailableForBorrowing(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }

        return book.canBeBorrowed();
    }
}
