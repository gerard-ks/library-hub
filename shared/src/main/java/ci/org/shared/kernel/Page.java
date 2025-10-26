package ci.org.shared.kernel;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Value Object représentant une page de résultats.
 *
 * Abstraction PURE du domain - ne dépend d'AUCUNE technologie.
 * L'infrastructure créera ce VO à partir de Spring Page, JPA, etc.
 *
 * @param <T> Type des éléments
 */
public class Page<T> {

    private final List<T> content;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;

    private Page(List<T> content, int pageNumber, int pageSize, long totalElements) {
        this.content = Collections.unmodifiableList(content);
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;
    }

    /**
     * Crée une page de résultats.
     */
    public static <T> Page<T> of(List<T> content, PageRequest pageRequest, long totalElements) {
        return new Page<>(content, pageRequest.pageNumber(), pageRequest.pageSize(), totalElements);
    }

    /**
     * Crée une page vide.
     */
    public static <T> Page<T> empty(PageRequest pageRequest) {
        return new Page<>(Collections.emptyList(), pageRequest.pageNumber(), pageRequest.pageSize(), 0);
    }

    /**
     * Transforme le contenu de la page.
     *
     * Exemple : Page<Member> → Page<MemberResponse>
     */
    public <U> Page<U> map(Function<T, U> mapper) {
        List<U> mappedContent = content.stream()
                .map(mapper)
                .toList();
        return new Page<>(mappedContent, pageNumber, pageSize, totalElements);
    }

    // --- Getters ---

    public List<T> getContent() {
        return content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean hasContent() {
        return !content.isEmpty();
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public boolean isFirst() {
        return pageNumber == 0;
    }

    public boolean isLast() {
        return pageNumber >= totalPages - 1;
    }

    public boolean hasNext() {
        return pageNumber < totalPages - 1;
    }

    public boolean hasPrevious() {
        return pageNumber > 0;
    }

    public int getNumberOfElements() {
        return content.size();
    }
}

