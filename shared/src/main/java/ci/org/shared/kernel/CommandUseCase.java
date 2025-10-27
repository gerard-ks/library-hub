package ci.org.shared.kernel;

/**
 * Définit un cas d'utilisation qui modifie l'état du système.
 * @param <C> Le type de l'objet Command (input).
 * @param <R> Le type du résultat attendu (Output).
 */
@FunctionalInterface
public interface CommandUseCase<C, R> {
    R execute(C command);
}
