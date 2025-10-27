package ci.org.shared.kernel;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseCase {
    String value() default "";

    /**
     * Spécifie le mode de transaction pour ce cas d'utilisation.
     * (Défaut : NONE)
     */
    TransactionMode transactionMode() default TransactionMode.NONE;
}
