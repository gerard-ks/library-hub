package ci.org.domain.book.repositories;

import ci.org.domain.book.entities.Book;
import ci.org.domain.book.valueobjects.BookId;
import ci.org.domain.book.valueobjects.Isbn;
import ci.org.shared.kernel.Page;
import ci.org.shared.kernel.PageRequest;

import java.util.Optional;

public interface BookRepository {
    Book save(Book book);
    Optional<Book> findById(BookId id);
    Optional<Book> findByIsbn(Isbn isbn);
    Page<Book> findAll(PageRequest pageRequest);
    boolean existsById(BookId id);
    void deleteById(BookId id);
    long count();
}
