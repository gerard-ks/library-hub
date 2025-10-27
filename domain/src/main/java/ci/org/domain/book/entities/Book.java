package ci.org.domain.book.entities;

import ci.org.domain.book.exceptions.BookAlreadyAvailableException;
import ci.org.domain.book.exceptions.BookAlreadyUnavailableException;
import ci.org.domain.book.valueobjects.BookAuthor;
import ci.org.domain.book.valueobjects.BookId;
import ci.org.domain.book.valueobjects.BookTitle;
import ci.org.domain.book.valueobjects.Isbn;

import java.util.Objects;

/**
 * Aggregate Root : Livre de la bibliothèque.
 *
 * Responsabilité : Gérer la disponibilité d'un livre pour emprunt.
 *
 * Règles métier :
 * - Un livre possède un ISBN unique (format 10 ou 13 chiffres)
 * - Un livre possède un titre et un auteur obligatoires
 * - Par défaut, un livre est disponible (available = true)
 * - Un livre peut être marqué comme indisponible (emprunté)
 * - Un livre peut être marqué comme disponible (retourné)
 * - Protection contre double marquage
 */
public class Book {

    private final BookId id;
    private final Isbn isbn;
    private BookTitle title;
    private BookAuthor author;
    private boolean available;

    private Book(BookId id, Isbn isbn, BookTitle title, BookAuthor author, boolean available) {
        this.id = Objects.requireNonNull(id, "Book ID cannot be null");
        this.isbn = Objects.requireNonNull(isbn, "ISBN cannot be null");
        this.title = Objects.requireNonNull(title, "Book title cannot be null");
        this.author = Objects.requireNonNull(author, "Book author cannot be null");
        this.available = available;
    }

    public static Book create(String isbn, String title, String author) {
        return new Book(
                BookId.generate(),
                Isbn.of(isbn),
                BookTitle.of(title),
                BookAuthor.of(author),
                true  // available par défaut
        );
    }

    public static Book reconstitute(BookId id, String isbn, String title, String author, boolean available) {
        return new Book(id, Isbn.of(isbn), BookTitle.of(title), BookAuthor.of(author), available);
    }

    public void markAsUnavailable() {
        if (!this.available) {
            throw new BookAlreadyUnavailableException(this.id);
        }
        this.available = false;
    }

    public void markAsAvailable() {
        if (this.available) {
            throw new BookAlreadyAvailableException(this.id);
        }
        this.available = true;
    }

    public boolean canBeBorrowed() {
        return this.available;
    }

    public void updateTitle(String newTitle) {
        this.title = BookTitle.of(newTitle);;
    }

    public void updateAuthor(String newAuthor) {
        this.author = BookAuthor.of(newAuthor);
    }

    public BookId getId() {
        return id;
    }

    public Isbn getIsbn() {
        return isbn;
    }

    public String getIsbnValue() {
        return isbn.value();
    }

    public BookTitle getTitle() {
        return title;
    }

    public BookAuthor getAuthor() {
        return author;
    }

    public String getTitleValue() {
        return title.value();
    }

    public String getAuthorValue() {
        return author.value();
    }

    public boolean isAvailable() {
        return available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id.value() +
                ", isbn=" + isbn.value() +
                ", title='" + title.value() + '\'' +
                ", author='" + author.value() + '\'' +
                ", available=" + available +
                '}';
    }
}
