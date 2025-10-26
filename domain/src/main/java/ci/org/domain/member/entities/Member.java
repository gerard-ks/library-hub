package ci.org.domain.member.entities;

import ci.org.domain.member.exceptions.InvalidBorrowingLimitException;
import ci.org.domain.member.valueobjects.MemberId;
import ci.org.domain.member.valueobjects.MemberName;

import java.util.Objects;

/**
 * Aggregate Root : Membre de la bibliothèque.
 *
 * Responsabilité : Gérer l'éligibilité d'un membre à emprunter des livres.
 *
 * Règles métier :
 * - Un membre possède un nom (minimum 2 caractères)
 * - Par défaut, un membre peut emprunter (canBorrow = true)
 * - Chaque membre a une limite d'emprunts simultanés (défaut : 5)
 * - La limite peut être ajustée entre 1 et 20
 * - Un membre peut emprunter SI :
 *     • Il est autorisé (canBorrow = true)
 *     • Il n'a pas atteint sa limite d'emprunts actifs
 */
public class Member {
    private static final int DEFAULT_MAX_SIMULTANEOUS_LOANS = 5;
    private static final int MIN_BORROWING_LIMIT = 1;
    private static final int MAX_BORROWING_LIMIT = 20;

    private final MemberId id;
    private MemberName name;
    private boolean canBorrow;
    private int maxSimultaneousLoans;

    private Member(MemberId id, MemberName name, boolean canBorrow, int maxSimultaneousLoans) {
        this.id = Objects.requireNonNull(id, "Member ID cannot be null");
        this.name = Objects.requireNonNull(name, "Member name cannot be null");
        this.canBorrow = canBorrow;
        this.maxSimultaneousLoans = maxSimultaneousLoans;
    }

    public static Member create(String name) {
        return new Member(
                MemberId.generate(),
                MemberName.of(name),
                true,
                DEFAULT_MAX_SIMULTANEOUS_LOANS
        );
    }

    public static Member reconstitute(MemberId id, String name, boolean canBorrow, int maxSimultaneousLoans) {
        return new Member(id, MemberName.of(name), canBorrow, maxSimultaneousLoans);
    }

    /**
     * Vérifie si le membre est éligible pour emprunter un livre.
     *
     * Règle métier :
     * - Le membre doit être autorisé à emprunter (canBorrow = true)
     * - Le nombre d'emprunts actifs doit être inférieur à la limite
     *
     * @param currentActiveLoans le nombre d'emprunts actifs actuels
     * @return true si le membre peut emprunter, false sinon
     */
    public boolean isEligibleToBorrow(int currentActiveLoans) {
        if (currentActiveLoans < 0) {
            throw new IllegalArgumentException("Current active loans cannot be negative");
        }

        return this.canBorrow && currentActiveLoans < this.maxSimultaneousLoans;
    }

    /**
     * Interdit au membre d'emprunter.
     *
     * Cas d'usage : retards répétés, livres perdus, non-paiement, etc.
     */
    public void forbidBorrowing() {
        this.canBorrow = false;
    }

    /**
     * Autorise le membre à emprunter.
     *
     * Cas d'usage : situation régularisée, fin de suspension, etc.
     */
    public void allowBorrowing() {
        this.canBorrow = true;
    }

    /**
     * Ajuste la limite d'emprunts simultanés.
     *
     * Règle métier : La limite doit être entre 1 et 20.
     *
     * Cas d'usage :
     * - Augmentation : membre premium, VIP
     * - Diminution : sanction, membre débutant
     *
     * @param newLimit la nouvelle limite
     * @throws InvalidBorrowingLimitException si la limite est hors bornes
     */
    public void adjustBorrowingLimit(int newLimit) {
        if (newLimit < MIN_BORROWING_LIMIT || newLimit > MAX_BORROWING_LIMIT) {
            throw new InvalidBorrowingLimitException(newLimit);
        }
        this.maxSimultaneousLoans = newLimit;
    }

    /**
     * Modifie le nom du membre.
     *
     * @param newName le nouveau nom (sera validé par MemberName)
     */
    public void updateName(String newName) {
        this.name = MemberName.of(newName);
    }

    public MemberId getId() {
        return id;
    }

    public MemberName getName() {
        return name;
    }

    public String getNameValue() {
        return name.value();
    }

    public boolean canBorrow() {
        return canBorrow;
    }

    public int getMaxSimultaneousLoans() {
        return maxSimultaneousLoans;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name=" + name +
                ", canBorrow=" + canBorrow +
                ", maxSimultaneousLoans=" + maxSimultaneousLoans +
                '}';
    }
}
