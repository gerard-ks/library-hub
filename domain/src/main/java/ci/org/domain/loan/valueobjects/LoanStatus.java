package ci.org.domain.loan.valueobjects;

/**
 * Enum représentant les différents états d'un emprunt.
 *
 * Cycle de vie d'un emprunt :
 * ACTIVE → OVERDUE (automatique si date dépassée)
 * ACTIVE → RETURNED (retour à temps)
 * OVERDUE → RETURNED (retour en retard)
 *
 * États terminaux : RETURNED
 */
public enum LoanStatus {
    /**
     * Emprunt actif, dans les délais.
     */
    ACTIVE,

    /**
     * Emprunt en retard (date de retour dépassée).
     */
    OVERDUE,

    /**
     * Emprunt retourné (état terminal).
     */
    RETURNED;

    /**
     * Vérifie si l'emprunt est dans un état terminal.
     *
     * @return true si l'état est terminal (RETURNED)
     */
    public boolean isTerminal() {
        return this == RETURNED;
    }

    /**
     * Vérifie si l'emprunt est actif (ACTIVE ou OVERDUE).
     *
     * @return true si l'emprunt est actif
     */
    public boolean isActive() {
        return this == ACTIVE || this == OVERDUE;
    }
}
