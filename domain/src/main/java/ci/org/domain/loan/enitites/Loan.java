package ci.org.domain.loan.enitites;

import ci.org.domain.loan.exceptions.CannotReturnNonActiveLoanException;
import ci.org.domain.loan.exceptions.LoanAlreadyReturnedException;
import ci.org.domain.loan.valueobjects.LoanId;
import ci.org.domain.loan.valueobjects.LoanPeriod;
import ci.org.domain.loan.valueobjects.LoanStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Aggregate Root : Emprunt de livre.
 *
 * Responsabilité : Gérer le cycle de vie d'un emprunt (création, retour, retard).
 *
 * Règles métier :
 * - Un emprunt lie un membre (memberId opaque) à un livre (bookId opaque)
 * - Un emprunt a une date d'emprunt (borrowDate) et une date de retour prévue (dueDate)
 * - dueDate doit être APRÈS borrowDate
 * - Par défaut, un emprunt est ACTIVE
 * - Un emprunt devient OVERDUE si la date actuelle > dueDate
 * - Un emprunt peut être retourné (RETURNED) seulement s'il est ACTIVE ou OVERDUE
 * - Un emprunt retourné ne peut plus changer d'état (état terminal)
 * - On peut calculer le nombre de jours de retard
 */
public class Loan {
    private final LoanId id;
    private final String memberId;
    private final String bookId;
    private final LoanPeriod period;
    private LoanStatus status;
    private LocalDate returnDate;

    private Loan(LoanId id, String memberId, String bookId, LoanPeriod period, LoanStatus status, LocalDate returnDate) {
        this.id = Objects.requireNonNull(id, "Loan ID cannot be null");
        this.memberId = validateMemberId(memberId);
        this.bookId = validateBookId(bookId);
        this.period = Objects.requireNonNull(period, "Loan period cannot be null");
        this.status = Objects.requireNonNull(status, "Loan status cannot be null");
        this.returnDate = returnDate;
    }

    public static Loan create(String memberId, String bookId, LocalDate borrowDate, LocalDate dueDate) {
        return new Loan(
                LoanId.generate(),
                memberId,
                bookId,
                LoanPeriod.of(borrowDate, dueDate),
                LoanStatus.ACTIVE,
                null  // Pas encore retourné
        );
    }

    public static Loan reconstitute(LoanId id, String memberId, String bookId,
                                    LocalDate borrowDate, LocalDate dueDate,
                                    LoanStatus status, LocalDate returnDate) {
        return new Loan(id, memberId, bookId, LoanPeriod.of(borrowDate, dueDate), status, returnDate);
    }

    /**
     * Marque l'emprunt comme retourné.
     *
     * Règle : Seuls les emprunts ACTIVE ou OVERDUE peuvent être retournés.
     *
     * @param returnDate la date de retour effective
     * @throws LoanAlreadyReturnedException si déjà retourné
     * @throws CannotReturnNonActiveLoanException si statut invalide
     */
    public void markAsReturned(LocalDate returnDate) {
        Objects.requireNonNull(returnDate, "Return date cannot be null");

        if (this.status == LoanStatus.RETURNED) {
            throw new LoanAlreadyReturnedException(this.id);
        }

        if (!this.status.isActive()) {
            throw new CannotReturnNonActiveLoanException(this.id, this.status);
        }

        this.status = LoanStatus.RETURNED;
        this.returnDate = returnDate;
    }

    /**
     * Vérifie si l'emprunt est en retard par rapport à une date donnée.
     *
     * Règle : Un emprunt est en retard si la date actuelle > dueDate
     * ET si l'emprunt n'est pas encore retourné.
     *
     * @param currentDate la date actuelle
     * @return true si l'emprunt est en retard
     */
    public boolean isOverdue(LocalDate currentDate) {
        Objects.requireNonNull(currentDate, "Current date cannot be null");

        return this.status.isActive() && currentDate.isAfter(period.dueDate());
    }

    /**
     * Marque l'emprunt comme en retard.
     *
     * Règle : Seul un emprunt ACTIVE peut devenir OVERDUE.
     *
     * @throws IllegalStateException si le statut n'est pas ACTIVE
     */
    public void markAsOverdue() {
        if (this.status != LoanStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only ACTIVE loans can be marked as OVERDUE. Current status: " + this.status
            );
        }

        this.status = LoanStatus.OVERDUE;
    }

    /**
     * Calcule le nombre de jours de retard.
     *
     * Règle :
     * - Si l'emprunt est retourné : différence entre returnDate et dueDate
     * - Si l'emprunt est actif : différence entre currentDate et dueDate
     * - Si pas de retard : retourne 0
     *
     * @param currentDate la date actuelle (ignorée si emprunt retourné)
     * @return le nombre de jours de retard (0 si pas de retard)
     */
    public long calculateLateDays(LocalDate currentDate) {
        Objects.requireNonNull(currentDate, "Current date cannot be null");

        if (this.status == LoanStatus.RETURNED && this.returnDate != null) {
            // Emprunt retourné : comparer returnDate avec dueDate
            long days = ChronoUnit.DAYS.between(period.dueDate(), this.returnDate);
            return Math.max(0, days);
        } else if (this.status.isActive()) {
            // Emprunt actif : comparer currentDate avec dueDate
            long days = ChronoUnit.DAYS.between(period.dueDate(), currentDate);
            return Math.max(0, days);
        }

        return 0;
    }

    /**
     * Calcule la durée totale de l'emprunt en jours.
     *
     * @return le nombre de jours entre borrowDate et dueDate
     */
    public long getLoanDurationInDays() {
        return period.getDurationInDays();
    }

    private String validateMemberId(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("Member ID cannot be null or blank");
        }
        return memberId.trim();
    }

    private String validateBookId(String bookId) {
        if (bookId == null || bookId.isBlank()) {
            throw new IllegalArgumentException("Book ID cannot be null or blank");
        }
        return bookId.trim();
    }

    public LoanId getId() {
        return id;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getBookId() {
        return bookId;
    }

    public LoanPeriod getPeriod() {
        return period;
    }

    public LocalDate getBorrowDate() {
        return period.borrowDate();
    }

    public LocalDate getDueDate() {
        return period.dueDate();
    }

    public LoanStatus getStatus() {
        return status;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public boolean isReturned() {
        return this.status == LoanStatus.RETURNED;
    }

    public boolean isActive() {
        return this.status.isActive();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loan loan = (Loan) o;
        return Objects.equals(id, loan.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", memberId='" + memberId + '\'' +
                ", bookId='" + bookId + '\'' +
                ", period=" + period +
                ", status=" + status +
                ", returnDate=" + returnDate +
                '}';
    }
}
