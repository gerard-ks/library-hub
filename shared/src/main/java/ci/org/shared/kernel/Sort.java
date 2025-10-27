package ci.org.shared.kernel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Value Object représentant un tri.
 *
 * Abstraction PURE du domain - ne dépend d'AUCUNE technologie.
 */
public class Sort {

    private final List<Order> orders;

    private Sort(List<Order> orders) {
        this.orders = Collections.unmodifiableList(new ArrayList<>(orders));
    }

    /**
     * Crée un tri sans ordre (unsorted).
     */
    public static Sort unsorted() {
        return new Sort(Collections.emptyList());
    }

    /**
     * Crée un tri avec un seul ordre.
     */
    public static Sort by(String property, Direction direction) {
        return new Sort(List.of(new Order(property, direction)));
    }

    /**
     * Crée un tri ASC par défaut.
     */
    public static Sort by(String property) {
        return by(property, Direction.ASC);
    }

    /**
     * Crée un tri avec plusieurs ordres.
     */
    public static Sort by(List<Order> orders) {
        return new Sort(orders);
    }

    public boolean isSorted() {
        return !orders.isEmpty();
    }

    public List<Order> getOrders() {
        return orders;
    }

    /**
     * Représente un ordre de tri (propriété + direction).
     */
    public record Order(String property, Direction direction) {
        public Order {
            if (property == null || property.isBlank()) {
                throw new IllegalArgumentException("Property cannot be null or blank");
            }
            if (direction == null) {
                throw new IllegalArgumentException("Direction cannot be null");
            }
        }

        public static Order asc(String property) {
            return new Order(property, Direction.ASC);
        }

        public static Order desc(String property) {
            return new Order(property, Direction.DESC);
        }
    }

    /**
     * Direction du tri.
     */
    public enum Direction {
        ASC, DESC
    }
}
