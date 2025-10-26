package ci.org.shared.kernel;

/**
 * Définit un cas d'utilisation qui interroge l'état du système.
 * @param <Q> Le type de l'objet Query (input).
 * @param <R> Le type du résultat attendu (output).
 */
@FunctionalInterface
public interface QueryUseCase<Q, R> {
    R execute(Q query);
}
