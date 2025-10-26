package ci.org.infrastructure.persistence.memory;

import ci.org.domain.book.entities.Book;
import ci.org.domain.book.repositories.BookRepository;
import ci.org.domain.book.valueobjects.BookId;
import ci.org.domain.book.valueobjects.Isbn;
import ci.org.shared.kernel.Page;
import ci.org.shared.kernel.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implémentation en mémoire du BookRepository.
 *
 * Thread-safe grâce à ConcurrentHashMap.
 */
@Repository
public class InMemoryBookRepository implements BookRepository {

    private final Map<String, Book> store = new ConcurrentHashMap<>();

    @Override
    public Book save(Book book) {
        Objects.requireNonNull(book, "Book cannot be null");
        store.put(book.getId().value(), book);
        return book;
    }

    @Override
    public Optional<Book> findById(BookId id) {
        Objects.requireNonNull(id, "BookId cannot be null");
        return Optional.ofNullable(store.get(id.value()));
    }

    @Override
    public Optional<Book> findByIsbn(Isbn isbn) {
        Objects.requireNonNull(isbn, "Isbn cannot be null");
        return store.values().stream()
                .filter(book -> book.getIsbn().equals(isbn))
                .findFirst();
    }

    @Override
    public Page<Book> findAll(PageRequest pageRequest) {
        Objects.requireNonNull(pageRequest, "PageRequest cannot be null");

        List<Book> allBooks = new ArrayList<>(store.values());

        // Tri
        if (pageRequest.sort().isSorted()) {
            allBooks = sortBooks(allBooks, pageRequest);
        }

        // Pagination
        long totalElements = allBooks.size();
        int fromIndex = (int) pageRequest.getOffset();
        int toIndex = Math.min(fromIndex + pageRequest.pageSize(), allBooks.size());

        if (fromIndex > allBooks.size()) {
            return Page.of(Collections.emptyList(), pageRequest, totalElements);
        }

        List<Book> pageContent = allBooks.subList(fromIndex, toIndex);
        return Page.of(pageContent, pageRequest, totalElements);
    }

    @Override
    public boolean existsById(BookId id) {
        Objects.requireNonNull(id, "BookId cannot be null");
        return store.containsKey(id.value());
    }

    @Override
    public void deleteById(BookId id) {
        Objects.requireNonNull(id, "BookId cannot be null");
        store.remove(id.value());
    }

    @Override
    public long count() {
        return store.size();
    }

    /**
     * Trie les livres selon le PageRequest.
     */
    private List<Book> sortBooks(List<Book> books, PageRequest pageRequest) {
        Comparator<Book> comparator = null;

        for (var order : pageRequest.sort().getOrders()) {
            Comparator<Book> fieldComparator = switch (order.property()) {
                case "title" -> Comparator.comparing(b -> b.getTitle().value());
                case "author" -> Comparator.comparing(b -> b.getAuthor().value());
                case "isbn" -> Comparator.comparing(b -> b.getIsbn().value());
                case "id" -> Comparator.comparing(b -> b.getId().value());
                default -> Comparator.comparing(b -> b.getTitle().value());
            };

            if (order.direction() == ci.org.shared.kernel.Sort.Direction.DESC) {
                fieldComparator = fieldComparator.reversed();
            }

            comparator = (comparator == null) ? fieldComparator : comparator.thenComparing(fieldComparator);
        }

        if (comparator != null) {
            return books.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        }

        return books;
    }
}