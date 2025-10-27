package ci.org.shared.kernel;

/**
 * Définit le type de transaction requis pour un UseCase.
 */
public enum TransactionMode {
    /**
     * Aucune transaction n'est requise.
     */
    NONE,

    /**
     * Une transaction en lecture/écriture (read-write) est requise.
     * (Défaut pour les Commandes)
     */
    READ_WRITE,

    /**
     * Une transaction en lecture seule (read-only) est requise.
     * (Défaut pour les Requêtes)
     */
    READ_ONLY
}
